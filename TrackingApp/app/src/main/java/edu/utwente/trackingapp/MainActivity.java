package edu.utwente.trackingapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView AccelerometerXText;
    private TextView AccelerometerYText;
    private TextView AccelerometerZText;

    private TextView GyroscopeXText;
    private TextView GyroscopeYText;
    private TextView GyroscopeZText;
    private TextView SocketStatusText;

    private Spinner DropdownActivity;
    private Button ToggleRecordButton;

    private boolean isRecording = false;
    private String currentActivity;
    long tStart = System.currentTimeMillis();
    long tEnd = System.currentTimeMillis();

    private static final DecimalFormat df = new DecimalFormat("0.0000");

    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    // Storage for Sensor readings
    private float[] mAcceleration = null;
    private float[] mGyro = null;

    SensorEntryData currentSensorEntryData;
    ArrayList<SensorEntryData> recordedSensorEntryData;

    // data settings
    private final int DATA_SIZE = 64;

    // server settings
    private final String SERVER_ADDRESS = "10.53.85.94"; // make sure this matches whatever the server tells you
    private final int SERVER_PORT = 8776;

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

        SocketStatusText = findViewById(R.id.socketStatus);
        // start socket operations on a non-UI thread
        new Thread(socketThread).start();

        AccelerometerXText = findViewById(R.id.accelerometerX);
        AccelerometerYText = findViewById(R.id.accelerometerY);
        AccelerometerZText = findViewById(R.id.accelerometerZ);

        GyroscopeXText = findViewById(R.id.gyroscopeX);
        GyroscopeYText = findViewById(R.id.gyroscopeY);
        GyroscopeZText = findViewById(R.id.gyroscopeZ);

        DropdownActivity = findViewById(R.id.activityChooser);

        String[] activities =
                new String[]{"Sitting", "Standing", "Walking", "Medication", "BrushingTeeth", "Eating", "Laying"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, activities);
        DropdownActivity.setAdapter(adapter);
        DropdownActivity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        currentActivity = "Sitting";
                        break;
                    case 1:
                        currentActivity = "Standing";
                        break;
                    case 2:
                        currentActivity = "Walking";
                        break;
                    case 3:
                        currentActivity = "Medication";
                        break;
                    case 4:
                        currentActivity = "BrushingTeeth";
                        break;
                    case 5:
                        currentActivity="Eating";
                        break;
                    case 6:
                        currentActivity="Laying";
                        break;
                }
                System.out.println("Activity changed to: " + currentActivity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        ToggleRecordButton = (Button)findViewById(R.id.recordToggle);
        ToggleRecordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (!isRecording) {
                    ToggleRecordButton.setText("Stop");
                    tStart = System.currentTimeMillis();
                } else {
                    ToggleRecordButton.setText("Record");
                }
                isRecording = !isRecording;
            }
        });

        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //  Accelerometer
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

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
                SocketStatusText.setText("Trying to connect...");

                clientSocket = new Socket(InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            } catch (IOException e) {
                SocketStatusText.setText("Error. Cannot establish a connection.");
                e.printStackTrace();
            }

            if (clientSocket != null) {
                SocketStatusText.setText("Socket established.");
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

                        String strPacket = currentSensorEntryData.toString();
                        tEnd = System.currentTimeMillis();

                        if (isRecording) {
                            long tDelta = tEnd - tStart;
                            int elapsedSeconds = (int) (tDelta / 1000.0);

                            strPacket =  String.valueOf(tEnd) + "," + elapsedSeconds + "," + strPacket + "," + currentActivity;
                        } else {
                            strPacket = String.valueOf(tEnd) + "," + 0 + "," + strPacket + ",Null";
                        }

                        System.out.println("Sending: " + strPacket);

                        byte[] packet = strPacket.getBytes();
                        try {
                            clientSocketOut.write(packet);
                            clientSocketOut.flush();
                            Thread.sleep(100);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            //This should crash the app "Intended feature"
                            SocketStatusText.setText("Sending data error. Connection problems.");
                        }
                    }
                }
            }
        }
    };


    public void onSensorChanged(SensorEvent event) {
        // we received a sensor event. it is a good practice to check
        // that we received the proper event

        String strX;
        String strY;
        String strZ;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            strX = df.format(event.values[0]);
            strY = df.format(event.values[1]);
            strZ = df.format(event.values[2]);

            //Acceleration force along the x axis (including gravity).
            AccelerometerXText.setText(strX);
            //Acceleration force along the y axis (including gravity).
            AccelerometerYText.setText(strY);
            //Acceleration force along the z axis (including gravity).
            AccelerometerZText.setText(strZ);

            mAcceleration = new float[3];
            System.arraycopy(event.values, 0, mAcceleration, 0, 3);
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            strX = df.format(event.values[0]);
            strY = df.format(event.values[1]);
            strZ = df.format(event.values[2]);

            //Rate of rotation around the x axis.
            GyroscopeXText.setText(strX);
            //Rate of rotation around the y axis.
            GyroscopeYText.setText(strY);
            //Rate of rotation around the z axis.
            GyroscopeZText.setText(strZ);

            mGyro = new float[3];
            System.arraycopy(event.values, 0, mGyro, 0, 3);
        }

        if (mAcceleration != null && mGyro != null) {

            currentSensorEntryData = new SensorEntryData();

            currentSensorEntryData.setAx(round(mAcceleration[0], 6));
            currentSensorEntryData.setAy(round(mAcceleration[1], 6));
            currentSensorEntryData.setAz(round(mAcceleration[2], 6));

            currentSensorEntryData.setGx(round(mGyro[0], 6));
            currentSensorEntryData.setGy(round(mGyro[1], 6));
            currentSensorEntryData.setGz(round(mGyro[2], 6));

            //System.out.println(currentSensorEntryData.toString());

            //// Reset sensor event data arrays
            mAcceleration = mGyro = null;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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