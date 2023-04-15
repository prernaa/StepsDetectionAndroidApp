# StepsDetectionAndroidApp
An android app that performs step detecting from scratch

Here is how the accelerometer data was processed to derive step count:
1. For each new sensor value (x, y, z), magnitude m was calculated.
2. Magnitude values over ACCEL_BUFFER_SIZE (set to 50) number of samples were stored in a global buffer.
3. A local buffer which was a copy of the global buffer was created. All calculations moving forward were performed on this local buffer. 
4. Mean of the buffer was subtracted from each element of the buffer. This process is called demeaning.
5. Median filtering is also performed on the buffer with a window size of MEDIAN_FILTER_WIN (set to 8) number of samples. Median filtering also reduces the size of the buffer such that new_length_of_buffer = old_length_of_buffer - MEDIAN_FILTER_WIN + 1.
6. Zero-crossings are counted next. However, only zero-crossings where the absolute maximum value (like amplitude) between previous and current zero-crossing is above a certain threshold are counted. This threshold is ZERO_CROSSING_AMP_THRES (set to 12).
7. If final zero-crossings are greater than 2 (‘cause we only want to count either +ve to -ve or -ve to +ve but not both) and ACCEL_STEP_EVERY (set to 40) number of samples have passed since the last set of values for which steps were counted, we increment step count by 1. Note that ACCEL_STEP_EVERY (=40) is the step size after which we calculate new steps. So, steps are calculated over buffer size of 50 with step size of 40. Hence only 10 samples are common between any two buffers from which new steps are calculated. 

The following parameters are empirically tuned:
ACCEL_BUFFER_SIZE = 50
ACCEL_STEP_EVERY  = 40
MEDIAN_FILTER_WIN = 8
ZERO_CROSSING_AMP_THRES = 12

Results are especially sensitive to ZERO_CROSSING_AMP_THRES, and this parameter depends on how a person walks and holds the phone. 