package weiner.noah.groceryguide;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

//This code is based on code found in https://github.com/ChenHongruixuan/In-outdoorSeamlessPositioningNavigationSystem

//A Service is an application component that can perform long-running operations in the background
public class LocationService extends Service implements SensorEventListener {
    private boolean USE_STEP_APPROXIMATION_IMPLEMENTATION = true;

    private boolean isStopInertialLoc = false; // flag for whether the thread of inertial positioning is stopped
    private float[] accCurrVal = new float[3];    // storing acceleration values returned by sensor
    private float[] orientCurrVal = new float[3];    // storing orientation values returned by sensor

    //buffer of acceleration magnitude vals
    private double[] accelMagnSlidingWindowBuffer = new double[SLIDING_WINDOW_LEN];

    private final Object[] obj = new Object[1];

    private static String TAG = "LocationService";

    // Binder given to clients.
    private final IBinder binder = new LocalBinder();

    //how many radians = 1 deg?
    private static final double RADIANS_PER_DEGREE = Math.PI / 180;

    //the value of the constant k in self defined nonlinear step size algorithm
    private static final double K_CONST = 0.026795089522;

    //private static final double M_L_L_CONST = 111194.926644558;
    private static final double DIST_MULTIPLIER = 1;

    private static final int SLIDING_WINDOW_LEN = 31;

    private static final double MAX_ACCEL_VAL_LOWER_BOUND = 11.8;
    private static final double MAX_ACCEL_VAL_UPPER_BOUND = 20.0;

    private static final double MIN_ACCEL_VAL_LOWER_BOUND = 4.0;
    private static final double MIN_ACCEL_VAL_UPPER_BOUND = 8.8;

    private double xPos, yPos;

    //background Thread on which the service runs
    private Thread locServiceThread;

    //which direction we're walking wrt phone's top edge: 0 is still, -1 is backwards, 1 is fwd
    private int walkingDir = 0;

    private SensorManager sensorManager;
    private int count = 0, stepCount = 0;
    private long startTime, endTime;

    private int accFrame = 0, orFrame = 0;



    //NAIVE IMPLEMENTATION---------------------
    //temporary array to store raw linear accelerometer data before low-pass filter applied
    private final float[] NtempAcc = new float[3];

    //acceleration array for data after filtering
    private final float[] Nacc = new float[3];

    //velocity array (integrated from acceleration values)
    private final float[] Nvelocity = new float[3];

    //position array (integrated from velocity values)
    private final float[] Nposition = new float[3];

    //long to use for keeping track of thyme
    private long timestamp = 0;

