package com.robodoot.dr.RoboApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.robodoot.dr.facetracktest.R;
import com.robodoot.roboapp.BatteryView;
import com.robodoot.roboapp.CatCameraView;
import com.robodoot.roboapp.ColorValues;
import com.robodoot.roboapp.VirtualCat;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * This activity can be used to determine the min/max HSV color values to use for color tracking,
 * and store them on the external storage of the device.
 */
public class ColorTrackingActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2, VirtualCat.CatBatteryListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "ColorTrackingActivity";

    private CatCameraView mOpenCvCameraView;
    private BatteryView mBatteryView;
    private SeekBar mSeekBarLowH, mSeekBarHighH, mSeekBarLowS, mSeekBarHighS, mSeekBarLowV, mSeekBarHighV;

    private Mat mRgba;
    private Mat mGray;

    private boolean mShowThreshold;

    private Logger mRedLogger;
    private Logger mGreenLogger;
    private Logger mBlueLogger;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_color_tracking);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mOpenCvCameraView = (CatCameraView) findViewById(R.id.color_tracking_camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setAlpha(1.0f);
        mOpenCvCameraView.bringToFront();
        //mOpenCvCameraView.setRotation(90.0f);

        ((Switch)findViewById(R.id.switch_threshold)).setOnCheckedChangeListener(this);
        ((Switch)findViewById(R.id.switch_camera)).setOnCheckedChangeListener(this);
        //((Switch)findViewById(R.id.switch_battery)).setOnCheckedChangeListener(this);;

        /*mBatteryView = (BatteryView) findViewById(R.id.battery_view);
        mBatteryView.setConnected(true);
        mBatteryView.setAlpha(0.0f); // hide initially*/

        mSeekBarLowH = (SeekBar) findViewById(R.id.seek_bar_low_h);
        mSeekBarHighH = (SeekBar) findViewById(R.id.seek_bar_high_h);
        mSeekBarLowS = (SeekBar) findViewById(R.id.seek_bar_low_s);
        mSeekBarHighS = (SeekBar) findViewById(R.id.seek_bar_high_s);
        mSeekBarLowV = (SeekBar) findViewById(R.id.seek_bar_low_v);
        mSeekBarHighV = (SeekBar) findViewById(R.id.seek_bar_high_v);

        mRedLogger = new Logger("red_color_values", false);
        mGreenLogger = new Logger("green_color_values", false);
        mBlueLogger = new Logger("blue_ color_values", false);

        String[] lines = new Logger("green_color_values", false).ReadLines();
        if (lines != null) {
            ColorValues cv = new ColorValues(lines[0]);
            mSeekBarLowH.setProgress(cv.lowH);
            mSeekBarHighH.setProgress(cv.highH);
            mSeekBarLowS.setProgress(cv.lowS);
            mSeekBarHighS.setProgress(cv.highS);
            mSeekBarLowV.setProgress(cv.lowV);
            mSeekBarHighV.setProgress(cv.highV);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        mOpenCvCameraView.setCameraIndex(0);
                        //mOpenCvCameraView.enableFpsMeter();
                        mOpenCvCameraView.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.switch_camera:
                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setCameraIndex(isChecked ? 1 : 0);
                mOpenCvCameraView.enableView();
                break;
            case R.id.switch_threshold:
                mShowThreshold = isChecked;
                break;
            /*case R.id.switch_battery:
                mBatteryView.setAlpha(isChecked ? 1.0f : 0.0f);
                break;*/
        }
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC4);
    }

    // it is probably possible to rewrite this stuff so it uses 100% javacv code.
    // it would be faster overall if if the javacv functions are at least as fast as the opencv ones
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat imgThresholded = null;
        try {
            inputFrame.rgba().copyTo(mRgba);

            // resize to transpose dimensions because it's stupid. not necessary with opencv 3.1.0
            Imgproc.resize(mRgba, mRgba, mRgba.t().size());

            int iLowH = mSeekBarLowH.getProgress();
            int iHighH = mSeekBarHighH.getProgress();

            int iLowS = mSeekBarLowS.getProgress();
            int iHighS = mSeekBarHighS.getProgress();

            int iLowV = mSeekBarLowV.getProgress();
            int iHighV = mSeekBarHighV.getProgress();

            imgThresholded = new Mat();
            Imgproc.cvtColor(mRgba, imgThresholded, Imgproc.COLOR_RGB2HSV); //Convert the captured frame from BGR to HSV

            Core.inRange(imgThresholded, new Scalar(iLowH, iLowS, iLowV), new Scalar(iHighH, iHighS, iHighV), imgThresholded); //Threshold the image

            // morphological opening (removes small objects from the foreground)
            Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
            Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

            // morphological closing (removes small holes from the foreground)
            Imgproc.dilate(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
            Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));

            // Calculate the moments of the thresholded image
            // the moments stuff is missing from opencv 3.0.0 so we have to use javacv for this.
            // get the mat data from imgThresholded as a byte array
            int length = (int) (imgThresholded.total() * imgThresholded.elemSize());
            byte buffer[] = new byte[length];
            imgThresholded.get(0, 0, buffer);

            // construct a javacv mat from the byte array
            opencv_core.Mat momentsMat = new opencv_core.Mat(imgThresholded.height(), imgThresholded.width(), imgThresholded.type());
            momentsMat.data().put(buffer);

            // compute moments
            opencv_core.Moments oMoments = opencv_imgproc.moments(momentsMat);

            double dM01 = oMoments.m01();
            double dM10 = oMoments.m10();
            double dArea = oMoments.m00();

            momentsMat.release();

            // if area <= 100000, considered to be noise
            if (dArea > 100000) {
                //calculate the position of the object
                double posX = dM10 / dArea;
                double posY = dM01 / dArea;

                if (posX >= 0 && posY >= 0) {
                    // draw a red line from the previous point to the current point
                    //Imgproc.line(mRgba, new Point(posX, posY), new Point(posX, posY), new Scalar(0, 0, 255), 2);

                    // draw rectangle at point
                    Imgproc.rectangle(mRgba, new Point(posX, posY), new Point(posX + 20, posY + 20), new Scalar(255, 255, 255), 5);
                    //Imgproc.rectangle(mRgba, new Point(0, 0), new Point(20, 20), new Scalar(255, 255, 255), 5);

                    // compute relative position of the object
                    double relativeX = posX - (mRgba.width() / 2.0f);
                    double relativeY = posY - (mRgba.height() / 2.0f);

                    Log.i(TAG, "I SEE AN OBJECT");
                }
            }

        } catch (Exception e) {
            Log.i(TAG, "Exception " + e.getMessage());
        }

        if (imgThresholded != null) {
            // if showThreshold enabled, copy it into mRgba for display
            if (mShowThreshold) {
                imgThresholded.copyTo(mRgba);
            }

            // free imgThresholded resources
            imgThresholded.release();
        }

        // transpose and flip, not necessary for now
        //Core.flip(mRgba.t(), mRgba, 0);

        // return the mat to be displayed
        return mRgba;
    }

    public void onCameraViewStopped() {
        // this doesn't seem like it should be necessary but better safe than sorry.
        // the mat objects themselves only contain a pointer to the pixel data, so
        // when you copy a mat it still points to the same data as the original.
        // i believe the opencv library handles releasing each frame
        mGray.release();
        mRgba.release();
    }

    @Override
    public void UpdateBatteryLevel(float level) {
        mBatteryView.setCharge(level);
    }

    public void setColor(View view) {
        final int button_id = view.getId();
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Saving color values")
            .setMessage("Are you sure you want to save these color values?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                String s = mSeekBarLowH.getProgress() + " " + mSeekBarHighH.getProgress()
                        + " " + mSeekBarLowS.getProgress() + " " + mSeekBarHighS.getProgress()
                        + " " + mSeekBarLowV.getProgress() + " " + mSeekBarHighV.getProgress();
                switch (button_id) {
                    case R.id.button_save_red:
                        Log.i(TAG, "set red");
                        mRedLogger.addRecordToLog(s);
                        break;
                    case R.id.button_save_green:
                        Log.i(TAG, "set green");
                        mGreenLogger.addRecordToLog(s);
                        break;
                    case R.id.button_save_blue:
                        Log.i(TAG, "set blue");
                        mBlueLogger.addRecordToLog(s);
                        break;
                }
                    ;
                }

            })
            .setNegativeButton("No", null)
            .show();
    }

    public void loadColor(View view) {
        final int button_id = view.getId();
        String[] lines = null;
        switch (button_id) {
            case R.id.button_load_red:
                Log.i(TAG, "load red");
                lines = new Logger("red_color_values", false).ReadLines();
                break;
            case R.id.button_load_green:
                Log.i(TAG, "load green");
                lines = new Logger("green_color_values", false).ReadLines();
                break;
            case R.id.button_load_blue:
                Log.i(TAG, "load blue");
                lines = new Logger("blue_color_values", false).ReadLines();
                break;
        }

        if (lines == null)
            return;

        ColorValues cv = new ColorValues(lines[0]);

        mSeekBarLowH.setProgress(cv.lowH);
        mSeekBarHighH.setProgress(cv.highH);
        mSeekBarLowS.setProgress(cv.lowS);
        mSeekBarHighS.setProgress(cv.highS);
        mSeekBarLowV.setProgress(cv.lowV);
        mSeekBarHighV.setProgress(cv.highV);
    }
}
