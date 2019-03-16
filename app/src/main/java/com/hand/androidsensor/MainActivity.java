package com.hand.androidsensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 用方向传感器获取方向值TYPE_ACCELEROMETER/TYPE_MAGNETIC_FIELD
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private TextView mTvShow;
    private TextView mTvOrigin;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticSensor;
    private boolean mRegister1;
    private boolean mRegister2;

    private float[] aValues = new float[3];
    private float[] mValues = new float[3];

    private Kalman mKalman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvShow = findViewById(R.id.show_tv);
        mTvOrigin = findViewById(R.id.origin_tv);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mKalman = new Kalman();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRegister1 = mSensorManager.registerListener(this,mAccelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);
        mRegister2= mSensorManager.registerListener(this,mMagneticSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRegister1 || mRegister2){
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType ()){
            case Sensor.TYPE_ACCELEROMETER:
                aValues = sensorEvent.values.clone ();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = sensorEvent.values.clone ();
                break;
        }

        float[] R = new float[16];
        float[] orientationValues = new float[3];

        SensorManager.getRotationMatrix (R, null, aValues, mValues);
        SensorManager.getOrientation (R, orientationValues);

        orientationValues[0] = (float)Math.toDegrees (orientationValues[0]);
        orientationValues[1] = (float)Math.toDegrees (orientationValues[1]);
        orientationValues[2] = (float)Math.toDegrees (orientationValues[2]);

        SensorSingleData data = mKalman.filter(new SensorSingleData(orientationValues[1],orientationValues[2],orientationValues[0]));
        String originTxt = "(pitch)x:"+orientationValues[1]+
                "\n(roll)y:"+orientationValues[2]+
                "\n(azimuth)z:"+orientationValues[0];

        String showTxt = "(pitch)x:"+data.getAccX()+
                "\n(roll)y:"+data.getAccY()+
                "\n(azimuth)z:"+data.getAccZ();
        mTvOrigin.setText(originTxt);
        mTvShow.setText(showTxt);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