    //END NAIVE IMPLEMENTATION--------------------


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "LocationService onCreate()!");

        //get sensor manager
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        //get the sensors themselves and register the listeners to listen for incoming sensor readings
        Sensor accelerometer = Objects.requireNonNull(sensorManager).getDefaultSensor(Sensor.TYPE_ACCELEROMETER); //accelerometer vals are: [0] x accel, [1] y accel, [2] z accel
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //orientation vals are: [0] azimuth (deg of rotation around -z axis)
        Sensor linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, linearAccel, SensorManager.SENSOR_DELAY_GAME);
    }

    public void start(Intent intent) {
        Log.i(TAG, "onStartCommand() called!");

        //create new thread
        locServiceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //initialize the sliding window of acceleration
                initAccSlidingWindow();

                //get the initial client position via the intent
                UserPosition pos = (UserPosition) Objects.requireNonNull(intent).getSerializableExtra("init_pos");

                double stepDist;

                //when phone is at rest, the acceleration magnitude should be about 9.8 (since z accel is calibrated to be 9.8 at rest)
                //z axis points down to ground, -z points up to sky?
                double accelMaxTemp = 9.8, accelMinTemp = 9.8;

                //Log.i(TAG, "xpos is " + (long)pos.getXPos() + ", ypos is " + (long)pos.getYPos());

                synchronized (obj) {
                    //get initial lat and lon
                    xPos = pos.getXPos();
                    yPos = pos.getYPos();
                }

                boolean watchForMinAccelVal = false, watchForMaxAccelVal = true;

                float angle;

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
                        if (!USE_STEP_APPROXIMATION_IMPLEMENTATION) {
                            postNewPosition();
                        }
                        else {
                            //at start of step detection frame, the loop can only catch an acceleration peak (first phase of human step) in the sliding window buffer

                            //check if there's a peak value of acceleration at the center of the window buffer
                            if (watchForMaxAccelVal && accelMagnSlidingWindowBuffer[15] > accelMagnSlidingWindowBuffer[14] &&
                                    accelMagnSlidingWindowBuffer[15] > accelMagnSlidingWindowBuffer[16] &&
                                    isValidMaxAccelVal(accelMagnSlidingWindowBuffer[15]) && //make sure peak value is high enough
                                    accelMaxTemp < accelMagnSlidingWindowBuffer[15]) { //make sure the center of the buffer is bigger than prev stored max value

                                //save the peak accel value
                                accelMaxTemp = accelMagnSlidingWindowBuffer[15];

                                //when peak value of acceleration appears for the first time, start to detect the valley value of acceleration
                                watchForMinAccelVal = true;
                            }

                            //once watchForMinAccelVal is set true above, the loop can now catch both an acceleration peak and/or valley (second phase of human step) in the sliding window buffer
                            //note that the z accel will become low (wrt gravity) when the user starts to bend a knee, since then the device is falling

                            //check if there's a valley (min) value of accel at the center of the window buffer
                            if (watchForMinAccelVal && accelMagnSlidingWindowBuffer[15] < accelMagnSlidingWindowBuffer[14] &&
                                    accelMagnSlidingWindowBuffer[15] < accelMagnSlidingWindowBuffer[16] &&
                                    isValidMinAccelVal(accelMagnSlidingWindowBuffer[15]) && //make sure valley value is in correct rg of vals
                                    accelMinTemp > accelMagnSlidingWindowBuffer[15]) { //make sure center of the buffer is smaller than prev stored min value

                                accelMinTemp = accelMagnSlidingWindowBuffer[15];

                                //when valley value of acceleration appears for the first time, start to detect the effective peak value of acceleration
                                watchForMaxAccelVal = false;
                            }

                            //if a valid valley value of acceleration (heaviest deceleration in human step) has been detected, the acceleration starts to rise,
                            //which means that the previous step has been completed and a new step is started. Wait for a sufficient rise before posting the previous step
                            if (!watchForMaxAccelVal && accelMagnSlidingWindowBuffer[15] >= 9.0) {
                                stepCount++;

                                //get approximate step size of this step (in meters)
                                stepDist = getThisStepDist(accelMinTemp, accelMaxTemp);

                                //get the azimuth orientation, subtracting heading of facing front of SS store
                                angle = orientCurrVal[0] - Constants.SS_WHALLEY_HEADING;

                                //get new lat,lon by adding the step dist
                                computeNewPos(stepDist, angle);

                                //update the user's position
                                pos = new UserPosition(xPos, yPos);

                                Log.i(TAG, "broadcasting new locate pos_data intent!!!");

                                //broadcast new user position via an Intent obj
                                Intent posIntent = new Intent("locate");
                                posIntent.putExtra("pos_data", pos);
                                sendBroadcast(posIntent);

                                //clear the temp acceleration min/max values
                                accelMaxTemp = 9.8;
                                accelMinTemp = 9.8;

                                //start watching for acceleration peak signaling first phase of human step
                                watchForMaxAccelVal = true;

                                //dont allow detection of acceleration valley (the heaviest deceleration), which should be second phase of human step
                                watchForMinAccelVal = false;
                            }
                        }

                    }

                    //otherwise, the user is standing still
                    else {
                        resetVel();
                        walkingDir = 0;

                        synchronized (obj) {
                            xPos = CurrentUserPosition.getCurrXPos();
                            yPos = CurrentUserPosition.getCurrYPos();
                        }
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
    }


    public float getAzimuth() {
        return orientCurrVal[0];
    }

    public float getXAccel() {
        return accCurrVal[0];
    }

    public float getYAccel() {
        return accCurrVal[1];
    }

    public float getZAccel() {
        return accCurrVal[2];
    }

    public float getLinearXAccel() {
        if (!USE_STEP_APPROXIMATION_IMPLEMENTATION) {
            //return raw last linear acceleration val
            return NtempAcc[0];
        }
        else {
            return accCurrVal[0];
        }
    }

    public float getLinearYAccel() {
        if (!USE_STEP_APPROXIMATION_IMPLEMENTATION) {
            //return raw last linear acceleration val
            return NtempAcc[1];
        }
        else {
            return accCurrVal[1];
        }
    }

    public float getLinearZAccel() {
        if (!USE_STEP_APPROXIMATION_IMPLEMENTATION) {
            //return raw last linear acceleration val
            return NtempAcc[1];
        }
        else {
            return accCurrVal[1];
        }
    }

    public float getXVel() {
        return Nvelocity[0];
    }

    public float getYVel() {
        return Nvelocity[1];
    }

    public float getZVel() {
        return Nvelocity[2];
    }


    public void resetPos() {
        Nposition[0] = Nposition[1] = Nposition[2] = 0.0f;

        CurrentUserPosition.setCurrXPos(0);
        CurrentUserPosition.setCurrYPos(0);
        synchronized (obj) {
            xPos = yPos = 0;
        }
    }

    public void setPos(float x, float y) {
        xPos = x;
        yPos = y;

        CurrentUserPosition.setCurrXPos(xPos);
        CurrentUserPosition.setCurrYPos(yPos);
    }

    public void resetVel(){
        Nvelocity[0] = Nvelocity[1] = Nvelocity[2] = 0.0f;
    }
    public void postNewPosition() {
        Log.i(TAG, "postNewPosition(): x is " + Nposition[0] + ", y is " + Nposition[1]);

        //update the user's position
        UserPosition pos = new UserPosition(Nposition[0], Nposition[1]);

        //broadcast new user position via an Intent obj
        Intent posIntent = new Intent("locate");
        posIntent.putExtra("pos_data", pos);
        sendBroadcast(posIntent);
    }

    //a naive and heavily error-prone implementation of NoShake which attempts to calculate the displacement of the phone by integrating the accelerometer data
    public void naivePhysicsImplementation(SensorEvent event) {
        if (timestamp != 0) {
            //get change in time, convert from nanoseconds to seconds
            float dt = (event.timestamp - timestamp) * Constants.NANOSEC_TO_SEC;

            //find friction to be applied using last velocity reading
            float vFrictionToApply = Constants.VELOCITY_FRICTION_DEFAULT * Nvelocity[1];
            Nvelocity[1] += (Nacc[1] * dt) - vFrictionToApply;

            //if resulting value is NaN or infinity, just change it to 0
            Nvelocity[1] = Utils.fixNanOrInfinite(Nvelocity[1]);

            //find position friction to be applied using last position reading
            float pFrictionToApply = Constants.POSITION_FRICTION_DEFAULT * Nposition[1];
            Nposition[1] += (Nvelocity[1] * Constants.VELOCITY_AMPL_DEFAULT * dt) - pFrictionToApply;
        }

        //if timestamp is 0, we just started. init position to 0
        else {
            Nvelocity[0] = Nvelocity[1] = Nvelocity[2] = 0f;
            Nposition[0] = Nposition[1] = Nposition[2] = 0f;

            //fill in the acceleration float array
            Nacc[0] = Utils.rangeValue(event.values[0], -Constants.MAX_ACC, Constants.MAX_ACC);
            Nacc[1] = Utils.rangeValue(event.values[1], -Constants.MAX_ACC, Constants.MAX_ACC);
            Nacc[2] = Utils.rangeValue(event.values[2], -Constants.MAX_ACC, Constants.MAX_ACC);
        }

        //set timestamp to the current time of the sensor reading in nanoseconds
        timestamp = event.timestamp;
    }


    /**
     * The system invokes this method by calling startService() when another component (ie Activity) requests that service be started. When this method executes,
     * service is started and can run in background indefinitely. If you implement this, it's your responsibility to stop service when its work is complete
     * by calling stopSelf() or stopService(). If you only want to provide binding, you don't need to implement this method.
     *
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        start(intent);

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Initialize the parameters of inertial location and pad the sliding window of acceleration.
     */
    private void initAccSlidingWindow() {
        for (int i = 0; i < accelMagnSlidingWindowBuffer.length; i++) {
            //fill entire window with current accel magnitude val
            accelMagnSlidingWindowBuffer[i] = computeAccelMagnitude();

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
        System.arraycopy(accelMagnSlidingWindowBuffer, 1, accelMagnSlidingWindowBuffer, 0, SLIDING_WINDOW_LEN - 1);

        //store new acceleration at end of buffer
        accelMagnSlidingWindowBuffer[SLIDING_WINDOW_LEN - 1] = accelMagnitude;
    }

    /**
     * Judging whether the user is in motion according to the standard deviation of the acceleration sliding window buffer.
     * When the standard deviation of acceleration is greater than 0.8, the user is considered to be in motion since that means the acceleration moved significantly
     * in the past n sensor frames.
     *
     * @return Whether or not user is in motion.
     */
    private boolean isUserInMotion() {
        return getAccStd() >= Constants.IN_MOTION_STD_THRESH;
    }

    /**
     * Obtain the step size of one step using nonlinear step size algorithm
     *
     * @param accMin Valley value of acceleration in a step
     * @param accMax Peak value of acceleration in a step
     * @return step size
     */
    private double getThisStepDist(double accMin, double accMax) {
        double accDiff = accMax - accMin;

        return K_CONST * ((accDiff * 3.5) + Math.pow(accDiff, 0.25));
    }

    /**
     * Method for returning the magnitude of the current acceleration
     *
     * @return Magnitude of acceleration collected by acceleration sensor
     */
    private double computeAccelMagnitude() {
        //Log.i(TAG, "accel magn is " + Math.sqrt(accCurrVal[0] * accCurrVal[0] + accCurrVal[1] * accCurrVal[1] + accCurrVal[2] * accCurrVal[2]));

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
        for (double val : accelMagnSlidingWindowBuffer)
            accVar += (val - accMean) * (val - accMean);

        return accVar / accelMagnSlidingWindowBuffer.length;
    }

    /**
     * Calculate the mean of acceleration in sliding window buffer (last n number of acceleration vals)
     *
     * @return Mean of acceleration in the sliding window buffer.
     */
    private double getAccelMean() {
        double cumSum = 0;

        for (double val : accelMagnSlidingWindowBuffer)
            cumSum += val;

        return cumSum / accelMagnSlidingWindowBuffer.length;
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

                break;

            //save current orientation val
            case Sensor.TYPE_ORIENTATION:
                orientCurrVal = event.values;

                /*
                if (orFrame % 6 == 0) {
                    Log.i(TAG, "Orientation: azimuth " + orientCurrVal[0]);
                }*/

                break;

            //Linear Accelerometer is built-in "virtual sensor" that uses raw data from magnetometer and accelerometer to remove
            //influence of gravity from acceleration vals on all axes
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if (!USE_STEP_APPROXIMATION_IMPLEMENTATION) {
                    //fill the temporary acceleration vector with the current sensor readings
                    for (int i = 0; i < 3; i++) {
                        NtempAcc[i] = Utils.rangeValue(event.values[i], -Constants.MAX_ACC, Constants.MAX_ACC);
                        NtempAcc[i] = Utils.rangeValue(event.values[i], -Constants.MAX_ACC, Constants.MAX_ACC);
                        NtempAcc[i] = Utils.rangeValue(event.values[i], -Constants.MAX_ACC, Constants.MAX_ACC);

                        Nacc[i] = NtempAcc[i];
                    }

                    //apply lowpass filter and store results in acc float arrays
                    //Utils.lowPassFilter(NtempAcc, Nacc, Constants.LOW_PASS_ALPHA);

                    naivePhysicsImplementation(event);
                }

                break;
        }
        orFrame++;
        accFrame++;

        if (orFrame % 8 == 0) {
            Intent accIntent = new Intent("sensors");
            accIntent.putExtra("accel_x", getXAccel());
            accIntent.putExtra("accel_y", getYAccel());
            accIntent.putExtra("accel_z", getZAccel());

            accIntent.putExtra("azim", getAzimuth());

            accIntent.putExtra("vel_x", getXVel());
            accIntent.putExtra("vel_y", getYVel());
            accIntent.putExtra("vel_z", getZVel());

            sendBroadcast(accIntent);
        }
    }

    /**
     * Compute new lat,lon using distance travelled and the azimuth angle
     *
     * @param distTravelled   distance
     * @param azimuthAngle angle
     */
    public void computeNewPos(double distTravelled, float azimuthAngle) {
        //transform distance into longitude
       // double dx = (distTravelled * Math.sin(Math.toRadians(azimuthAngle))) / (DIST_MULTIPLIER * Math.cos(xPos * RADIANS_PER_DEGREE));

        //transform distance into latitude
        //double dy = (distTravelled * Math.cos(Math.toRadians(azimuthAngle))) / DIST_MULTIPLIER;

        //MY VERSION (for x, y position on flat plane)
        double dx = distTravelled * Math.sin(Math.toRadians(azimuthAngle));
        double dy = distTravelled * Math.cos(Math.toRadians(azimuthAngle));

        double newPosX = xPos + dx;
        double newPosY = yPos + dy;

        Log.i(TAG, "computeNewPos(): new posx is " + newPosX + " and new posy is " + newPosY);

        synchronized (obj) {
            xPos = newPosX;
            yPos = newPosY;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        LocationService getService() {
            // Return this instance of LocalService so clients can call public methods.
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        start(intent);
        return binder;
    }

    @Override
    public void onDestroy() {   // stop sub thread of inertial location
        super.onDestroy();
        isStopInertialLoc = true;
        sensorManager.unregisterListener(this);
    }
}
