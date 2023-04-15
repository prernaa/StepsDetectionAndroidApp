package com.example.pchikers.stepcounter;

import android.hardware.SensorEvent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.SensorManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.view.View;
import android.view.View.OnClickListener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView TvSteps;
    private Button BtnStart;
    private Button BtnStop;
    private StepDetector myStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    // charting
    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> series;
    private int lastX = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        myStepDetector = new StepDetector();
        myStepDetector.registerMain(this);
        TvSteps = (TextView) findViewById(R.id.tv_steps);
        BtnStart = (Button) findViewById(R.id.btn_start);
        BtnStop = (Button) findViewById(R.id.btn_stop);
        BtnStart.setOnClickListener(btnStartListener);
        BtnStop.setOnClickListener(btnStopListener);
        //charting
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(-20);
        viewport.setMaxY(20);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(500);
        viewport.setScrollable(true);
    }
//    // add random data to graph
//    private void addEntry() {
//        // here, we choose to display max 10 points on the viewport and we scroll to end
//        series.appendData(new DataPoint(lastX++, RANDOM.nextDouble() * 10d), true, 1000);
//    }
    private void addPoint(double mag) {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        series.appendData(new DataPoint(lastX++, mag), true, 1000);
    }

    private OnClickListener btnStartListener = new OnClickListener() {
        public void onClick(View v) {
            numSteps = 0;
            sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        }
    };
    private OnClickListener btnStopListener = new OnClickListener() {
        public void onClick(View v) {
            sensorManager.unregisterListener(MainActivity.this);
        }
    };
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            myStepDetector.updateAccel(event.values[0], event.values[1], event.values[2]);
        }
    }
    @Override
    public void step() {
        numSteps++;
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }
    @Override
    public void dispNewValGraph(double mag) {
//        addEntry();
//        Log.d("Out: ", String.valueOf(mag));
        addPoint(mag); // this should be moved to a different function

    }
}
