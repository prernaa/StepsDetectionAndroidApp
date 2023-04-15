package com.example.pchikers.stepcounter;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.sqrt;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by pchikers on 10/2/17.
 */

public class StepDetector {
    // Define params
    private static final int ACCEL_BUFFER_SIZE = 50;
    private static final int ACCEL_STEP_EVERY= 40; // overlap of 10 steps between buffers for which we count steps
    private static final int MEDIAN_FILTER_WIN = 8;
    private static final double ZERO_CROSSING_AMP_THRES = 12.0;

    private int accelValCounter = 0;
    private double[] accelBufferMag = new double[ACCEL_BUFFER_SIZE];

    private StepListener listenerMain;

    public void registerMain(StepListener main) {
        this.listenerMain = main;
    }
    private double[] medianFilter(double[] buffer, int win){
        double [] filtered = new double[buffer.length-win+1];
        for (int i = 0; i<=buffer.length-win; i++){
            double[] vals = new double[win];
            for (int w = 0; w<win; w++){
                vals[w] = buffer[i+w];
//                Log.d("val in win = ", String.valueOf(vals[w]));
            }
            Arrays.sort(vals);
            double median;
            if (win%2==0){ // even win size
                int firstIdx = (win/2)-1; // Eg: win = 4 (0,1,2,3), firstIdx = 1
                int secondIdx = firstIdx + 1;
                median = vals[firstIdx]+ vals[secondIdx];
            }
            else{ // odd win size
                int Idx = (win/2); // Eg: win = 5 (0,1,2,3,4), Idx = 2
                median = vals[Idx];
            }
//            Log.d("median = ", String.valueOf(median));
            filtered[i] = median;
        }

        return filtered;
    }
    public void updateAccel(float x, float y, float z) {
        double currentMag = sqrt(x*x + y*y + z*z);
        accelBufferMag[accelValCounter % ACCEL_BUFFER_SIZE] = currentMag;
        // Local buffer for computation
        double[] accelBufferMag_NORM = new double[ACCEL_BUFFER_SIZE];
        // De-meaning
        double sum = 0;
        for (int i = 0; i < accelBufferMag.length; i++) {
            sum += accelBufferMag[i];
        }
        double mean = sum / ((double) accelBufferMag.length);
        for (int i = 0; i < accelBufferMag.length; i++)
        {
            accelBufferMag_NORM[i] = accelBufferMag[i] - mean;
        }
        // Median filtering, changes size of local buffer s.t. newlen = len-win+1
        int win = MEDIAN_FILTER_WIN;
        if (accelValCounter>ACCEL_BUFFER_SIZE) {
            accelBufferMag_NORM = medianFilter(accelBufferMag_NORM, win);
        }
        // Zero crossing: Count zero crossing, when max value or amplitude between zero crossings is above a certain threshold
        // We basically want each peak to be above a certain threshold
        int numCrossing = 0;
        double thres = ZERO_CROSSING_AMP_THRES;
        double maxval = 0;
        for (int i = 1; i < accelBufferMag_NORM.length; i++)
        {
            double diff = abs(accelBufferMag_NORM[i]-accelBufferMag_NORM[i-1]);
            if (maxval<abs(accelBufferMag_NORM[i-1])){
                maxval = abs(accelBufferMag_NORM[i-1]);
            }
            if (diff>0) {
                if(maxval > thres){
                    numCrossing++;
                }
                maxval = 0;
            }
        }
        // If this buffer is complete, AND has 2 zero-crossing (1 + to - and 1 - to +), AND step size has been taken
        //Log.d("zero crossing = ", String.valueOf(numCrossing));
        if ((accelValCounter>ACCEL_BUFFER_SIZE) && numCrossing >= 2 && (accelValCounter % ACCEL_STEP_EVERY == 0)) {
            listenerMain.step();
        }
        // sends a new point to graph. Only do it after we have the first full buffer
        if (accelValCounter>ACCEL_BUFFER_SIZE) {
            //Log.d("lngth = ", String.valueOf(accelBufferMag_NORM.length));
            double outMag = accelBufferMag_NORM[accelValCounter % (ACCEL_BUFFER_SIZE - MEDIAN_FILTER_WIN + 1)];
            //Log.d("mag = ", String.valueOf(outMag));
            listenerMain.dispNewValGraph(outMag);
        }
        // update counter for samples
        accelValCounter++;
    }

}