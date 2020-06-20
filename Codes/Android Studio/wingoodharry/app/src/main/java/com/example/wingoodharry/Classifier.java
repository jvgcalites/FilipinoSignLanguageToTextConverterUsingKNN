package com.example.wingoodharry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classifier {
    private static final String TAG = "Classifier";
    private double flex1;
    private double flex2;
    private double flex3;
    private double flex4;
    private double flex5;
    private double gyroX;
    private double gyroY;
    private double gyroZ;
    private double accX;
    private double accY;
    private double accZ;
    private double[] testFeatures;
    private List<double[]> trainedFeatures;
    private List<String> trainedLabel;
    private double[][] distanceList;
    private int k;
    private List<String> targetLabels;
    private String[] topThreeLabel;
    private int[] topThreeLabelCount;
    private double[] topThreeConfidence;

    public Classifier() {
        this.k = 5;
        this.trainedFeatures = new ArrayList<>();
        this.trainedLabel = new ArrayList<>();
        this.targetLabels = new ArrayList<>();
        this.topThreeLabel = new String[3];
        this.topThreeLabelCount = new int[3];
        this.topThreeConfidence = new double[3];
    }


    public void setFlex1(String flex1) {
        this.flex1 = Double.parseDouble(flex1);
    }

    public void setFlex2(String flex2) {
        this.flex2 = Double.parseDouble(flex2);
    }

    public void setFlex3(String flex3) {
        this.flex3 = Double.parseDouble(flex3);
    }

    public void setFlex4(String flex4) {
        this.flex4 = Double.parseDouble(flex4);
    }

    public void setFlex5(String flex5) {
        this.flex5 = Double.parseDouble(flex5);
    }

    public void setGyroX(String gyroX) {
        this.gyroX = Double.parseDouble(gyroX);
    }

    public void setGyroY(String gyroY) {
        this.gyroY = Double.parseDouble(gyroY);
    }

    public void setGyroZ(String gyroZ) {
        this.gyroZ = Double.parseDouble(gyroZ);
    }

    public void setAccX(String accX) {
        this.accX = Double.parseDouble(accX);
    }

    public void setAccY(String accY) {
        this.accY = Double.parseDouble(accY);
    }

    public void setAccZ(String accZ) {
        this.accZ = Double.parseDouble(accZ);
    }

    private void AddTestFeatures () {
        this.testFeatures = new double[]{
                flex1, flex2, flex3, flex4, flex5,
                gyroX, gyroY, gyroZ,
                accX, accY, accZ};
    }

    public void AddTrainedFeatures(double[] trainedFeatures) {
        this.trainedFeatures.add(trainedFeatures);
    }

    public void AddTrainedLabel(String trainedLabel) {
        this.trainedLabel.add(trainedLabel);
    }

    private void ComputeDistance() {
        int totalTrainedFeatures = this.trainedFeatures.size();
        this.distanceList = new double[totalTrainedFeatures][2];

        double[] indexTrainedFeature;
        double sum;
        double distance;

        for(int i = 0; i < totalTrainedFeatures; i++) {
            indexTrainedFeature = this.trainedFeatures.get(i);
            sum = 0;
            for(int j = 0; j < indexTrainedFeature.length; j++) {
                sum += Math.pow(indexTrainedFeature[j] - testFeatures[j], 2);
            }
            distance = Math.sqrt(sum);
            this.distanceList[i][0] = distance;
            this.distanceList[i][1] = i;
        }
    }

    private void SortDistanceList() {
        int distanceListLength = distanceList.length;

        for(int i = 0; i < distanceListLength; i++) {
            for(int j = 0; j < distanceListLength - 1; j++) {
                if(distanceList[j][0] > distanceList[j+1][0]) {
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
    }

    private void GetTopLabels() {
        for(int i = 0; i < this.k; i++){
            targetLabels.add(trainedLabel.get((int) distanceList[i][1]));
        }
    }

    private void GetMostNumberOfLabel() {
        Map<String, Integer> hashmap = new HashMap<String, Integer>();

        for(String label : targetLabels){
            if (hashmap.keySet().contains(label))
                hashmap.put(label, hashmap.get(label) + 1);
            else
                hashmap.put(label, 1);
        }

        for(int counter = 0; counter < 3; counter++) {
            String highestLabelCount = "";
            int labelCount = 0;

            for(Map.Entry<String, Integer> entry : hashmap.entrySet()){
                String key = entry.getKey();
                Integer value = entry.getValue();
                if (value > labelCount){
                    highestLabelCount = key;
                    labelCount = value;
                }
                else if(value == labelCount && highestLabelCount.compareTo(key) > 0)
                    highestLabelCount = key;
            }

            if(labelCount == 0){
                topThreeLabel[counter] = "-";
                topThreeLabelCount[counter] = 0;
            } else {
                topThreeLabel[counter] = highestLabelCount;
                topThreeLabelCount[counter] = labelCount;
            }

            hashmap.remove(highestLabelCount);
        }
    }

    private void ComputeConfidence() {
        for(int i = 0; i < 3; i++) {
            topThreeConfidence[i] = (topThreeLabelCount[i] / (double) this.k) * 100;
        }
    }

    public void Classify(){
        AddTestFeatures();
        ComputeDistance();
        SortDistanceList();
        GetTopLabels();
        GetMostNumberOfLabel();
        ComputeConfidence();
    }

    public double GetConfidence(int i) {
        return topThreeConfidence[i];
    }

    public String GetLabel(int i) {
        return topThreeLabel[i];
    }
}
