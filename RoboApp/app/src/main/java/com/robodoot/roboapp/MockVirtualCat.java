package com.robodoot.roboapp;

import android.os.Handler;
import android.util.Log;

import com.robodoot.dr.RoboApp.PololuHandler;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of the VirtualCat interface. It is a dummy implementation that just
 * logs everything that happens. It is meant to be used when the actual cat isn't available.
 */
public class MockVirtualCat extends VirtualCat {
    private static final String TAG = "MockVirtualCat";

    // DEFAULT CONSTRUCTOR
    public MockVirtualCat() {
        batteryUpdateHandler.postDelayed(batteryUpdateRunnable, 100);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BATTERY LEVEL STUFF
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // periodically raise battery update event
    private float batteryLevel = 0.0f;
    private Handler batteryUpdateHandler = new Handler();
    private Runnable batteryUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            batteryLevel = (batteryLevel + 0.01f) % 1.0f;

            for (CatBatteryListener listener : batteryListeners) {
                listener.UpdateBatteryLevel(batteryLevel);
            }

            // call me again in 100 ms
            batteryUpdateHandler.postDelayed(this, 100);
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SENDING DATA TO CAT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void UpdateObjectPosition(int relX, int relY) {
        Log.i(TAG, "Sending relative coordinates to cat: x = " + relX + ", y = " + relY);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Mock cat movements
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void stepForward() {
        Log.i(TAG, "mock cat stepping forward.");
    }
    @Override
    public void stepBackward() {
        Log.i(TAG, "mock cat stepping backward.");
    }
    @Override
    public void stepLeft() {
        Log.i(TAG, "mock cat stepping left.");
    }
    @Override
    public void stepRight() {
        Log.i(TAG, "mock cat stepping right.");
    }
    @Override
    public void turnHeadDown() {
        Log.i(TAG, "mock cat turning head down.");
    }
    @Override
    public void turnHeadLeft() {
        Log.i(TAG, "mock cat turning head left.");
    }
    @Override
    public void turnHeadRight() {
        Log.i(TAG, "mock cat turning head right.");
    }
    @Override
    public void turnHeadUp() {
        Log.i(TAG, "mock cat turning head up.");
    }

    @Override
    public void lookToward(Point relPos) {}
    @Override
    public void lookAwayFrom(Point relPos) {}

    @Override
    public void resetHead() {}
}
