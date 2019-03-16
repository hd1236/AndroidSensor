package com.hand.androidsensor;

public class Constants {

    // Kalman Filter
    public static final double VARIANCE = 0.05;

    /**
     *to value in range [0.0 - 1.0]. Smaller the value is -> Kalman filter algorithm has less impact to the final data.
     */
    public static final double FILTER_GAIN = 0.7;
}
