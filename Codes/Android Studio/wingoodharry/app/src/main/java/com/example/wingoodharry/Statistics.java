package com.example.wingoodharry;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
    private List<Double> speedList;
    private List<Double> accuracyList;

    public Statistics() {
        this.speedList = new ArrayList<>();
        this.accuracyList = new ArrayList<>();
    }

    public void setSpeedAccuracyList(List<String[]> gameDataList) {
        String[] gameRecord;
        for(int i = 0; i < gameDataList.size(); i++) {
            gameRecord = gameDataList.get(i);
            speedList.add(Double.parseDouble(gameRecord[0]));
            accuracyList.add(Double.parseDouble(gameRecord[1]));
        }
    }

    public int getGamesPlayedCount() {
        return speedList.size();
    }

    public double getSpeedRecord() {
        double speedRecord = 0;
        for(int i = 0; i < speedList.size(); i++) {
            if (speedRecord < speedList.get(i)) {
                speedRecord = speedList.get(i);
            }
        }
        return speedRecord;
    }

    public double getAverageSpeed() {
        double averageSpeed = 0;
        int totalSum = 0;
        for(int i = 0; i < speedList.size(); i++) {
            totalSum += speedList.get(i);
        }
        averageSpeed = totalSum/speedList.size();
        return averageSpeed;
    }

    public double getAverageAccuracy() {
        double averageAccuracy = 0;
        int totalSum = 0;
        for(int i = 0; i < accuracyList.size(); i++) {
            totalSum += accuracyList.get(i);
        }
        averageAccuracy = totalSum/accuracyList.size();
        return averageAccuracy;
    }
}
