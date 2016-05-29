package gr.uoa.di.giannis.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by Giannis on 28/5/2016.
 */
public class CompassSensor implements SensorEventListener {

    private static final String TAG = "COMPASS_SENSOR";
    private SensorManager sensorManager;
    private android.hardware.Sensor msensor, gsensor;

    private float[] Acceleration = new float[3];
    private float[] Magnetic = new float[3];

    public static float azimuth = 0f;
    private float currectAzimuth = 0f;

    public ImageView compassArrowView = null;

    public CompassSensor(Context context){
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void start(){
        sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) {
                Magnetic[0] = event.values[0];
                Magnetic[1] = event.values[1];
                Magnetic[2] = event.values[2];
            }
            if (event.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) {
                Acceleration[0] = event.values[0];
                Acceleration[1] = event.values[1];
                Acceleration[2] = event.values[2];
            }
            float R[] = new float[9];
            float I[] = new float[9];
            if(SensorManager.getRotationMatrix(R, I, Acceleration, Magnetic)){
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]); // orientation
                azimuth = (azimuth + 360) % 360;
                Log.i(TAG, "Azimuth: " + azimuth);
                updateCompassArrowRotation();
            }
        }
    }



    private void updateCompassArrowRotation() {
        if (compassArrowView == null) {
            Log.i(TAG, "arrow view is not set");
            return;
        }
        Log.i(TAG, "will set rotation from " + currectAzimuth + " to "
                + azimuth);

        Animation an = new RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectAzimuth = azimuth;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        compassArrowView.startAnimation(an);

    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {

    }
}
