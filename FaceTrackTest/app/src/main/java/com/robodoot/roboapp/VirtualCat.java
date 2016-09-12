package com.robodoot.roboapp;

import android.app.Activity;
import android.content.Intent;

import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class which specifies a common interface for all "virtual cat" classes. All
 * activities can be written to operate on a VirtualCat, and the actual implementation of the
 * cat behavior will be provided by some concrete class that inherits from VirtualCat.
 */
public abstract class VirtualCat {
    // interface for anyone who wants battery level updates
    public interface CatBatteryListener {
        void UpdateBatteryLevel(float level);
    }

    public void onResume(Intent intent, Activity parent) {}

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // BATTERY LEVEL STUFF
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // battery event listeners
    protected List<CatBatteryListener> batteryListeners = new ArrayList<CatBatteryListener>();

    public void AddBatteryListener(CatBatteryListener listener) {
        batteryListeners.add(listener);
    }

    public abstract void UpdateObjectPosition(int relX, int relY);

    public abstract void stepForward();

    public abstract void stepBackward();

    public abstract void stepLeft();

    public abstract void stepRight();

    public abstract void turnHeadLeft();

    public abstract void turnHeadRight();

    public abstract void turnHeadUp();

    public abstract void turnHeadDown();

    public abstract void lookToward(Point relPos);

    public abstract void lookAwayFrom(Point relPos);

    public abstract void resetHead();
}
