package com.example.wingoodharry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class GestureTyping extends AppCompatActivity implements CompleteDialog.ExampleDialogListener {

    // Log Tag
    private static final String TAG = "GestureTyping";
    private static final String FILE_NAME = "gameData.txt";
    private static final String DATASET = "dataset.txt";

    // Text Views
    TextView letter, label1, label2, label3;
    TextView confidence1, confidence2, confidence3;
    TextView sentence, timer;

    // Handler
    Handler bluetoothIn;

    // Use to identify handler message
    final int handlerState = 0;

    // Bluetooth
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    // Contains the string received from Bluetooth
    private StringBuilder recDataString = new StringBuilder();

    // Threads
    private GestureTyping.ConnectedThread mConnectedThread;
    private Timer timerThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address;

    // Serves as pointer of the current character the user is typing
    private int counter;

    // Game timer
    private int seconds;

    // Checks if the game is complete
    private boolean isComplete;

    // Sentence to type on
    private String givenSentence;

    // TypeSpeed and Accuracy
    private double speed, accuracy;

    // User's total character input
    private int totalEntries;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("counter", counter);
        outState.putInt("seconds", seconds);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_typing);

        if (savedInstanceState != null) {
            counter = savedInstanceState.getInt("counter");
            seconds = savedInstanceState.getInt("seconds");
        }

        // Link the buttons and textViews to respective views
        timer = findViewById(R.id.timer);
        sentence = findViewById(R.id.sentence);
        letter = findViewById(R.id.text_letter);
        label1 = findViewById(R.id.text_label1);
        label2 = findViewById(R.id.text_label2);
        label3 = findViewById(R.id.text_label3);
        confidence1 = findViewById(R.id.text_confidence1);
        confidence2 = findViewById(R.id.text_confidence2);
        confidence3 = findViewById(R.id.text_confidence3);

        initializeActivity();

        bluetoothIn = new Handler() {
            @SuppressLint("HandlerLeak")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {                        //if message is what we want
                    String readMessage = (String) msg.obj;             // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                 //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");   // determine the end-of-line
                    if (endOfLineIndex > 0) {                          // make sure there data before ~
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
                                                getAssets().open(DATASET), "UTF-8"));

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


                            //======================FOR TYPING GAME=============================//
                            Log.i(TAG, "tag_typingGame");
                            String text = givenSentence;
                            String letter = classifier.GetLabel(0);
                            Log.i(TAG, "letter" + letter);
                            String space = "Space";
                            if(letter.length() > 1){
                                Log.i(TAG, "SPACE");
                                letter = " ";
                            }

                            //Convert Text to List of Characters
                            List<Character> charList = new ArrayList<>();
                            for (char ch : text.toCharArray()) {
                                charList.add(ch);
                            }

                            // This is used for coloring characters
                            SpannableString ss = new SpannableString(text);
                            ForegroundColorSpan fcsRed = new ForegroundColorSpan(Color.RED);
                            ForegroundColorSpan fcsGreen = new ForegroundColorSpan(Color.GREEN);

                            // The users input
                            char input = letter.charAt(0);

                            // Compare the users input to the specific character
                            char currentCharacter = Character.toUpperCase(charList.get(counter));
                            if(input == currentCharacter){
                                // Change the color to green
                                ss.setSpan(fcsGreen, 0, counter + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // Increment counter
                                counter++;
                                //Check if it is the last letter
                                if(counter == charList.size()){
                                    isComplete = true;
                                }
                            } else {
                                if(counter == 0){
                                    // Change the color to red
                                    ss.setSpan(fcsRed, counter, counter + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                } else{
                                    // Change the color of the previous letters to green and current letter to red
                                    ss.setSpan(fcsGreen, 0, counter, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    ss.setSpan(fcsRed, counter, counter + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }

                            totalEntries++;
                            sentence.setText(ss);
                            Log.i(TAG, "tag_handler");

                        }
                        //clear all string data
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    public void initializeActivity(){
        Log.i(TAG, "tag_initActivity");
        // initialize variables
        counter = 0;
        seconds = 0;
        isComplete = false;
        try {
            givenSentence = generateSentence();
        } catch (IOException e) {
            e.printStackTrace();
        }
        totalEntries = 0;

        // create new timer thread
        timerThread = new Timer();
        new Thread(timerThread).start();

        // prepare textviews
        timer.setText("0");
        sentence.setText(givenSentence);
        letter.setText("-");
        label1.setText("-");
        label2.setText("-");
        label3.setText("-");
        confidence1.setText("-");
        confidence2.setText("-");
        confidence3.setText("-");
    }

    private String generateSentence() throws IOException {
        String generatedSentence = "" ;

        // Generate random number from 0 to 5
        Random random = new Random();
        int fileNum = random.nextInt(5);
        String fileName = Integer.toString(fileNum) + ".txt";

        BufferedReader reader = null;
        try {
            String line;
            reader = new BufferedReader(new InputStreamReader(
                            getAssets().open(fileName), "UTF-8"));
            while ((line = reader.readLine()) != null) {
                generatedSentence = line;
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
        return generatedSentence;
    }

    public void save(double speed, double accuracy, String date) {
        FileOutputStream fos = null;
        try {
            // to delete, use MODE_PRIVATE, then change to MODE_APPEND
            fos = openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content =
                String.format("%.2f",speed) + "," + String.format("%.2f",accuracy) + "," + date + "\n";
        byte[] bytesArray = content.getBytes();
        try {
            fos.write(bytesArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openDialog(){
        CompleteDialog completeDialog = new CompleteDialog();
        completeDialog.setSpeed(speed);
        completeDialog.setAccuracy(accuracy);
        completeDialog.show(getSupportFragmentManager(), "complete dialog");
        Log.i(TAG, "tag_openDialog");
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
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

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

        mConnectedThread = new GestureTyping.ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause() {
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
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
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

    private class Timer implements Runnable{
        @Override
        public void run() {
            while(!isComplete){
                try {
                    Thread.sleep(1000);  //1000ms = 1 sec
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            seconds++;
                            timer.setText(String.valueOf(seconds));
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // compute for speed and accuracy
            double wordChar = 5.00;
            double minute = 60.00;
            double wordsNum = (double) counter/wordChar;
            double time = (double) seconds/minute;
            speed = wordsNum/time;

            // compute for accuracy
            double acc = (double) counter/totalEntries;
            accuracy = acc*100;

            Log.i(TAG, "speed = " + speed);
            Log.i(TAG, "accuracy = " + accuracy);
            Log.i(TAG, "counter = " + counter);
            Log.i(TAG, "totalEntries = " + totalEntries);

            // save speed and accuracy
            String currentDate;
            currentDate = new SimpleDateFormat("MM/ dd/ yy", Locale.getDefault()).format(new Date());
            Log.i(TAG, currentDate);
            save(speed,accuracy,currentDate);
            openDialog();
        }
    }
}

