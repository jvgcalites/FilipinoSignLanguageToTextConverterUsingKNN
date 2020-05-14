package com.example.wingoodharry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String FILE_NAME = "dataset.txt";

    // Text Views
    TextView letter, label1, label2, label3, confidence1, confidence2, confidence3;
    TextView flex1, flex2, flex3, flex4, flex5, gyroX, gyroY, gyroZ, accX, accY, accZ;
    TextView txtString, txtStringLength;

    Handler bluetoothIn;
    final int handlerState = 0;                        //used to identify handler message

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket btSocket = null;

    // Contains the string received from Bluetooth
    private StringBuilder recDataString = new StringBuilder();

    // Create Thread
    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;
    public int counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        // Link the buttons and textViews to respective views
        txtString = findViewById(R.id.txtString);
        txtStringLength = findViewById(R.id.testView1);
        flex1 = findViewById(R.id.FlexSensor1);
        flex2 = findViewById(R.id.FlexSensor2);
        flex3 = findViewById(R.id.FlexSensor3);
        flex4 = findViewById(R.id.FlexSensor4);
        flex5 = findViewById(R.id.FlexSensor5);
        gyroX = findViewById(R.id.GyroX);
        gyroY = findViewById(R.id.GyroY);
        gyroZ = findViewById(R.id.GyroZ);
        accX = findViewById(R.id.AccelerometerX);
        accY = findViewById(R.id.AccelerometerY);
        accZ = findViewById(R.id.AcceleromterZ);
        letter = findViewById(R.id.text_letter);
        label1 = findViewById(R.id.text_label1);
        label2 = findViewById(R.id.text_label2);
        label3 = findViewById(R.id.text_label3);
        confidence1 = findViewById(R.id.text_confidence1);
        confidence2 = findViewById(R.id.text_confidence2);
        confidence3 = findViewById(R.id.text_confidence3);

        bluetoothIn = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                                             //if message is what we want
                    String readMessage = (String) msg.obj;                                  // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                        // determine the end-of-line
                    if (endOfLineIndex > 0) {                                               // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data Received = " + dataInPrint);
                        int dataLength = dataInPrint.length();                              //get length of data received
                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                        //if it starts with # we know it is what we are looking for
                        if (recDataString.charAt(0) == '#')
                        {
                            // Parse the packet String into array of characters
                            String packet = recDataString.substring(1, endOfLineIndex);
                            String[] values = packet.split(",");

                            // Store the values in the array into variables
                            String f1 = values[0];
                            String f2 = values[1];
                            String f3 = values[2];
                            String f4 = values[3];
                            String f5 = values[4];
                            String gx = values[5];
                            String gy = values[6];
                            String gz = values[7];
                            String ax = values[8];
                            String ay = values[9];
                            String az = values[10];

                            // Set the textViews and show their values
                            flex1.setText(" F1 = " + f1);
                            flex2.setText(" F2 = " + f2);
                            flex3.setText(" F3 = " + f3);
                            flex4.setText(" F4 = " + f4);
                            flex5.setText(" F5 = " + f5);
                            gyroX.setText(" Gx = " + gx);
                            gyroY.setText(" Gy = " + gy);
                            gyroZ.setText(" Gz = " + gz);
                            accX.setText(" Ax = " + ax);
                            accY.setText(" Ay = " + ay);
                            accZ.setText(" Az = " + az);

                            // Create a classifier object
                            Classifier classifier = new Classifier();

                            // Prepare testFeatures inputs in the object
                            classifier.setFlex1(f1);
                            classifier.setFlex2(f2);
                            classifier.setFlex3(f3);
                            classifier.setFlex4(f4);
                            classifier.setFlex5(f5);
                            classifier.setGyroX(gx);
                            classifier.setGyroY(gy);
                            classifier.setGyroZ(gz);
                            classifier.setAccX(ax);
                            classifier.setAccY(ay);
                            classifier.setAccZ(az);

                            // Prepare the trained features and label values
                            BufferedReader reader = null;
                            try {
                                String line;
                                // Get it from the text file and store it in a List
                                reader = new BufferedReader(
                                        new InputStreamReader(
                                                getAssets().open(FILE_NAME), "UTF-8"));

                                // Do reading, usually loop until end of file reading
                                while ((line = reader.readLine()) != null) {
                                    String[] split = line.split(",");
                                    double[] feature = new double[split.length - 1];

                                    // Store each line in an array
                                    for (int i = 0; i < split.length - 1; i++)
                                        feature[i] = Double.parseDouble(split[i]);

                                    // Add the trained features and labels in the object
                                    classifier.AddTrainedFeatures(feature);
                                    classifier.AddTrainedLabel(split[feature.length]);
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error: " + e);
                            } finally {
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Error: " + e);
                                    }
                                }
                            }

                            // Process the test inputs and trained inputs in the object
                            classifier.Classify();

                            // Get the label and confidence output from the object
                            // Print the text to UI
                            letter.setText(classifier.GetLabel(0));
                            label1.setText("1.    " + classifier.GetLabel(0));
                            label2.setText("2.    " + classifier.GetLabel(1));
                            label3.setText("3.    " + classifier.GetLabel(2));
                            DecimalFormat df2 = new DecimalFormat("#.##");
                            confidence1.setText(df2.format(classifier.GetConfidence(0)) + "%");
                            confidence2.setText(df2.format(classifier.GetConfidence(1)) + "%");
                            confidence3.setText(df2.format(classifier.GetConfidence(2)) + "%");

                            Log.i(TAG, "tag_handler");

                        }
                        //clear all string data
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "tag_onResume");
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivity via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        Log.i(TAG, "tag_onPause");
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        //creates secure outgoing connection with BT device using UUID
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(bluetoothAdapter==null) {
            Toast.makeText(getBaseContext(),
                    "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    Log.i(TAG, "tag_firstThread");
                    //read bytes from input buffer
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    Log.i(TAG, "tag_secondThread");
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            //converts entered String into bytes
            byte[] msgBuffer = input.getBytes();
            try {
                //write bytes over BT connection via outstream
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

