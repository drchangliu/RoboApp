package com.robodoot.roboapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.robodoot.dr.RoboApp.PololuHandler;

import org.opencv.core.Point;

/**
 * A concrete implementation of the VirtualCat interface which controls the cat by communicating
 * only with the Pololu (no Arduino).
 */
public class PololuVirtualCat extends VirtualCat {
    private static final String TAG = "PololuVirtualCat";

    static final int TURN_SPEED = 5;

    public static PololuHandler p;

    // DEFAULT CONSTRUCTOR
    public PololuVirtualCat() {
        p = new PololuHandler();
    }

    public void UpdateObjectPosition(int relX, int relY) {
        Log.i(TAG, "UpdateObjectPosition: not implemented");
    }

    public void onResume(Intent intent, Activity parent) {
        p.onResume(intent, parent);
        Log.w(TAG, "IN ONRESUME");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation of cat movements
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void stepForward() {
        p.stepForward();
    }

    @Override
    public void stepBackward() {
        //p.setBackward();
    }

    @Override
    public void stepLeft() {
        //p.stepLeft();
    }

    @Override
    public void stepRight() {
        //p.stepRight();
    }

    @Override
    public void turnHeadDown() {
        //p.cameraPitchSpeed(-0.3f);
        p.addToPitch((int)(p.NECK_PITCH_SERVO_RANGE));
    }

    @Override
    public void turnHeadLeft() {
        //p.cameraYawSpeed(0.3f);
        p.addToYaw((int)(p.NECK_YAW_SERVO_RANGE));
    }

    @Override
    public void turnHeadRight() {
        p.addToYaw((int)(-p.NECK_YAW_SERVO_RANGE * 0.9f));
        //p.cameraYawSpeed(-0.3f);
    }

    @Override
    public void turnHeadUp() {
        //p.cameraPitchSpeed(-0.3f);
        p.addToPitch((int)(-p.NECK_PITCH_SERVO_RANGE));
    }

    /**
     * Make the cat look toward a point relative to the center of the camera's view.
     * @param relPos Expects a value in the range [-0.5, 0.5]
     */
    @Override
    public void lookToward(Point relPos) {
        // maybe should do this
        relPos.x = Util.clamp(relPos.x, -0.5, 0.5);
        relPos.y = Util.clamp(relPos.y, -0.5, 0.5);

        // maybe something like this
        int yaw = (int)(relPos.x * p.NECK_YAW_SERVO_RANGE / 8.0f);
        //int yaw = (int)(relPos.x * p.NECK_YAW_SERVO_MAX * 0.1f);
        if (Math.abs(yaw) >= 25) {
            p.addToYaw(yaw);
        }
        int pitch = (int)(relPos.y * p.NECK_PITCH_SERVO_MAX / 10.0f);
        //int pitch = (int) (relPos.y * p.NECK_PITCH_SERVO_MAX * 0.075f);
        if (Math.abs(pitch) >= 25) {
            p.addToPitch(pitch);
        }
    }

    /**
     * Make the cat look away from a point relative to the center of the camera's view.
     * @param relPos Expects a value in the range [-0.5, 0.5]
     */
    @Override
    public void lookAwayFrom(Point relPos) {
        // maybe should do this
        relPos.x = Util.clamp(relPos.x, -0.5, 0.5);
        relPos.y = Util.clamp(relPos.y, -0.5, 0.5);

        Point p = new Point();
        p.x = -(relPos.x > 0 ? 0.5f - relPos.x : -0.5f + relPos.x);
        p.y = -(relPos.y > 0 ? 0.5f - relPos.y : -0.5f + relPos.y);
        lookToward(p);
    }

    public void resetHead() {
        p.home();
        Log.i(TAG, "RESETTING HEAD");
    }
}
