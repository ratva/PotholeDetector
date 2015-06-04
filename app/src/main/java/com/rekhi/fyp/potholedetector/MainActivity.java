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
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {


    Sensor accelerometer;
    SensorManager sensorManager;
    TextView xText;
    TextView yText;
    TextView zText;
    public static EditText TriggerInput;

    int MaxArraySize = 500;
    Double myLat = 0d;
    Double myLong = 0d;
    float currentSpeed = 0;
    float compValue = 0f;
    float triggerOrig = 10f;
    float triggerAccel = triggerOrig;
    ArrayList<Float> accelData = new ArrayList<>();
    ArrayList<Long> accelTimestamp = new ArrayList<>();
    ArrayList<Float> SpeedArray = new ArrayList<>();


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
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        xText = (TextView) findViewById(R.id.xText);
        yText = (TextView) findViewById(R.id.yText);
        zText = (TextView) findViewById(R.id.zText);
        TriggerInput = (EditText) findViewById(R.id.TriggerInput);
    }

    /*
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
*/
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

        // Send Y axis data to a Array List, and Timestamps. X=[0],Y=[1],Z=[2]
        accelData.add(sensorEvent.values[1]);
        accelTimestamp.add(sensorEvent.timestamp);
        SpeedArray.add(currentSpeed);

        // Use HTML in order to get superscript in TextView
        // Show latest accelerometer reading
        xText.setText(Html.fromHtml("Vertical Acceleration: " + sensorEvent.values[1] + " ms<sup><small>-2</small></sup>"));

        // Track a value (CompareElem) to compare to the trigger quantity
        int accelSize = accelData.size();
        int compElem = 3 * MaxArraySize / 10;
        if (accelSize > compElem + 1) {
            compValue = accelData.get(compElem);
        }
        else {
            compValue = 0f;
        }

        yText.setText("Number of Readings Stored: " + String.valueOf(accelSize));


        // Output full Array List to a scrollview (set up in layout file)
        zText.setText(accelData.toString());

        // Send single (latest) accelerometer reading via the Array List (which keeps compiling)
        // zText.setText(accelData.getLast().toString());

        // Removes elements from Array List once it reaches max size.
        if (accelData.size() >= MaxArraySize) {
            accelData.remove(0);
            accelTimestamp.remove(0);
            SpeedArray.remove(0);
        }

        // Check for trigger
        if (Math.abs(compValue) >= triggerAccel) {
            saveCSV();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void saveCSV() {
        File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

        Toast.makeText(MainActivity.this, "Saved " + PotholeCSV.toString(), Toast.LENGTH_SHORT).show();

        try {
            writeToCsv(PotholeCSV);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to write to file.", e);
        }
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

        for (Float s : SpeedArray) {
            accelBuffer.write(s.toString());
            accelBuffer.write(COMMA_SEPARATOR);
        }
        accelBuffer.newLine();


        // Clear Array Lists so that duplicate readings aren't recorded
        accelData.clear();
        accelTimestamp.clear();
        SpeedArray.clear();
        // To ensure that array can repopulate again.
        accelData.trimToSize();

        accelBuffer.write(myLat.toString());
        accelBuffer.write(COMMA_SEPARATOR);
        accelBuffer.write(myLong.toString());
        accelBuffer.write(COMMA_SEPARATOR);
      /*  accelBuffer.newLine();
        accelBuffer.write(Float.toString(currentSpeed));
        accelBuffer.write(COMMA_SEPARATOR);
      */
        accelBuffer.newLine();
        accelBuffer.newLine();

        accelBuffer.close();
    }

    public void deleteCSV(File file) {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.

        if (file != null) {
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(MainActivity.this, "Cleared " + file.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void emailCSV() {
        File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent.setType("vnd.android.cursor.dir/email");
        String to[] = {"avtarrekhi@gmail.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + PotholeCSV.getAbsolutePath()));
        // the mail subject
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Pothole Readings");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }

    public void onButtonClick() {
        Button SaveButton = (Button) findViewById(R.id.SaveButton);

        SaveButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Save Button Clicked");
                saveCSV();
            }
        });

        Button ClearButton = (Button) findViewById(R.id.ClearButton);

        ClearButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Clear Button Clicked");
                File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

                // Check if there is a file to clear
                if(PotholeCSV.exists()){
                    deleteCSV(PotholeCSV);
                }
                else{
                    Log.i(LOG_TAG, "No file to clear");
                    Toast.makeText(MainActivity.this, "There is already no file", Toast.LENGTH_LONG).show();
                }




            }
        });

        Button EmailButton = (Button) findViewById(R.id.EmailButton);

        EmailButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Email Button Clicked");

                File PotholeCSV = new File(getExternalFilesDir(null), "Potholes.csv");

                // Check if there is a file to email
                if(PotholeCSV.exists()){
                    emailCSV();
                }
                else{
                    Log.i(LOG_TAG, "No file to send");
                    Toast.makeText(MainActivity.this, "There is no data file to email", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button TriggerButton = (Button) findViewById(R.id.TriggerButton);

        TriggerButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Trigger Button Clicked");

                String strTriggerInput = TriggerInput.getText().toString();

                if(TextUtils.isEmpty(strTriggerInput)) {
                    TriggerInput.setError("Enter a value greater than 1.0");
                }
                else{

                    float inputTrigger = Float.valueOf(strTriggerInput);
                    if(inputTrigger>=1f){
                        triggerAccel = inputTrigger;
                        Toast.makeText(MainActivity.this, "Trigger changed to " + Float.toString(triggerAccel), Toast.LENGTH_SHORT).show();
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);

                        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    else{
                        TriggerInput.setError("Enter a value greater than 1.0");
                        Toast.makeText(MainActivity.this, "Trigger must be greater than 1.0", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        Button ResetButton = (Button) findViewById(R.id.ResetButton);

        ResetButton.setOnClickListener(new View.OnClickListener() {
            // Called as soon as the button is clicked
            @Override
            public void onClick(View v) {
                Log.i(LOG_TAG, "Reset Button Clicked");
                triggerAccel = triggerOrig;

                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                Toast.makeText(MainActivity.this, "Trigger changed to " + Float.toString(triggerOrig), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
