package com.hand.androidsensor.chart;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hand.androidsensor.Kalman;
import com.hand.androidsensor.R;
import com.hand.androidsensor.SensorSingleData;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener {

    private LineChart chart;
    private final int mDataCount = 30;
    private int index;
    private ArrayList<Entry> values1 = new ArrayList<>(),values2 = new ArrayList<>();

    private TextView mTvShow;
    private TextView mTvOrigin;

    private SensorManager mSensorManager;
    private Sensor mAccelerometerSensor;
    private Sensor mMagneticSensor;
    private boolean mRegister1;
    private boolean mRegister2;

    private boolean mHasAccelerometerValue;
    private boolean mHasMagneticValue;

    private float[] aValues = new float[3];
    private float[] mValues = new float[3];

    private Kalman mKalman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        mTvShow = findViewById(R.id.show_tv);
        mTvOrigin = findViewById(R.id.origin_tv);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mKalman = new Kalman();

        initCharts();
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
                mHasAccelerometerValue = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mValues = sensorEvent.values.clone ();
                mHasMagneticValue = true;
                break;
        }

        Log.i("ChartActivity","mHasAccelerometerValue:"+mHasAccelerometerValue+"----mHasMagneticValue:"+mHasMagneticValue);
        if(!mHasAccelerometerValue || !mHasMagneticValue){
            return;
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

        if(index < mDataCount){
            values1.add(new Entry(index,orientationValues[1]));
            values2.add(new Entry(index, (float) data.getAccX()));
            index++;
        }
        if(index == mDataCount){
            setData(values1,values2);
            index++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    private void initCharts() {
        chart = findViewById(R.id.chart1);
        chart.setOnChartValueSelectedListener(this);

        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);

        // set an alternative background color
        chart.setBackgroundColor(Color.LTGRAY);

        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
//        l.setYOffset(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaximum(0.3f);
        leftAxis.setAxisMinimum(0.09f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(Color.RED);
        rightAxis.setAxisMaximum(0.3f);
        rightAxis.setAxisMinimum(0.09f);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);
    }

    /**
     * values1为蓝色线
     * values2为红色线
     * @param values1
     * @param values2
     */
    private void setData(ArrayList<Entry> values1,ArrayList<Entry> values2) {

        Log.i("yzmhand","values1："+values1.toString()+"---values2"+values2.toString());
        if(values1 == null || values2 == null){
            return;
        }

        LineDataSet set1, set2;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) chart.getData().getDataSetByIndex(1);
            set1.setValues(values1);
            set2.setValues(values2);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values1, "DataSet 1");

            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(1f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(values2, "DataSet 2");
            set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set2.setColor(Color.RED);
            set2.setCircleColor(Color.WHITE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(1f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            // create a data object with the data sets
            LineData data = new LineData(set1, set2);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);

            // set data
            chart.setData(data);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
    }

    @Override
    public void onNothingSelected() {
    }
}
