package edu.utwente.trackingapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView AccelerometerXText;
    private TextView AccelerometerYText;
    private TextView AccelerometerZText;

    private TextView GyroscopeXText;
    private TextView GyroscopeYText;
    private TextView GyroscopeZText;

    private TextView MagnetometerXText;
    private TextView MagnetometerYText;
    private TextView MagnetometerZText;

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor gyroscope;

    // Storage for Sensor readings
    private float[] mMagnetic = null;
    private float[] mAcceleration = null;
    private float[] mGyro = null;

    SensorEntryData currentSensorEntryData;

    Context context = this;

    // data settings
    private final int DATA_SIZE = 64;

    // server settings
    private final String SERVER_ADDRESS = "192.168.1.76"; // make sure this matches whatever the server tells you
    private final int SERVER_PORT = 8888;

    // client settings
    private Socket clientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
            try {
                clientSocket.close();
                System.out.println("The server is shut down!");
            } catch (IOException e) { /* failed */ }
        }});
        // start socket operations on a non-UI thread
        new Thread(socketThread).start();


        AccelerometerXText = findViewById(R.id.accelerometerX);
        AccelerometerYText = findViewById(R.id.accelerometerY);
        AccelerometerZText = findViewById(R.id.accelerometerZ);

        GyroscopeXText = findViewById(R.id.gyroscopeX);
        GyroscopeYText = findViewById(R.id.gyroscopeY);
        GyroscopeZText = findViewById(R.id.gyroscopeZ);

        MagnetometerXText = findViewById(R.id.magnetometerX);
        MagnetometerYText = findViewById(R.id.magnetometerY);
        MagnetometerZText = findViewById(R.id.magnetometerZ);

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //  Accelerometer
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        //  Magnetometer
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(MainActivity.this, magnetometer, SensorManager.SENSOR_DELAY_GAME);

        // Gyroscope
        gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(MainActivity.this, gyroscope, SensorManager.SENSOR_DELAY_GAME);

    }

    private Runnable socketThread = new Runnable() {
        @Override
        public void run() {
            // configure client socket
            try {
                System.out.println("Trying to connect...");
                clientSocket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (clientSocket != null) {
                System.out.println("Socket established...");
                // grab output stream of client socket
                OutputStream clientSocketOut = null;
                try {
                    clientSocketOut = clientSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // send data over the client socket
                while (clientSocketOut != null && clientSocket.isConnected()) {
                    if (currentSensorEntryData != null) {
                        System.out.println("Sending data" + currentSensorEntryData.toString());
                        byte[] packet = currentSensorEntryData.toString().getBytes();
                        try {
                            clientSocketOut.write(packet);
                            clientSocketOut.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };


    public void onSensorChanged(SensorEvent event) {
        // we received a sensor event. it is a good practice to check
        // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Acceleration force along the x axis (including gravity).
            AccelerometerXText.setText(String.valueOf(event.values[0]));
            //Acceleration force along the y axis (including gravity).
            AccelerometerYText.setText(String.valueOf(event.values[1]));
            //Acceleration force along the z axis (including gravity).
            AccelerometerZText.setText(String.valueOf(event.values[2]));

            mAcceleration = new float[3];
            System.arraycopy(event.values, 0, mAcceleration, 0, 3);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Rate of rotation around the x axis.
            GyroscopeXText.setText(String.valueOf(event.values[0]));
            //Rate of rotation around the y axis.
            GyroscopeYText.setText(String.valueOf(event.values[1]));
            //Rate of rotation around the z axis.
            GyroscopeZText.setText(String.valueOf(event.values[2]));

            mGyro = new float[3];
            System.arraycopy(event.values, 0, mGyro, 0, 3);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            MagnetometerXText.setText(String.valueOf(event.values[0]));
            MagnetometerYText.setText(String.valueOf(event.values[1]));
            MagnetometerZText.setText(String.valueOf(event.values[2]));

            mMagnetic = new float[3];
            System.arraycopy(event.values, 0, mMagnetic, 0, 3);
        }


        if (mAcceleration != null && mGyro != null && mMagnetic != null) {

            currentSensorEntryData = new SensorEntryData();

            currentSensorEntryData.setAx(mAcceleration[0]);
            currentSensorEntryData.setAy(mAcceleration[1]);
            currentSensorEntryData.setAz(mAcceleration[2]);

            currentSensorEntryData.setGx(mGyro[0]);
            currentSensorEntryData.setGy(mGyro[1]);
            currentSensorEntryData.setGz(mGyro[2]);

            currentSensorEntryData.setMx(mMagnetic[0]);
            currentSensorEntryData.setMy(mMagnetic[1]);
            currentSensorEntryData.setMz(mMagnetic[2]);

            //System.out.println(currentSensorEntryData.toString());

            //// Reset sensor event data arrays
            mAcceleration = mGyro = mMagnetic = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}