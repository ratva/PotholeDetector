package com.rekhi.fyp.potholedetector;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayDeque;


public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {


    // android.content.Intent.ACTION_SENDTO (new Intent(Intent.ACTION_SENDTO);)
    // intent.putExtra(Intent.EXTRA_EMAIL, "emailaddress@emailaddress.com");
    // intent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
    // intent.putExtra(Intent.EXTRA_TEXT, "I'm email body.");

    Sensor accelerometer;
    SensorManager sensorManager;
    TextView xText;
    TextView yText;
    TextView zText;

    ArrayDeque accelData = new ArrayDeque();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.onLocationChanged(null);

        sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer=sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        xText=(TextView)findViewById(R.id.xText);
        yText=(TextView)findViewById(R.id.yText);
        zText=(TextView)findViewById(R.id.zText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView Speed = (TextView) this.findViewById(R.id.Speed);
        if(location==null){
            Speed.setText("-.- m/s");
        }
        else{
            float CurrentSpeed = location.getSpeed();
            Speed.setText( String.format("%.2f m/s", CurrentSpeed) );
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // Send Z axis data to a Array Deque
        accelData.add(sensorEvent.values[2]);

        int accelSize = accelData.size();
        // Use HTML in order to get superscript in TextView
        //xText.setText(Html.fromHtml(sensorEvent.values[0]+" ms<sup><small>-2</small></sup>"));
        xText.setText(String.valueOf(accelSize));
        yText.setText(Html.fromHtml(sensorEvent.values[1]+" ms<sup><small>-2</small></sup>"));

        // Output full Array Deque to a scrollview (set up in layout file)
        zText.setText(accelData.toString());
        // Send single (latest) accelerometer reading via the Array Deque (which keeps compiling)
        // zText.setText(accelData.getLast().toString());

        if (accelData.size() > 20) {
            accelData.remove(1);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
