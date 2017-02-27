package com.example.komal.blue;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity{
    //GUI components
    private EditText messageText, phoneText;
    public TextView accelerometerValues;
    private Button sendButton, locationButton;

    //main activity variables
    private String fileName = "defaultPhone.txt";
    private String defaultPhone = "";
    private String locationMessage = "";
    int permission = 3;
    double latitude, longitude;

    //shake detection
    private Context shakeContext;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 20.25f;
    private static final int SHAKE_INTERVAL = 1000;
    private long lastShakeTime, currentTime;
    private float x, y, z;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //GUI findViewById
        messageText = (EditText) findViewById(R.id.messageText);
        phoneText = (EditText) findViewById(R.id.phoneText);
        accelerometerValues = (TextView) findViewById(R.id.accelerometerValues);
        sendButton = (Button) findViewById(R.id.sendButton);
        locationButton = (Button) findViewById(R.id.locationButton);

        //create object for finding location
        final FindLocation findLocation;
        findLocation = new FindLocation(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS}, permission);
            return;
        }

        findLocation.getLocation();
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLocation(findLocation);
            }
        });

    //    ShakeListener shakeListener = new ShakeListener(this);

        //sensor listener to detect shake
        SensorEventListener accelerometerListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                //display values
                setValues(x, y, z);

                currentTime = System.currentTimeMillis();
                if((currentTime - lastShakeTime) > SHAKE_INTERVAL){
                    double acceleration = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;
                    Log.d("Acceleration ", Double.toString(acceleration));
                    if(acceleration > SHAKE_THRESHOLD){
                        lastShakeTime = currentTime;
                        setLocation(findLocation);
                        sendSMS();
                        Log.d("Shook", "Shake");
                        Toast.makeText(getApplicationContext(), "Shake!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(accelerometerListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);


        //read file for previously used phone number
        try{
            InputStream in = openFileInput(fileName);
            if(in != null){
                //found some default phone number
                InputStreamReader inReader = new InputStreamReader(in);
                BufferedReader bufReader = new BufferedReader(inReader);
                String temp;
                if((temp = bufReader.readLine()) != null){
                    //set as default phone number
                    defaultPhone = temp;
                }
                bufReader.close();
                inReader.close();
            }
            in.close();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error opening file", Toast.LENGTH_SHORT).show();
        }
        phoneText.setText(defaultPhone);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS();
            }
        });


    }


    //set location in message field
    private void setLocation(FindLocation findLocation){
        if(findLocation.isCanGetLocation()){
            latitude = findLocation.getLatitude();
            longitude = findLocation.getLongitude();
            locationMessage = "Location: " + latitude + ", " + longitude + " ";
            messageText.setText(locationMessage);
        }else{
            messageText.setText("Location not available");
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            findLocation.getLocation();
        }
    }


    //send text in message field to phone number entered
    private void sendSMS(){
        String phone = phoneText.getText().toString();
        String message = messageText.getText().toString();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_SHORT).show();
            updateDefaultPhone(phone);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error sending message!", Toast.LENGTH_SHORT).show();
        }
        messageText.setText("");

    }


    //update file with new phone number if used
    private void updateDefaultPhone(String newDefaultPhone) {
        if(defaultPhone.compareTo(newDefaultPhone) == 0){
            //default phone was used..no need to update
            return;
        }
        try {
            OutputStreamWriter outWriter = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
            outWriter.write(newDefaultPhone);
            outWriter.close();
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error writing to file", Toast.LENGTH_SHORT).show();
        }
    }

    //request user for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 3:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                }
                return;
        }
    }


    //set the values to accelerometer text
    public void setValues(float x, float y, float z){
        accelerometerValues.setText("X: " + x + "\nY: " + y + "\nZ: " + z);
    }

}
