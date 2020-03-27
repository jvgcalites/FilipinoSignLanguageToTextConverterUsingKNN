package com.example.learnfsl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    //    private final String DEVICE_NAME="MyBTBee";
    private final String DEVICE_ADDRESS="00:15:83:35:98:6B";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    Button startButton, sendButton,clearButton,stopButton;
    TextView fs0,fs1, fs2, fs3, fs4, fs[], connection;
    int i = 0;
    // EditText editText;
    String empty = "";
    boolean deviceConnected=false;
    Thread thread;
    byte buffer[];
    int bufferPosition, x;
    boolean stopThread;
    int[] textViewIDs = new int[] {R.id.flexsensor0};//, R.id.flexsensor1, R.id.flexsensor2,R.id.flexsensor3,R.id.flexsensor4 };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.button);
        //  sendButton = (Button) findViewById(R.id.send);
        clearButton = (Button) findViewById(R.id.clear);
        stopButton = (Button) findViewById(R.id.Stop);
        //   editText = (EditText) findViewById(R.id.editText);
        fs0 = (TextView) findViewById(R.id.flexsensor0);
    /*    fs1 = (TextView) findViewById(R.id.flexsensor1);
        fs2 = (TextView) findViewById(R.id.flexsensor2);
        fs3 = (TextView) findViewById(R.id.flexsensor3);
        fs4 = (TextView) findViewById(R.id.flexsensor4);

*/
        connection = (TextView) findViewById(R.id.textView6);
        setUiEnabled(false);



        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BTinit())
                {
                    if(BTconnect())
                    {
                        setUiEnabled(true);
                        deviceConnected=true;
                        beginListenForData();
                        connection.setText("Connection Opened!");
                    }

                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView fsensor0 = (TextView)findViewById(R.id.flexsensor0);
                fsensor0.setText(empty);
             /*   TextView fsensor1 = (TextView)findViewById(R.id.flexsensor1);
                fsensor1.setText(empty);
                TextView fsensor2 = (TextView)findViewById(R.id.flexsensor2);
                fsensor2.setText(empty);
                TextView fsensor3 = (TextView)findViewById(R.id.flexsensor3);
                fsensor3.setText(empty);
                TextView fsensor4 = (TextView)findViewById(R.id.flexsensor4);
                fsensor4.setText(empty);

                TextView accelerometerx = (TextView)findViewById(R.id.accelx);
                accelerometerx.setText(empty);
                TextView accelerometery = (TextView)findViewById(R.id.accely);
                accelerometery.setText(empty);
                TextView accelerometerz = (TextView)findViewById(R.id.accelz);
                accelerometerz.setText(empty);

                TextView gyroscopex = (TextView)findViewById(R.id.gyrox);
                gyroscopex.setText(empty);
                TextView gyroscopey = (TextView)findViewById(R.id.gyroy);
                gyroscopey.setText(empty);
                TextView gyroscopez = (TextView)findViewById(R.id.gyroz);
                gyroscopez.setText(empty);
*/
            }
        });



        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
    ////////////////////
    public void setUiEnabled(boolean bool)
    {
        startButton.setEnabled(!bool);
        // sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        fs0.setEnabled(bool);
        // fs1.setEnabled(bool);
    }
    ////////////
    public boolean BTinit()
    {
        boolean found=false;
        BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"Device doesnt Support Bluetooth",Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty())
        {
            Toast.makeText(getApplicationContext(),"Please Pair the Device first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            for (BluetoothDevice iterator : bondedDevices)
            {
                if(iterator.getAddress().equals(DEVICE_ADDRESS))
                {
                    device=iterator;
                    found=true;
                    break;
                }
            }
        }
        return found;
    }
    //////////////
    public boolean BTconnect()
    {
        boolean connected=true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected=false;
        }
        if(connected)
        {
            try {
                outputStream=socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream=socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return connected;
    }


    ///////////////
    void beginListenForData()
    {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];

        Thread thread  = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopThread)
                {

                    try
                    {

                        int byteCount = inputStream.available();


                        //  for( i=0; i < textViewIDs.length; i++) {


                        if (byteCount > 0) {
                            byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes);
                            final String string = new String(rawBytes, "UTF-8");

                            handler.post(new Runnable()
                            {
                                public void run() {
                                    //  TextView tv = (TextView) findViewById(textViewIDs[i]);
                                    // tv.setText(string);

                                    TextView fsensor0 = (TextView)findViewById(R.id.flexsensor0);
                                    fsensor0.append(string);
                                       /* if(i==textViewIDs.length-1)
                                        {
                                            i=0;
                                        }*/

                                }

                            });

                        }

                        // }




                    }
                    catch (IOException ex)
                    {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    //   public void onClickSend(View view) {
    //      String string = editText.getText().toString();
    //      string.concat("\n");
    //     try {
    //         outputStream.write(string.getBytes());
    //     } catch (IOException e) {
    //        e.printStackTrace();
    //     }
    //     textView.append("\nSent Data:"+string+"\n");

    //   }

    public void onClickStop(View view) throws IOException {
        stopThread = true;
        outputStream.close();
        inputStream.close();
        socket.close();
        setUiEnabled(false);
        deviceConnected=false;
        connection.append("\nConnection Closed!\n");
    }

    public void onClickClear(View view) {
        //textView.setText("");
    }



}



