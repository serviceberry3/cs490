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
    private double[] accSlidingWindow = new double[SLIDING_WINDOW_LEN];

    private static String TAG = "LocationService";

    //how many radians = 1 deg?
    private static final double RADIANS_PER_DEGREE = Math.PI / 180;

    //the value of k in self defined nonlinear step size algorithm
    private static final double kConst = 0.026795089522;

    private static final double M_L_L_CONST = 111194.926644558;

    private static final int SLIDING_WINDOW_LEN = 31;

    //background Thread on which the service runs
    private Thread locServiceThread;

    private SensorManager sensorManager;
    private int count = 0, stepCount = 0;
    private long startTime, endTime;

    @Override
    public void onCreate() {
        super.onCreate();

        //get sensor manager
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        //get the sensors themselves and register the listeners to listen for incoming sensor readings
        Sensor accelerometer = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //accelerometer vals are: [0] x accel, [1] y accel, [2] z accel
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //orientation vals are: [0] azimuth (deg of rotation around -z axis)

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
        //create new thread
        locServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //initialize the sliding window of acceleration
                initAccSlidingWindow();

                //get the initial client position via the intent
                UserPosition pos = (UserPosition) Objects.requireNonNull(intent).getSerializableExtra("init_pos");

                double step, accTempMax = 9.8, accTempMin = 9.8;

                //get initial lat and lon
                double posLat = pos.getLat(), posLon = pos.getLon();

                boolean isStartCheckMinValue = false, isStartCheckMaxValue = true;

                //get current time
                startTime = System.currentTimeMillis();

                //while service should still be running
                while (!isStopInertialLoc) {
                    count++;

                    //throw out oldest accel val in the buffer (head) and store the newest one at tail
                    moveAccWindow(computeAccelMagnitude());

                    //if the user is deemed to be in motion
                    if (isWalk()) {
                        //check if there's a peak value of acceleration in one step
                        if (isStartCheckMaxValue && accSlidingWindow[15] > accSlidingWindow[14] &&
                                accSlidingWindow[15] > accSlidingWindow[16] &&
                                isEffectiveMaxValue(accSlidingWindow[15]) &&
                                accTempMax < accSlidingWindow[15]) {

                            //save the peak accel value
                            accTempMax = accSlidingWindow[15];

                            //when peak value of acceleration appears for the first time, start to detect the valley value of acceleration
                            isStartCheckMinValue = true;
                        }

                        //check if there's a valley (min) value of accel in one step
                        if (isStartCheckMinValue && accSlidingWindow[15] < accSlidingWindow[14] &&
                                accSlidingWindow[15] < accSlidingWindow[16] &&
                                isEffectiveMinValue(accSlidingWindow[15]) &&
                                accTempMin > accSlidingWindow[15]) {

                            accTempMin = accSlidingWindow[15];

                            //when valley value of acceleration appears for the first time, start to detect the effective peak value of acceleration
                            isStartCheckMaxValue = false;
                        }

                        // When the valley value of acceleration has been detected, the acceleration starts to rise, which means that the previous step has been completed and a new step is started
                        if (!isStartCheckMaxValue && accSlidingWindow[15] >= 9.0) {
                            stepCount++;

                            //get step size of this step
                            step = getThisStepDist(accTempMin, accTempMax);

                            //get the azimuth orientation
                            float angle = orientCurrVal[0];

                            //get distance to
                            String[] llStr = disToLL(step, posLat, posLon, angle).split(",");
                            posLat = Double.parseDouble(llStr[0]);
                            posLon = Double.parseDouble(llStr[1]);

                            //update the user's position
                            pos = new UserPosition(posLat, posLon, NowClientPos.getNowFloor());

                            //broadcast new user position via an Intent obj
                            Intent posIntent = new Intent("locate");
                            posIntent.putExtra("pos_data", pos);
                            sendBroadcast(posIntent);

                            //init params
                            accTempMax = 9.8;
                            accTempMin = 9.8;
                            isStartCheckMaxValue = true;
                            isStartCheckMinValue = false;
                        }
                    }

                    //otherwise, the user is standing still
                    else {
                        posLat = NowClientPos.getNowLatitude();
                        posLon = NowClientPos.getNowLongitude();
                    }


                    try {
                        //this sets the "sample rate"
                        Thread.sleep(20);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Inertial locate Wait Error:" + e.getMessage());
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
        for (int i = 0; i < accSlidingWindow.length; i++) {
            //fill entire window with current accel magnitude val
            accSlidingWindow[i] = computeAccelMagnitude();

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
        System.arraycopy(accSlidingWindow, 1, accSlidingWindow, 0, SLIDING_WINDOW_LEN - 1);

        //store new acceleration at end of buffer
        accSlidingWindow[SLIDING_WINDOW_LEN - 1] = accelMagnitude;
    }

    /**
     * Judging whether the user is in motion according to the standard deviation of acceleration
     * When the standard deviation of acceleration is greater than 0.8, the user is considered to be in motion
     *
     * @return If the user is in motion
     */
    private boolean isWalk() {
        return getAccStd() >= 0.8;
    }

    /**
     * Obtain the step size of one step by self defined nonlinear step size algorithm
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
     * Method for calculating the standard deviation of acceleration in sliding window
     *
     * @return Standard deviation of acceleration in the sliding window
     */
    private double getAccStd() {
        return Math.sqrt(getAccVar());
    }


    /**
     * Method for calculating the variance of acceleration in sliding window
     *
     * @return Variance of acceleration in the sliding window
     */
    private double getAccVar() {
        double accVar = 0;
        double accMean = getAccMean();
        for (double anAccSlidingWindow : this.accSlidingWindow)
            accVar += (anAccSlidingWindow - accMean) * (anAccSlidingWindow - accMean);
        return accVar / accSlidingWindow.length;
    }

    /**
     * Method for calculating the mean of acceleration in sliding window
     *
     * @return Mean of acceleration in the sliding window
     */
    private double getAccMean() {
        double sumAcc = 0;
        for (double anAccSlidingWindow : this.accSlidingWindow)
            sumAcc += anAccSlidingWindow;
        return sumAcc / accSlidingWindow.length;
    }

    /**
     * Judging whether the current peak acceleration exceeds the threshold value and become an effective peak acceleration
     *
     * @return whether becoming an effective peak acceleration
     */
    private boolean isEffectiveMaxValue(double acc) {
        return acc > 11.8 && acc < 20.0;
    }

    /**
     * Judging whether the current valley acceleration exceeds the threshold value and become an effective peak acceleration
     *
     * @return whether becoming an effective valley acceleration
     */
    private boolean isEffectiveMinValue(double acc) {
        return acc < 8.8 && acc > 4.0;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        switch (type) {
            //save current accelerometer val
            case Sensor.TYPE_ACCELEROMETER:
                accCurrVal = event.values;
                break;

            //save current orientation val
            case Sensor.TYPE_ORIENTATION:
                orientCurrVal = event.values;
                break;
        }
    }

    /**
     * Compute new lat,lon using distance travelled and the azimuth angle
     *
     * @param dis   distance
     * @param lat   latitude
     * @param lon   longitude
     * @param angle angle
     * @return string composed of latitude and longitude
     */
    public String disToLL(double dis, double lat, double lon, float angle) {
        // transform distance into longitude
        double newLon = lon + (dis * Math.sin(angle * RADIANS_PER_DEGREE)) / (M_L_L_CONST * Math.cos(lat * RADIANS_PER_DEGREE));

        // transform distance into latitude
        double newLat = lat + (dis * Math.cos(angle * RADIANS_PER_DEGREE)) / M_L_L_CONST;
        return newLat + "," + newLon;
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
