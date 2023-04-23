package weiner.noah.groceryguide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import java.util.Objects;

//This code is based on code found in https://github.com/ChenHongruixuan/In-outdoorSeamlessPositioningNavigationSystem

//A Service is an application component that can perform long-running operations in the background
public class LocationService extends Service implements SensorEventListener {
    private boolean isStopInertialLoc = false; // flag for whether the thread of inertial positioning is stopped
    private float[] accCurrVal = new float[3];    // storing acceleration values returned by sensor
    private float[] orientCurrVal = new float[3];    // storing orientation values returned by sensor

    //buffer of acceleration magnitude vals
    private double[] accelSlidingWindowBuffer = new double[SLIDING_WINDOW_LEN];

    private static String TAG = "LocationService";

    //how many radians = 1 deg?
    private static final double RADIANS_PER_DEGREE = Math.PI / 180;

    //the value of k in self defined nonlinear step size algorithm
    private static final double kConst = 0.026795089522;

    //private static final double M_L_L_CONST = 111194.926644558;
    private static final double DIST_MULTIPLIER = 1;

    private static final int SLIDING_WINDOW_LEN = 31;

    private static final double MAX_ACCEL_VAL_LOWER_BOUND = 11.8;
    private static final double MAX_ACCEL_VAL_UPPER_BOUND = 20.0;

    private static final double MIN_ACCEL_VAL_LOWER_BOUND = 4.0;
    private static final double MIN_ACCEL_VAL_UPPER_BOUND = 8.8;

    //background Thread on which the service runs
    private Thread locServiceThread;

    private SensorManager sensorManager;
    private int count = 0, stepCount = 0;
    private long startTime, endTime;

