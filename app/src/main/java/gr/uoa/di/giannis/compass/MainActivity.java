package gr.uoa.di.giannis.compass;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Intent locationIntent;
    private static final String TAG = "MAIN_ACTIVITY";
    private MainActivity context;
    private boolean navigationON = false;

    private CompassSensor compassSensor;

    TextView tvLatitude;
    TextView tvLongitude;
    Button startButton;
    public ImageView destinationArrowView = null;

    double latitudeDest, longitudeDest, latitude, longitude;
    Location destLoc;

    private float bearing = 0f;
    private float currectBearing = 0f;

    // Broadcast receiver to get messages from LocationService
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            updateUI(intent);
        }
    };

    private void updateUI(Intent intent) {
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);
        Log.e(TAG, "latitude" + latitude + " - longitude" + longitude);
        if (navigationON) {
            Location curLoc = new Location("");
            curLoc.setLatitude(latitude);
            curLoc.setLongitude(longitude);
            bearing = curLoc.bearingTo(destLoc);
            bearing = (bearing + 360) % 360;
            Log.i(TAG, "Bearing:" + bearing);
            updateDestinationArrowRotation();
        }
    }

    private void updateDestinationArrowRotation() {
        if (destinationArrowView == null) {
            Log.i(TAG, "arrow view is not set");
            return;
        }
        // Calculating destination direction in relevance to north
        bearing = CompassSensor.azimuth - bearing;
        Log.i(TAG, "will set rotation from " + currectBearing + " to "
                + bearing);

        Animation an = new RotateAnimation(-currectBearing, -bearing,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        currectBearing = bearing;

        an.setDuration(500);
        an.setRepeatCount(0);
        an.setFillAfter(true);

        destinationArrowView.startAnimation(an);
    }

    public void startNavigation(View view) {
        Log.i(TAG, "startButton pressed");

        if (!navigationON) {
            if (tvLatitude.getText().toString().matches("") || tvLongitude.getText().toString().matches("")) {
                Log.e(TAG, "No destination coordinates given");
                Toast.makeText(context, "Please give destination coordinates", Toast.LENGTH_LONG).show();
                return;
            }
            latitudeDest = Float.valueOf(tvLatitude.getText().toString());
            longitudeDest = Float.valueOf(tvLongitude.getText().toString());
            navigationON = true;
            destLoc = new Location("");
            destLoc.setLatitude(latitudeDest);
            destLoc.setLongitude(longitudeDest);
            Log.i(TAG, "Starting navigation to latitude: " + latitudeDest + ", longitude: " + longitudeDest);
            Toast.makeText(context, "Starting navigation to latitude: " + latitudeDest + ", longitude: " + longitudeDest, Toast.LENGTH_LONG).show();
            startButton.setText("Stop");


        } else {
            navigationON = false;
            Log.i(TAG, "stopping navigation");
            Toast.makeText(context, "Navigation stopped", Toast.LENGTH_LONG).show();
            startButton.setText("Start");
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e(TAG, "onCreate");

        context = this;

        compassSensor = new CompassSensor(context);
        compassSensor.compassArrowView = (ImageView) findViewById(R.id.compass_arrow_image);
        destinationArrowView = (ImageView) findViewById(R.id.destination_arrow_image);
        compassSensor.start();

        tvLatitude = (TextView) findViewById(R.id.latitudeText);
        tvLongitude = (TextView) findViewById(R.id.longitudeText);
        startButton = (Button) findViewById(R.id.startButton);

        locationIntent = new Intent(MainActivity.this, LocationService.class);
        startService(locationIntent);

        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.BROADCAST_LOCATION_DATA));
    }


    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        stopService(locationIntent);
        unregisterReceiver(broadcastReceiver);
        compassSensor.stop();
        super.onDestroy();
    }
}
