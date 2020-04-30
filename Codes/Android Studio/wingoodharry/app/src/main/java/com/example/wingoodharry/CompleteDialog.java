package com.example.wingoodharry;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.DialogFragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CompleteDialog extends AppCompatDialogFragment {

    private TextView txtSpeed, txtAccuracy;
    private Button btnMenu, btnNext;
    private ExampleDialogListener listener;
    private double speed, accuracy;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        // Build the dialog with the builder
        builder.setView(view);

        // link textViews and buttons
        txtSpeed = view.findViewById(R.id.txtSpeed);
        txtAccuracy = view.findViewById(R.id.txtAccuracy);
        btnMenu = view.findViewById(R.id.btnMenu);
        btnNext = view.findViewById(R.id.btnNext);

        // setup the dialog box
        DecimalFormat df2 = new DecimalFormat("#.##");
        txtSpeed.setText("Speed : " + df2.format(speed) + "WPM");
        txtAccuracy.setText("Accuracy : " + df2.format(accuracy) + "%");

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // refresh the GestureTyping activity
                listener.initializeActivity();
                // close the dialog
                getDialog().dismiss();
            }
        });
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // return to menu
                Intent intent = new Intent(v.getContext(), MainMenu.class);
                startActivity(intent);
            }
        });

        return builder.create();
    }

    public void setSpeed(double speed){
        this.speed = speed;
    }

    public void setAccuracy(double accuracy){
        this.accuracy = accuracy;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ExampleDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    public interface ExampleDialogListener {
        void initializeActivity();
    }
}
