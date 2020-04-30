package com.example.wingoodharry;

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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class GestureTyping extends AppCompatActivity implements CompleteDialog.ExampleDialogListener {

    // Log Tag
    private static final String TAG = "GestureTyping";
    private static final String FILE_NAME = "gameData.txt";

    // Text Views
    TextView letter, label1, label2, label3;
    TextView confidence1, confidence2, confidence3;
    TextView sentence, timer;

    Handler bluetoothIn;
    final int handlerState = 0;                        //used to identify handler message

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

    private int counter;
    private int seconds;
    private boolean isComplete;
    private String givenSentence;
    private double speed, accuracy;
    private int totalEntries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_typing);

        Log.i(TAG, "tag_onCreate");
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
                if (msg.what == handlerState) {                                             //if message is what we want
                    String readMessage = (String) msg.obj;                                  // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);                                      //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                        // determine the end-of-line
                    if (endOfLineIndex > 0) {                                               // make sure there data before ~
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

                            // Convert the string values into double, and store it into an array
                            // This will be the input for KNN
                            double flex1, flex2, flex3, flex4, flex5, gyrox, gyroy, gyroz, accx, accy, accz;
                            flex1 = Double.parseDouble(f1);
                            flex2 = Double.parseDouble(f2);
                            flex3 = Double.parseDouble(f3);
                            flex4 = Double.parseDouble(f4);
                            flex5 = Double.parseDouble(f5);
                            gyrox = Double.parseDouble(gx);
                            gyroy = Double.parseDouble(gy);
                            gyroz = Double.parseDouble(gz);
                            accx = Double.parseDouble(ax);
                            accy = Double.parseDouble(ay);
                            accz = Double.parseDouble(az);
                            double[] testfeatures = {flex1, flex2, flex3, flex4, flex5, gyrox, gyroy, gyroz, accx, accy, accz};

                            // Prepare the trained features and label values
                            // Get it from the text file and store it in a List
                            List<double[]> trainfeatures = new ArrayList<>();
                            List<String> trainlabel = new ArrayList<>();
                            BufferedReader reader = null;
                            try {
                                String line;
                                reader = new BufferedReader(new InputStreamReader(getAssets().open("dataset.txt"), "UTF-8"));
                                // Do reading, usually loop until end of file reading
                                // Once the loop ended, the values of trained features and labels are stored in the lists
                                while ((line = reader.readLine()) != null) {
                                    String[] split = line.split(",");
                                    double[] feature = new double[split.length - 1];
                                    for (int i = 0; i < split.length - 1; i++)
                                        feature[i] = Double.parseDouble(split[i]);
                                    trainfeatures.add(feature);
                                    trainlabel.add(split[feature.length]);
                                }
                            } catch (IOException e) {
                                //log the exception
                            } finally {
                                if (reader != null) {
                                    try {
                                        reader.close();
                                    } catch (IOException e) {
                                        //log the exception
                                    }
                                }
                            }

                            // The data preparation is done, its time for computation
                            // Get the distance between test values and each trained values.
                            double[][] distanceList = new double[trainfeatures.size()][2];
                            for(int index = 0; index < trainfeatures.size(); index++) {
                                //compute the distance
                                double[] features1 = trainfeatures.get(index);
                                double sum = 0;
                                for (int i = 0; i < features1.length; i++)
                                {
                                    //applied Euclidean distance formula
                                    sum += Math.pow(features1[i] - testfeatures[i], 2);
                                }
                                double distance = Math.sqrt(sum);

                                //add the computed distance in the list
                                distanceList[index][0] = distance;
                                distanceList[index][1] = index;
                            }

                            // Sort the list of distances
                            for(int i = 0; i < distanceList.length; i++)
                            {
                                for(int j = 0; j < distanceList.length -1; j++)
                                {
                                    if(distanceList[j][0] > distanceList[j+1][0])
                                    {
                                        //swap distance
                                        double temp = distanceList[j][0];
                                        distanceList[j][0] = distanceList[j+1][0];
                                        distanceList[j+1][0] = temp;
                                        //as well as label
                                        double temp2 = distanceList[j][1];
                                        distanceList[j][1] = distanceList[j+1][1];
                                        distanceList[j+1][1] = temp2;
                                    }
                                }
                            }

                            // Get the top "K" index in the distance lists.
                            // Then store the labels in a List of string
                            int k = 11;
                            List<String> targets = new ArrayList<>();
                            for(int i = 0; i < k; i++)
                            {
                                targets.add(trainlabel.get((int)distanceList[i][1]));
                            }

                            // Get the most number of label in the list.
                            // Insert all unique strings and update count if a string is not unique.
                            Map<String,Integer> hshmap = new HashMap<String, Integer>();
                            for (String str : targets)
                            {
                                if (hshmap.keySet().contains(str)) // if already exists then update count.
                                    hshmap.put(str, hshmap.get(str) + 1);
                                else
                                    hshmap.put(str, 1); // else insert it in the map.
                            }

                            // Use for storing the top 3 labels and their counts
                            String[] label = new String[3];
                            int[] value = new int[3];

                            // Loop 3 times
                            for(int ctr = 0; ctr < 3 ; ctr++){
                                String max_str = "";
                                int maxVal = 0;

                                // Traverse the map for the highest count of labels.
                                for (Map.Entry<String,Integer> entry : hshmap.entrySet())
                                {
                                    String key = entry.getKey();
                                    Integer count = entry.getValue();
                                    if (count > maxVal)
                                    {
                                        maxVal = count;
                                        max_str = key;
                                    }
                                    // Condition for the tie.
                                    else if (count == maxVal && max_str.compareTo(key) > 0)
                                        max_str = key;
                                }

                                // Check if there is no more label found
                                // Else, store the key and value in the lists
                                if(maxVal == 0) {
                                    label[ctr] = "-";
                                    value[ctr] = 0;
                                } else {
                                    label[ctr] = max_str;
                                    value[ctr] = maxVal;
                                }
                                // Remove the highest value in the hashmap
                                hshmap.remove(max_str);
                            }

                            // Compute for the confidence level of the top 3 labels
                            double conf1 = (value[0]/(double)k)*100;
                            double conf2 = (value[1]/(double)k)*100;
                            double conf3 = (value[2]/(double)k)*100;

                            // Print the text to UI
                            letter.setText(label[0]);
                            label1.setText("1.    " + label[0]);
                            label2.setText("2.    " + label[1]);
                            label3.setText("3.    " + label[2]);
                            DecimalFormat df2 = new DecimalFormat("#.##");
                            confidence1.setText(df2.format(conf1) + "%");
                            confidence2.setText(df2.format(conf2) + "%");
                            confidence3.setText(df2.format(conf3) + "%");

                            //======================FOR TYPING GAME=============================//
                            Log.i(TAG, "tag_typingGame");
                            String text = givenSentence;

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
                            char input = label[0].charAt(0);

                            // Compare the users input to the specific character
                            if(input == charList.get(counter)){
                                // Change the color to green
                                ss.setSpan(fcsGreen, 0, counter + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                // Increment counter
                                counter++;
                                //Check if it is the last letter
                                if(counter == charList.size()){
                                    isComplete = true;
                                }
                            } else{
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
                        recDataString.delete(0, recDataString.length());                    //clear all string data
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
        givenSentence = "CC";
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

    public void save(double speed, double accuracy, String date) {
//        File file = new File(FILE_NAME);
//        try {
//            FileWriter fileWriter = new FileWriter(file, true);
//            String content = speed + "," + accuracy + "," + date + "\n";
//            fileWriter.write(content);
//            fileWriter.close();
//            Log.i(TAG, "SAVED");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        FileOutputStream fos = null;
        try {
            // to delete, use MODE_PRIVATE, then change to MODE_APPEND
            fos = openFileOutput(FILE_NAME, MODE_APPEND);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String content = speed + "," + accuracy + "," + date + "\n";
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
        // mConnectedThread.write("x");
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

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        Log.i(TAG, "tag_checkBTState");
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
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
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
//        public void write(String input) {
//            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
//            try {
//                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
//            } catch (IOException e) {
//                //if you cannot write, close the application
//                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
//                finish();
//
//            }
//        }
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
    /*
    private class FileStream implements Runnable {

        @Override
        public void run() {
            FileOutputStream fileOutputStream = null;
            fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fileOutputStream.write();

            FileOutputStream fos = null;

            try {
                fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
                fos.write(Integer.parseInt(speed + "," + accuracy + "," + date));
                Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME,
                        Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }

     */

}