    private int accFrame = 0, orFrame = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "LocationService onCreate()!");

        //get sensor manager
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        //get the sensors themselves and register the listeners to listen for incoming sensor readings
        Sensor accelerometer = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //accelerometer vals are: [0] x accel, [1] y accel, [2] z accel
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //orientation vals are: [0] azimuth (deg of rotation around -z axis)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
    }


    /**
     * The system invokes this method by calling startService() when another component (ie Activity) requests that service be started. When this method executes,
     * service is started and can run in background indefinitely. If you implement this, it's your responsibility to stop service when its work is complete
     * by calling stopSelf() or stopService(). If you only want to provide binding, you don't need to implement this method.
     *
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand() called!");

        //create new thread
        locServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //initialize the sliding window of acceleration
                initAccSlidingWindow();

                //get the initial client position via the intent
                UserPosition pos = (UserPosition) Objects.requireNonNull(intent).getSerializableExtra("init_pos");

                double stepSize;

                //when phone is at rest, the acceleration magnitude should be about 9.8 (since z accel is calibrated to be 9.8 at rest)
                //z axis points down to ground, -z points up to sky?
                double accelMaxTemp = 9.8, accelMinTemp = 9.8;

                //get initial lat and lon
                double posLat = pos.getLat(), posLon = pos.getLon();

                boolean isStartCheckMinValue = false, isStartCheckMaxValue = true;

                //get current time
                startTime = System.currentTimeMillis();

                //while service should still be running
                while (!isStopInertialLoc) {
                    count++;

                    //throw out oldest accel val in the buffer (head) and store the newest one at tail
                    //notice we're recording acceleration magnitudes, no direction information coming from accelerometer Sensor
                    moveAccWindow(computeAccelMagnitude());

                    //if the user is deemed to be in motion
                    if (isUserInMotion()) {
                        //check if there's a peak value of acceleration at the center of the window buffer
                        if (isStartCheckMaxValue && accelSlidingWindowBuffer[15] > accelSlidingWindowBuffer[14] &&
                                accelSlidingWindowBuffer[15] > accelSlidingWindowBuffer[16] &&
                                isValidMaxAccelVal(accelSlidingWindowBuffer[15]) && //make sure peak value is high enough
                                accelMaxTemp < accelSlidingWindowBuffer[15]) { //make sure the peak value has some positive magnitude

                            //save the peak accel value
                            accelMaxTemp = accelSlidingWindowBuffer[15];

                            //when peak value of acceleration appears for the first time, start to detect the valley value of acceleration
                            isStartCheckMinValue = true;
                        }

                        //check if there's a valley (min) value of accel at the center of the window buffer
                        if (isStartCheckMinValue && accelSlidingWindowBuffer[15] < accelSlidingWindowBuffer[14] &&
                                accelSlidingWindowBuffer[15] < accelSlidingWindowBuffer[16] &&
                                isValidMinAccelVal(accelSlidingWindowBuffer[15]) && //make sure valley value is in correct rg of vals
                                accelMinTemp > accelSlidingWindowBuffer[15]) { //make sure valley value has some negative magnitude //FIXME wut??

                            accelMinTemp = accelSlidingWindowBuffer[15];

                            //when valley value of acceleration appears for the first time, start to detect the effective peak value of acceleration
                            isStartCheckMaxValue = false;
                        }

                        //if a valid valley value of acceleration has been detected, the acceleration starts to rise, which means that the previous step has been completed and a new step is started
                        if (!isStartCheckMaxValue && accelSlidingWindowBuffer[15] >= 9.0) {
                            stepCount++;

                            //get approximate step size of this step (in meters)
                            stepSize = getThisStepDist(accelMinTemp, accelMaxTemp);

                            //get the azimuth orientation
                            float angle = orientCurrVal[0];

                            //get new lat,lon by adding the step dist
                            String[] llStr = computeNewPos(stepSize, posLat, posLon, angle).split(",");

                            posLat = Double.parseDouble(llStr[0]);
                            posLon = Double.parseDouble(llStr[1]);

                            //update the user's position
                            pos = new UserPosition(posLat, posLon);

                            //broadcast new user position via an Intent obj
                            Intent posIntent = new Intent("locate");
                            posIntent.putExtra("pos_data", pos);
                            sendBroadcast(posIntent);

                            //init params
                            accelMaxTemp = 9.8;
                            accelMinTemp = 9.8;

                            isStartCheckMaxValue = true;
                            isStartCheckMinValue = false;
                        }
                    }

                    //otherwise, the user is standing still
                    else {
                        posLat = CurrentUserPosition.getCurrLat();
                        posLon = CurrentUserPosition.getCurrLon();
                    }


                    try {
                        //this sets the "sample rate"
                        Thread.sleep(20);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Location wait error:" + e.getMessage());
                    }
                }

                //the service has been flagged for STOPping
                endTime = System.currentTimeMillis();
                stopSelf();
            }
        });

        //set thread priority and start thread
        locServiceThread.setPriority(10);
        locServiceThread.start();

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Initialize the parameters of inertial location and pad the sliding window of acceleration.
     */
    private void initAccSlidingWindow() {
        for (int i = 0; i < accelSlidingWindowBuffer.length; i++) {
            //fill entire window with current accel magnitude val
            accelSlidingWindowBuffer[i] = computeAccelMagnitude();

            //sleep
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "initAccSlidingWindow() encountered error while sleeping: " + e.getMessage());
            }
        }
    }

    /**
     * Slide the acceleration buffer "forward" by throwing out the oldest value (head).
     * Then, add the newest acceleration magnitude to tail of buffer.
     *
     * @param accelMagnitude Magnitude of acceleration
     */
    private void moveAccWindow(double accelMagnitude) {
        //arraycopy() copies source array from a specific beginning position to the destination array from the mentioned position
        //so here we're sliding the acceleration window to the left one position (throwing out first val), leaving a space at end of the buffer
        System.arraycopy(accelSlidingWindowBuffer, 1, accelSlidingWindowBuffer, 0, SLIDING_WINDOW_LEN - 1);

        //store new acceleration at end of buffer
        accelSlidingWindowBuffer[SLIDING_WINDOW_LEN - 1] = accelMagnitude;
    }

    /**
     * Judging whether the user is in motion according to the standard deviation of the acceleration sliding window buffer.
     * When the standard deviation of acceleration is greater than 0.8, the user is considered to be in motion since that means the acceleration moved significantly
     * in the past n sensor frames.
     *
     * @return Whether or not user is in motion.
     */
    private boolean isUserInMotion() {
        return getAccStd() >= 0.8;
    }

    /**
     * Obtain the step size of one step by self-defined nonlinear step size algorithm
     *
     * @param accMin Valley value of acceleration in a step
     * @param accMax Peak value of acceleration in a step
     * @return step size
     */
    private double getThisStepDist(double accMin, double accMax) {
        return kConst * ((accMax - accMin) * 3.5 + Math.pow(accMax - accMin, 0.25));
    }

    /**
     * Method for returning the magnitude of the current acceleration
     *
     * @return Magnitude of acceleration collected by acceleration sensor
     */
    private double computeAccelMagnitude() {
        //magnitude of the 3D accel vector is sqrt of sum of squares
        return Math.sqrt(accCurrVal[0] * accCurrVal[0] + accCurrVal[1] * accCurrVal[1] + accCurrVal[2] * accCurrVal[2]);
    }

    /**
     * Calculate the standard deviation of acceleration in sliding window buffer.
     *
     * @return Standard deviation of acceleration in the sliding window buffer.
     */
    private double getAccStd() {
        return Math.sqrt(getAccVar());
    }


    /**
     * Calculate the variance of acceleration in sliding window buffer.
     *
     * @return Variance of acceleration in the sliding window buffer.
     */
    private double getAccVar() {
        double accVar = 0;
        double accMean = getAccelMean();

        //variance = (sum[(val - mean)^2] for all vals) / total num of vals
        for (double val : accelSlidingWindowBuffer)
            accVar += (val - accMean) * (val - accMean);

        return accVar / accelSlidingWindowBuffer.length;
    }

    /**
     * Calculate the mean of acceleration in sliding window buffer (last n number of acceleration vals)
     *
     * @return Mean of acceleration in the sliding window buffer.
     */
    private double getAccelMean() {
        double cumSum = 0;

        for (double val : accelSlidingWindowBuffer)
            cumSum += val;

        return cumSum / accelSlidingWindowBuffer.length;
    }

    /**
     * Determine whether the current peak acceleration seems appropriate (falls within our specified threshold).
     *
     * @return Whether or not the value of acceleration passed to the fxn is acceptable to use as a max value in the user's step.
     */
    private boolean isValidMaxAccelVal(double accelVal) {
        return accelVal > MAX_ACCEL_VAL_LOWER_BOUND && accelVal < MAX_ACCEL_VAL_UPPER_BOUND;
    }

    /**
     * Determine whether the current min "valley" acceleration seems appropriate (falls within our specified threshold).
     *
     * @return Whether or not the value of acceleration passe to the fxn is acceptable to use as a min value in the user's step.
     */
    private boolean isValidMinAccelVal(double accelVal) {
        return accelVal < MIN_ACCEL_VAL_UPPER_BOUND && accelVal > MIN_ACCEL_VAL_LOWER_BOUND;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            //save current accelerometer val
            case Sensor.TYPE_ACCELEROMETER:
                accCurrVal = event.values;

                /*
                if (accFrame % 6 == 0) {
                    Log.i(TAG, "Accelerometer: x " + accCurrVal[0] + ", y " + accCurrVal[1] + ", z " + accCurrVal[2]);
                }*/
                accFrame++;
                break;

            //save current orientation val
            case Sensor.TYPE_ORIENTATION:
                orientCurrVal = event.values;

                /*
                if (orFrame % 6 == 0) {
                    Log.i(TAG, "Orientation: azimuth " + orientCurrVal[0]);
                }*/
                orFrame++;
                break;
        }
    }

    /**
     * Compute new lat,lon using distance travelled and the azimuth angle
     *
     * @param distTravelled   distance
     * @param currXPos   latitude
     * @param currYPos   longitude
     * @param azimuthAngle angle
     * @return string composed of latitude and longitude
     */
    public String computeNewPos(double distTravelled, double currXPos, double currYPos, float azimuthAngle) {
        //transform distance into longitude
        double newYPos = currYPos + (distTravelled * Math.sin(azimuthAngle * RADIANS_PER_DEGREE)) / (DIST_MULTIPLIER * Math.cos(currXPos * RADIANS_PER_DEGREE));

        //transform distance into latitude
        double newXPos = currXPos + (distTravelled * Math.cos(azimuthAngle * RADIANS_PER_DEGREE)) / DIST_MULTIPLIER;

        return newXPos + "," + newYPos;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {   // stop sub thread of inertial location
        super.onDestroy();
        isStopInertialLoc = true;
        sensorManager.unregisterListener(this);
    }
}
