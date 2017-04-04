package com.robodoot.roboapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.util.Log;

import com.robodoot.dr.RoboApp.PololuHandler;

//  -- OPENCVRMV
// import org.opencv.core.Point;

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
    }

    public void onResume(Intent intent, Activity parent) {
        p.onResume(intent, parent);
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
        p.addToPitch(PololuHandler.NECK_PITCH_SERVO_RANGE);
    }

    @Override
    public void turnHeadLeft() {
        //p.cameraYawSpeed(0.3f);
        p.addToYaw(PololuHandler.NECK_YAW_SERVO_RANGE);
    }

    @Override
    public void turnHeadRight() {
        p.addToYaw((int)(-PololuHandler.NECK_YAW_SERVO_RANGE * 0.9f));
        //p.cameraYawSpeed(-0.3f);
    }

    @Override
    public void turnHeadUp() {
        //p.cameraPitchSpeed(-0.3f);
        p.addToPitch(-PololuHandler.NECK_PITCH_SERVO_RANGE);
    }


    public void lookToward(PointF relfPos) {
        relfPos.x = Util.clamp(relfPos.x, -50.0f, 50.0f);
        relfPos.y = Util.clamp(relfPos.y, -50.0f, 50.0f);

        int yaw = (int)(relfPos.x);
        p.addToYaw(yaw);

        int pitch = (int)(relfPos.y);
        p.addToPitch(pitch);
    }

    /**
     * Make the cat look away from a point relative to the center of the camera's view.
     *//* -- OPENCVRMV
    @Override
    public void lookAwayFrom(Point relPos) {
        // maybe should do this
        relPos.x = Util.clamp(relPos.x, -0.5, 0.5);
        relPos.y = Util.clamp(relPos.y, -0.5, 0.5);

        Point p = new Point();
        p.x = -(relPos.x > 0 ? 0.5f - relPos.x : -0.5f + relPos.x);
        p.y = -(relPos.y > 0 ? 0.5f - relPos.y : -0.5f + relPos.y);
        lookToward(p);
    }*/

    public void resetHead() {
        p.home();
    }
}
