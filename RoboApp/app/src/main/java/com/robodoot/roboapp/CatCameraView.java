package com.robodoot.roboapp;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Created by alex on 3/5/16.
 * Camera view that sets its fps range to the supported range with the maximum high end.
 * Not sure if this helps at all (default is probably the max)
 */
public class CatCameraView extends JavaCameraView {
    private static final String TAG = "CatCameraView";

    public CatCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void enableView() {
        super.enableView();
        setMaxFps();
    }

    private void setMaxFps() {
        Camera.Parameters params = null;
        synchronized (this) {
            if (mCamera == null) {
                Log.i(TAG, "mCamera is null. can't set fps");
                return;
            }
            params = mCamera.getParameters();
        }
        if (params == null) return;

        List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
        if (supportedFpsRanges == null) return;

        int[] maxRange = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        for (int[] range : supportedFpsRanges) {
            if (range[1] > maxRange[1]) {
                maxRange = range;
            }
        }

        params.setPreviewFpsRange(maxRange[0], maxRange[1]);
        mCamera.setParameters(params);
    }
}