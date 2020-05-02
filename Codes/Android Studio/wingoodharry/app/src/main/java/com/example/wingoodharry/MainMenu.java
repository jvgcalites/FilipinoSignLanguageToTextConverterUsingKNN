package com.example.wingoodharry;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    public static String KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }
    /** Called when the user taps the GestureClassifying button */
    public void gestureClassifer(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DeviceListActivity.class);

        // Attach the key value pair using putExtra to this intent
        String key = "classify";
        intent.putExtra(KEY, key);

        // Starting the activity
        startActivity(intent);
    }

    /** Called when the user taps the GestureTyping button */
    public void gestureTyping(View view) {
        // Creating and intializing Intent object
        Intent intent = new Intent(this, DeviceListActivity.class);

        // Attach the key value pair using putExtra to this intent
        String key= "type";
        intent.putExtra(KEY, key);

        // Starting the Activity
        startActivity(intent);
    }

    /** Called when the user taps the ShowStats button */
    public void showStats(View view) {
         Intent intent = new Intent(this, StatsActivity.class);
         startActivity(intent);
    }
}
