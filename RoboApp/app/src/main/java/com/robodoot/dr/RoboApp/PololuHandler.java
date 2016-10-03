package com.robodoot.dr.RoboApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.robodoot.roboapp.Util;

import org.pololu.maestro.*;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Vector;

/**
 * A low-level (compared to PololuVirtualCat) class for interacting with the Pololu. Uses a
 * MaestroSSC object to send commands.
 */
public class PololuHandler {

    public MaestroSSC maestro;

    public static final int NECK_YAW_SERVO = 10;
    public static final int NECK_YAW_SERVO_HOME = 1600;
    public static final int NECK_YAW_SERVO_RANGE = 1400;
    public static final int NECK_PITCH_SERVO = 9;
    public static final int NECK_PITCH_SERVO_HOME = 2200;
    public static final int NECK_PITCH_SERVO_RANGE = 1000;
    public static final int NECK_YAW_SERVO_MIN = NECK_YAW_SERVO_HOME - NECK_YAW_SERVO_RANGE / 2;
    public static final int NECK_PITCH_SERVO_MIN = NECK_PITCH_SERVO_HOME - NECK_PITCH_SERVO_RANGE / 2;
    public static final int NECK_YAW_SERVO_MAX = NECK_YAW_SERVO_HOME + NECK_YAW_SERVO_RANGE / 2;
    public static final int NECK_PITCH_SERVO_MAX = NECK_PITCH_SERVO_HOME + NECK_PITCH_SERVO_RANGE / 2;
    private boolean isConnected=false;
    private boolean isWalking = false;

        public enum Motor {
            HEAD_YAW(NECK_PITCH_SERVO, "Head Yaw", NECK_YAW_SERVO_HOME, 0, 0),
            HEAD_PITCH(NECK_YAW_SERVO, "Head Pitch", NECK_PITCH_SERVO_HOME, 0, 0);

            public final int number;
            public final int homePos;
            public final int min;
            public final int max;
            public final String name;

            Motor(int num, String str, int h, int mi, int ma) {
                number = num;
                name = str;
                homePos = h;
                min = mi;
                max = ma;

            }
        }

        public float speedConst = 90f;

        public int yaw = NECK_YAW_SERVO_HOME;
        public int pitch = NECK_PITCH_SERVO_HOME;

    public void setTarget(int ID, int target) {
        if (isConnected) {
            maestro.setTarget(ID, target);
        }
    }

    public PololuHandler() {
        maestro = new MaestroSSC();
        yaw = NECK_YAW_SERVO_HOME;
    }

    public boolean isOpen() {
        return isConnected;
    }

    public void home() {
        maestro.setTarget(NECK_PITCH_SERVO, NECK_PITCH_SERVO_HOME);
        maestro.setTarget(NECK_YAW_SERVO, NECK_YAW_SERVO_HOME);
        yaw = NECK_YAW_SERVO_HOME;
        pitch = NECK_PITCH_SERVO_HOME;
        int[] a52 = {170, 12, 31, 4, 12, 104, 45, 104, 50, 104, 62, 104, 62};
        maestro.explicitSend(getBytes(a52));
        int[] a53 = {170, 12, 31, 3, 5, 104, 38, 104, 48, 104, 32};
        maestro.explicitSend(getBytes(a53));
    }

    public void setSpeedConst(float newConst) {
        speedConst = newConst;
    }

