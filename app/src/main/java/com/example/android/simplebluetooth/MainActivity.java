package com.example.android.simplebluetooth;

import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //pointers to Sensor Manager of this system and the Accelerometer (through the Sensor Manager)
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;
    //keeps track of x,y,z values of accelerometer
    private float last_x, last_y, last_z, speed;

    //add data to line chart
    //LineDataSet only used for styling
    List<Entry> dataX, dataY, dataZ;
    LineDataSet setX, setY, setZ;
    List<ILineDataSet> dataSets;

    LineChart lineChart;
    TextView speedValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        lineChart = (LineChart) findViewById(R.id.lineChart);
        speedValue = (TextView) findViewById(R.id.speedText);

        //lineChart.setDescription("");
        lineChart.setNoDataText("No data for the moment");

        //enable value highlighting
        lineChart.setHighlightPerTapEnabled(true);

        //enable touch gestures
        lineChart.setTouchEnabled(true);

        //enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);

        //enable pinch zoom to avoid scaling x and y axis separately
        lineChart.setPinchZoom(true);

        dataX = new ArrayList<Entry>();
        dataY = new ArrayList<Entry>();
        dataZ = new ArrayList<Entry>();

        updateValues();
    }

    protected void updateValues() {
        setX = new LineDataSet(dataX, "X axis");
        setY = new LineDataSet(dataY, "Y axis");
        setZ = new LineDataSet(dataZ, "Z axis");

        setX.setAxisDependency(YAxis.AxisDependency.LEFT);
        setY.setAxisDependency(YAxis.AxisDependency.LEFT);
        setZ.setAxisDependency(YAxis.AxisDependency.LEFT);

        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setX);
        dataSets.add(setY);
        dataSets.add(setZ);

        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        //refresh
        lineChart.invalidate();
    }

    //temporarily disables accelerometer
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        //double-check if the sensor that changed was the accelerometer
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();

            //makes sure data only taken every 0.1 seconds, curTime device time in milliseconds
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                speedValue.setText(Float.toString(speed));

                last_x = x;
                last_y = y;
                last_z = z;

                LineData data = lineChart.getData();

                if((data != null)&&(setX.getEntryCount() >= 100)){

                    if(setX.removeFirst()) {
                        data.notifyDataChanged();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }

                    if(setY.removeFirst()) {
                        data.notifyDataChanged();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }

                    if(setZ.removeFirst()){
                        data.notifyDataChanged();
                        lineChart.notifyDataSetChanged();
                        lineChart.invalidate();
                    }

                }

                Entry xValue = new Entry((float) setX.getEntryCount(), x);
                Entry yValue = new Entry((float) setX.getEntryCount(), y);
                Entry zValue = new Entry((float) setX.getEntryCount(), z);

                setX.addEntry(xValue);
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();

                setY.addEntry(yValue);
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();

                setZ.addEntry(zValue);
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();




            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }
}

