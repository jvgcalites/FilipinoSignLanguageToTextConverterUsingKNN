package com.example.wingoodharry;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsActivity extends AppCompatActivity{

    private static final String TAG = "StatsActivity";
    TextView gamesPlayed, speedRecord, averageSpeed, averageAccuracy;
    private static final String FILE_NAME = "gameData.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        gamesPlayed = findViewById(R.id.gamesPlayed);
        speedRecord = findViewById(R.id.speedRecord);
        averageSpeed = findViewById(R.id.averageSpeed);
        averageAccuracy = findViewById(R.id.averageAccuracy);

        try {
            load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();

        List<String[]> gameDataList = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] split = line.split(",");
            String[] gameData = new String[split.length];
            for (int index = 0; index < split.length; index++) {
                gameData[index] = split[index];
                Log.i(TAG, gameData[index]);
            }
            gameDataList.add(gameData);
        }

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/ dd/ yy");

        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(generateData(gameDataList));
        graph.addSeries(series);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // what if return null?
                    return simpleDateFormat.format(new Date((long)value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(graph.getContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
        //graph.getGridLabelRenderer().setHumanRounding(false);

        Statistics statistics = new Statistics();
        statistics.setSpeedAccuracyList(gameDataList);
        gamesPlayed.setText("Games Played: " + statistics.getGamesPlayedCount());
        speedRecord.setText("Speed Record: " + statistics.getSpeedRecord());
        averageSpeed.setText("Average Speed: " + statistics.getAverageSpeed());
        averageAccuracy.setText("Average Accuracy: " + statistics.getAverageAccuracy());
    }

    private Date stringToDate(String aDate,String aFormat) {

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat(aFormat);
        try {
            date = format.parse(aDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    private DataPoint[] generateData(List<String[]> gameDataList) {
        int count = gameDataList.size();
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            String[] gameData = gameDataList.get(i);

            String aDate = gameData[2];
            String aFormat = "MM/ dd/ yy";
            SimpleDateFormat format = new SimpleDateFormat(aFormat);
            Date date = null;
            try {
                date = format.parse(aDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date datex = new Date();
            Date x = stringToDate(gameData[2],"dd-MM-yyyy");


            double y = Double.parseDouble(gameData[0]);
            Log.i(TAG, "date = " + datex);
            Log.i(TAG,"date = " + date);
            Log.i(TAG,"date = " + x);
            Log.i(TAG,"speed = " + gameData[0]);
            Log.i(TAG,"accuracy = " + gameData[1]);
            Log.i(TAG,"date = " + gameData[2]);
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        return values;
    }
}
