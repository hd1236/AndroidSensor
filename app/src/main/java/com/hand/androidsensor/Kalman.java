package com.hand.androidsensor;

import java.util.ArrayList;
import java.util.List;

public class Kalman {
    private List<Double> noiseVariances;       // Noise variances
    private List<Double> predictedVariances;   // Predicted variances
    private List<Double> predictedValues;      // Predicted values

    private boolean isInitialised;
    private SensorSingleData sensorSingleData;

    public Kalman() {
        initObjects();
    }

    private void initObjects() {
        noiseVariances = new ArrayList<>();
        predictedVariances = new ArrayList<>();
        predictedValues = new ArrayList<>();
        isInitialised = false;
    }

    public SensorSingleData filter(SensorSingleData sensorSingleData) {
        this.sensorSingleData = sensorSingleData;
        List<Double> values = new ArrayList<>();
        values.add(sensorSingleData.getAccX());
        values.add(sensorSingleData.getAccY());
        values.add(sensorSingleData.getAccZ());
        return process(values);
    }

    private SensorSingleData init(List<Double> initValues) {
        for (int i = 0; i < initValues.size(); i++) {
            noiseVariances.add(i, Constants.VARIANCE);
            predictedVariances.add(i, noiseVariances.get(i));
            predictedValues.add(i, initValues.get(i));
        }
        isInitialised = true;

        sensorSingleData.setAccX(initValues.get(0));
        sensorSingleData.setAccY(initValues.get(1));
        sensorSingleData.setAccZ(initValues.get(2));


        return sensorSingleData;
    }

    private SensorSingleData process(List<Double> measurementValues) {
        if (isInitialised) {
            List<Double> correctedValues = new ArrayList<Double>();
            for (int i = 0; i < measurementValues.size(); i++) {
                // compute the Kalman gain
                double kalmanGain = predictedVariances.get(i) / (predictedVariances.get(i) + noiseVariances.get(i));

                // update the sensor prediction with the measurement
                double correctedValue = Constants.FILTER_GAIN * predictedValues.get(i) + (1.0 - Constants.FILTER_GAIN) *
                        measurementValues.get(i) + kalmanGain * (measurementValues.get(i) - predictedValues.get(i));

                // predict next variance and value
                predictedVariances.add(i, predictedVariances.get(i) * (1.0 - kalmanGain));
                predictedValues.add(i, correctedValue);

                correctedValues.add(i, correctedValue);
            }

            sensorSingleData.setAccX(correctedValues.get(0));
            sensorSingleData.setAccY(correctedValues.get(1));
            sensorSingleData.setAccZ(correctedValues.get(2));

            return sensorSingleData;
        } else {
            return init(measurementValues);
        }
    }
}