    public void onResume(Intent intent, Activity parent) {
        String action = intent.getAction();
        Log.d("POLOLU HANDLER", "action: " + action);
        Log.d("POLOLU", intent.toString());
        if(isConnected) return;
        isConnected = false;
        Log.w("POLOLUHANDLER", "IN ON RESUME");
        if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbManager usbManager = (UsbManager) parent.getSystemService(Context.USB_SERVICE);
                maestro.setDevice(usbManager, device);
                isConnected = true;
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                maestro.setDevice(null, null);
            }/* else {
            }*/
        }
    }

    public void stopNeckMotors() {
//        maestro.setSpeed(NECK_YAW_SERVO,0);
//        maestro.setSpeed(NECK_PITCH_SERVO,0);
    }

    public void setYaw(int y) {
        yaw = y;
        maestro.setTarget(NECK_YAW_SERVO, yaw);
    }

    public void setPitch(int p) {
        pitch = p;
        maestro.setTarget(NECK_PITCH_SERVO, pitch);
    }

    public void addToYaw(int y) {
        yaw = Util.clamp(yaw + y, NECK_YAW_SERVO_MIN, NECK_YAW_SERVO_MAX);

        //yaw += y;
        maestro.setTarget(NECK_YAW_SERVO, yaw);
    }

    public void addToPitch(int p) {
        pitch = Util.clamp(pitch + p, NECK_PITCH_SERVO_MIN, NECK_PITCH_SERVO_MAX);
        //pitch += p;
        maestro.setTarget(NECK_PITCH_SERVO, pitch);
    }

    public void cameraYawSpeed(float speedPercent) {
        int addToYaw = (int) (speedPercent * speedConst);
        yaw += addToYaw;
        maestro.setTarget(NECK_YAW_SERVO, yaw);
    }

    public void cameraPitchSpeed(float speedPercent) {
        int addToPitch = (int) (speedPercent * speedConst);
        pitch += addToPitch;
        maestro.setTarget(NECK_PITCH_SERVO, pitch);
    }

    private byte[] getBytes(int[] a) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(a.length * 4);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(a);

        byte[] array = byteBuffer.array();

        byte[] s = new byte[array.length / 4];

        for (int i = 3; i < array.length; i += 4) {
            s[i / 4] = (array[i]);
        }
        return s;
    }

    private class StepForwardTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... v) {
            isWalking = true;
            int[] a0 = {170, 12, 31, 3, 1, 104, 42, 104, 50, 104, 62};
            maestro.explicitSend(getBytes(a0));
            int[] a1 = {170, 12, 31, 3, 16, 104, 44, 104, 47, 104, 32};
            maestro.explicitSend(getBytes(a1));
            int[] a2 = {170, 12, 31, 3, 1, 104, 42, 104, 46, 104, 62};
            maestro.explicitSend(getBytes(a2));
            int[] a3 = {170, 12, 31, 3, 16, 104, 44, 104, 48, 104, 32};
            maestro.explicitSend(getBytes(a3));
            int[] a4 = {170, 12, 31, 3, 1, 104, 42, 104, 41, 104, 62};
            maestro.explicitSend(getBytes(a4));
            int[] a5 = {170, 12, 31, 3, 16, 104, 44, 104, 50, 104, 32};
            maestro.explicitSend(getBytes(a5));
            int[] a6 = {170, 12, 31, 3, 1, 104, 42, 104, 32, 104, 62};
            maestro.explicitSend(getBytes(a6));
            int[] a7 = {170, 12, 31, 3, 16, 104, 44, 104, 53, 104, 32};
            maestro.explicitSend(getBytes(a7));
            int[] a8 = {170, 12, 31, 3, 1, 104, 42, 104, 24, 104, 62};
            maestro.explicitSend(getBytes(a8));
            int[] a9 = {170, 12, 31, 3, 16, 104, 44, 104, 57, 104, 32};
            maestro.explicitSend(getBytes(a9));
            int[] a10 = {170, 12, 31, 3, 1, 104, 42, 104, 19, 104, 62};
            maestro.explicitSend(getBytes(a10));
            int[] a11 = {170, 12, 31, 3, 16, 104, 44, 104, 59, 104, 32};
            maestro.explicitSend(getBytes(a11));
            int[] a12 = {170, 12, 31, 3, 1, 104, 42, 104, 19, 104, 62};
            maestro.explicitSend(getBytes(a12));
            int[] a13 = {170, 12, 31, 3, 16, 104, 44, 104, 59, 104, 32};
            maestro.explicitSend(getBytes(a13));
            int[] a14 = {170, 12, 31, 3, 1, 104, 46, 104, 19, 104, 63};
            maestro.explicitSend(getBytes(a14));
            int[] a15 = {170, 12, 31, 3, 16, 104, 41, 104, 59, 104, 31};
            maestro.explicitSend(getBytes(a15));
            int[] a16 = {170, 12, 31, 3, 1, 104, 48, 104, 19, 104, 64};
            maestro.explicitSend(getBytes(a16));
            int[] a17 = {170, 12, 31, 3, 16, 104, 39, 104, 59, 104, 31};
            maestro.explicitSend(getBytes(a17));
            int[] a18 = {170, 12, 31, 3, 1, 104, 51, 104, 19, 104, 63};
            maestro.explicitSend(getBytes(a18));
            int[] a19 = {170, 12, 31, 3, 16, 104, 36, 104, 59, 104, 31};
            maestro.explicitSend(getBytes(a19));
            int[] a20 = {170, 12, 31, 3, 1, 104, 53, 104, 19, 104, 63};
            maestro.explicitSend(getBytes(a20));
            int[] a21 = {170, 12, 31, 3, 16, 104, 34, 104, 59, 104, 31};
            maestro.explicitSend(getBytes(a21));
            int[] a22 = {170, 12, 31, 3, 1, 104, 51, 104, 32, 104, 63};
            maestro.explicitSend(getBytes(a22));
            int[] a23 = {170, 12, 31, 3, 16, 104, 36, 104, 53, 104, 31};
            maestro.explicitSend(getBytes(a23));
            int[] a24 = {170, 12, 31, 3, 1, 104, 48, 104, 41, 104, 64};
            maestro.explicitSend(getBytes(a24));
            int[] a25 = {170, 12, 31, 3, 16, 104, 39, 104, 50, 104, 31};
            maestro.explicitSend(getBytes(a25));
            int[] a26 = {170, 12, 31, 3, 1, 104, 42, 104, 50, 104, 62};
            maestro.explicitSend(getBytes(a26));
            int[] a27 = {170, 12, 31, 3, 16, 104, 44, 104, 47, 104, 32};
            maestro.explicitSend(getBytes(a27));
            int[] a28 = {170, 12, 31, 4, 12, 104, 45, 104, 50, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a28));
            int[] a29 = {170, 12, 31, 3, 5, 104, 38, 104, 48, 104, 32};
            maestro.explicitSend(getBytes(a29));
            int[] a30 = {170, 12, 31, 4, 12, 104, 45, 104, 46, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a30));
            int[] a31 = {170, 12, 31, 3, 5, 104, 38, 104, 52, 104, 32};
            maestro.explicitSend(getBytes(a31));
            int[] a32 = {170, 12, 31, 4, 12, 104, 45, 104, 41, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a32));
            int[] a33 = {170, 12, 31, 3, 5, 104, 38, 104, 56, 104, 32};
            maestro.explicitSend(getBytes(a33));
            int[] a34 = {170, 12, 31, 4, 12, 104, 45, 104, 32, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a34));
            int[] a35 = {170, 12, 31, 3, 5, 104, 38, 104, 64, 104, 32};
            maestro.explicitSend(getBytes(a35));
            int[] a36 = {170, 12, 31, 4, 12, 104, 45, 104, 19, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a36));
            int[] a37 = {170, 12, 31, 3, 5, 104, 38, 104, 75, 104, 32};
            maestro.explicitSend(getBytes(a37));
            int[] a38 = {170, 12, 31, 4, 12, 104, 45, 104, 19, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a38));
            int[] a39 = {170, 12, 31, 3, 5, 104, 38, 104, 75, 104, 32};
            maestro.explicitSend(getBytes(a39));
            int[] a40 = {170, 12, 31, 4, 12, 104, 48, 104, 19, 104, 63, 104, 63};
            maestro.explicitSend(getBytes(a40));
            int[] a41 = {170, 12, 31, 3, 5, 104, 34, 104, 75, 104, 31};
            maestro.explicitSend(getBytes(a41));
            int[] a42 = {170, 12, 31, 4, 12, 104, 50, 104, 19, 104, 64, 104, 64};
            maestro.explicitSend(getBytes(a42));
            int[] a43 = {170, 12, 31, 3, 5, 104, 33, 104, 75, 104, 31};
            maestro.explicitSend(getBytes(a43));
            int[] a44 = {170, 12, 31, 4, 12, 104, 53, 104, 19, 104, 63, 104, 63};
            maestro.explicitSend(getBytes(a44));
            int[] a45 = {170, 12, 31, 3, 5, 104, 29, 104, 75, 104, 31};
            maestro.explicitSend(getBytes(a45));
            int[] a46 = {170, 12, 31, 4, 12, 104, 55, 104, 19, 104, 63, 104, 63};
            maestro.explicitSend(getBytes(a46));
            int[] a47 = {170, 12, 31, 3, 5, 104, 28, 104, 75, 104, 31};
            maestro.explicitSend(getBytes(a47));
            int[] a48 = {170, 12, 31, 4, 12, 104, 53, 104, 32, 104, 63, 104, 63};
            maestro.explicitSend(getBytes(a48));
            int[] a49 = {170, 12, 31, 3, 5, 104, 29, 104, 64, 104, 31};
            maestro.explicitSend(getBytes(a49));
            int[] a50 = {170, 12, 31, 4, 12, 104, 50, 104, 41, 104, 64, 104, 64};
            maestro.explicitSend(getBytes(a50));
            int[] a51 = {170, 12, 31, 3, 5, 104, 33, 104, 56, 104, 31};
            maestro.explicitSend(getBytes(a51));
            int[] a52 = {170, 12, 31, 4, 12, 104, 45, 104, 50, 104, 62, 104, 62};
            maestro.explicitSend(getBytes(a52));
            int[] a53 = {170, 12, 31, 3, 5, 104, 38, 104, 48, 104, 32};
            maestro.explicitSend(getBytes(a53));
            isWalking = false;
            return null;
        }

        protected void onProgressUpdate() {
        }

        protected void onPostExecute() {
        }
    }


    public void stepForward() {
        if (isWalking) {
            return;
        }
        else {
            new StepForwardTask().execute();
        }

    }

}
