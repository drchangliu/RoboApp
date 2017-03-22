package com.robodoot.roboapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.robodoot.dr.facetracktest.R;

import yuku.ambilwarna.AmbilWarnaDialog;

public class ColorTrackingActivity extends AppCompatActivity {

    private Camera myCamera = null;
    private ColorTrackingCamera myPreview;
    private BitmapFactory.Options options=new BitmapFactory.Options();
    private boolean sizeChecked = false;
    private boolean isTracking = false;
    static int ColorToTrack = Color.RED;
    static int colorDistance = 70;
    Button selectColorButton;
    Button startStopButton;
    TextView colorLocation;
    FrameLayout preview = null;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onPause() {
        super.onPause();
        preview.removeAllViews();
        myPreview = null;
        myCamera.release();
        isTracking = false;
        colorLocation.setText("...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        myCamera = getCameraInstance();
        myPreview = new ColorTrackingCamera(this, myCamera);
        myCamera.setDisplayOrientation(90);
        preview.addView(myPreview);
        myCamera.startPreview();
        colorLocation.setText("...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preview.removeAllViews();
        myPreview = null;
        myCamera.release();
        isTracking = false;
        colorLocation.setText("...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_tracking);

        int numCams = Camera.getNumberOfCameras();

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        if(checkCameraHardware(getApplicationContext())){
            myCamera = getCameraInstance();
        }

        if(myCamera != null){
            // Create our Preview view and set it as the content of our activity.
            myPreview = new ColorTrackingCamera(this, myCamera);
            myCamera.setDisplayOrientation(90);
            preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(myPreview);
            myCamera.startPreview();

            final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, ColorToTrack, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    // color is the color selected by the user.
                    ColorToTrack = color;
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // cancel was selected by the user
                }
            });

            colorLocation = (TextView) findViewById(R.id.color_location);

            selectColorButton = (Button) findViewById(R.id.select_color);
            selectColorButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.show();
                        }
                    }
            );

            Button startStopButton = (Button) findViewById(R.id.start_stop);
            startStopButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // get an image from the camera
                            if(!isTracking){
                                isTracking = true;
                                myCamera.takePicture(null, null, myPicture);
                            }
                            else {
                                isTracking = false;
                                colorLocation.setText("...");
                            }
                        }
                    }
            );

            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setProgress(colorDistance);
            seekBar.setMax(150);
            final TextView seekBarValue = (TextView) findViewById(R.id.textView);
            seekBarValue.setText(String.valueOf(colorDistance));

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    seekBarValue.setText(String.valueOf(progress));
                    colorDistance = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(), "No camera found.", Toast.LENGTH_LONG).show();
        }
    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width less than or equal to the requested height and width.
            while ((height / inSampleSize) >= reqHeight || (width / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.d("Camera Error", e.getMessage());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void captureImage(){
        try{
            myCamera.takePicture(null, null, myPicture);
        }
        catch (Exception ex){
            captureImage();
        }
    }

    private Camera.PictureCallback myPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if(isTracking){
                if(!sizeChecked) {
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, options);
                    options.inSampleSize = calculateInSampleSize(options, 160, 90);
                    options.inJustDecodeBounds = false;
                    sizeChecked = true;
                }

                Bitmap imageBitmap = BitmapFactory.decodeByteArray(data , 0, data.length, options);
                new ColorFinder(new ColorFinder.CallbackInterface() {
                    @Override
                    public void onCompleted(String color) {
                        colorLocation.setText(color);
                    }
                }).findDominantColor(imageBitmap, false);
                myCamera.startPreview();
                myCamera.takePicture(null, null, myPicture);
            }
            else{
                colorLocation.setText("...");
            }
        }
    };

}
