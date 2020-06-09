package com.example.wingoodharry;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

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
        // Open the file
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = openFileInput(FILE_NAME);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Get the data from the file
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String[]> gameDataList = new ArrayList<>();
        String string;
        while ((string = bufferedReader.readLine()) != null) {
            String[] split = string.split(",");
            String[] gameData = new String[split.length];
            for (int index = 0; index < split.length; index++) {
                gameData[index] = split[index];
            }
            gameDataList.add(gameData);
        }

        // Use the data to show statistics
        Statistics statistics = new Statistics();
        statistics.setSpeedAccuracyList(gameDataList);
        gamesPlayed.setText("Games Played: " + statistics.getGamesPlayedCount());
        speedRecord.setText("Speed Record: " + statistics.getSpeedRecord() + "WPM");
        averageSpeed.setText("Average Speed: " + statistics.getAverageSpeed() + "WPM");
        averageAccuracy.setText("Average Accuracy: " + statistics.getAverageAccuracy() + "%");

        // Prepare data for x and y axis
        List<String> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();
        for(int i = 0; i < gameDataList.size(); i++){
            String[] gameData = gameDataList.get(i);
            // store the date
            xData.add(gameData[2]);
            // store the speed
            yData.add(Float.parseFloat(gameData[0]));
        }

        // Declare a list which will hold the values in x and y axis
        List yAxisValues = new ArrayList();
        List xAxisValues = new ArrayList();

        // Declare and initialize the line that will show in the graph with set color
        Line line = new Line(yAxisValues).setColor(Color.parseColor("#9C27B0"));

        // Initialize the X and Y axis values lists
        for(int i = 0; i < xData.size(); i++){
            xAxisValues.add(i, new AxisValue(i).setLabel(xData.get(i)));
        }
        for (int i = 0; i < yData.size(); i++){
            yAxisValues.add(new PointValue(i, yData.get(i)));
        }

        // Declare a list that will hold the line of the graph chart
        List lines = new ArrayList();
        lines.add(line);

        // Add the graph line to the overall chart data
        LineChartData data = new LineChartData();
        data.setLines(lines);

        // View the android line chart
        LineChartView lineChartView = findViewById(R.id.graph);
        lineChartView.setLineChartData(data);

        // Show x and y axis
        Axis axis = new Axis();
        axis.setValues(xAxisValues);
        data.setAxisXBottom(axis);

        // Show y axis values
        Axis yAxis = new Axis();
        data.setAxisYLeft(yAxis);

        // change x axis values text size
        axis.setTextSize(8);

        // change text color of axis data
        axis.setTextColor(Color.parseColor("#03A9F4"));
        yAxis.setTextColor(Color.parseColor("#03A9F4"));

        // adjust y axis maximum value
        Viewport viewport = new Viewport(lineChartView.getMaximumViewport());
        Float top = (float) statistics.getSpeedRecord() + 1;
        viewport.top = top;
        lineChartView.setMaximumViewport(viewport);
        lineChartView.setCurrentViewport(viewport);

        // make the X axis labels tilt
        axis.setHasTiltedLabels(true);

    }
}
