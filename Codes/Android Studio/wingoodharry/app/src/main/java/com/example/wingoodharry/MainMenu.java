package com.example.wingoodharry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }
    /** Called when the user taps the Send button */
    public void gestureClassifer(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivity(intent);
    }

    /** Called when the user taps the Send button */
    public void gestureTyping(View view) {
        // Do something in response to button
    }
}
