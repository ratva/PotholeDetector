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
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

    Integer MaxArraySize = 20;
    Double myLat = 0d;
    Double myLong = 0d;
    float currentSpeed = 0;
    ArrayDeque<Float> accelData = new ArrayDeque<>();
    ArrayDeque<Long> accelTimestamp = new ArrayDeque<>();


    public static final String LOG_TAG = "Pothole Detector";
    private static final String COMMA_SEPARATOR = ",";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onButtonClick();

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        this.onLocationChanged(null);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);
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
        if (location == null) {
            Speed.setText("-.- m/s");
        } else {
            currentSpeed = location.getSpeed();
            Speed.setText(String.format("%.2f m/s", currentSpeed));
            myLat = location.getLatitude();
            myLong = location.getLongitude();
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

        // Send Y axis data to a Array Deque, and Timestamps. X=[0],Y=[1],Z=[2]
        accelData.add(sensorEvent.values[1]);
        accelTimestamp.add(sensorEvent.timestamp);

        // Use HTML in order to get superscript in TextView
        xText.setText(Html.fromHtml("Vertical Acceleration: " + sensorEvent.values[1] + " ms<sup><small>-2</small></sup>"));

        int accelSize = accelData.size();
        yText.setText("Number of Readings Stored: " + String.valueOf(accelSize));

       /* int triggerElem = 3*accelSize/10;



        Float hello = accelData.element(triggerElem);



       */
        // Output full Array Deque to a scrollview (set up in layout file)
        zText.setText(accelData.toString());

        // Send single (latest) accelerometer reading via the Array Deque (which keeps compiling)
        // zText.setText(accelData.getLast().toString());

        // Removes elements from Array Deque once it reaches max size.
        if (accelData.size() >= MaxArraySize) {
            accelData.removeFirst();
            accelTimestamp.removeFirst();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void writeToCsv(File file) throws IOException {
        // Write and append to a .csv file
        BufferedWriter accelBuffer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));

        // write data
        for (Float d : accelData) {
            accelBuffer.write(d.toString());
            accelBuffer.write(COMMA_SEPARATOR);
        }
        accelBuffer.newLine();

        for (Long t : accelTimestamp) {
            accelBuffer.write(t.toString());
            accelBuffer.write(COMMA_SEPARATOR);
        }
        accelBuffer.newLine();
        accelBuffer.write(myLat.toString());
        accelBuffer.write(COMMA_SEPARATOR);
        accelBuffer.write(myLong.toString());
        accelBuffer.write(COMMA_SEPARATOR);
        accelBuffer.newLine();
        accelBuffer.write(Float.toString(currentSpeed));
        accelBuffer.write(COMMA_SEPARATOR);
        accelBuffer.newLine();

        accelBuffer.close();
    }

    public void deleteCSV(File file) {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.

        if (file != null) {
            file.delete();
        }
    }


    public void onButtonClick() {
        Button SaveButton = (Button) findViewById(R.id.SaveButton);

        SaveButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Save Button Clicked");
                File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

                Toast.makeText(MainActivity.this, "Saved"+ PotholeCSV.toString(), Toast.LENGTH_LONG).show();

                try {
                    writeToCsv(PotholeCSV);
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Failed to write to file.", e);
                }
            }
        });

        Button ClearButton = (Button) findViewById(R.id.ClearButton);

       ClearButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Clear Button Clicked");
                File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

                Toast.makeText(MainActivity.this, "Cleared" + PotholeCSV.toString(), Toast.LENGTH_LONG).show();

                deleteCSV(PotholeCSV);

            }
        });

    }
}
