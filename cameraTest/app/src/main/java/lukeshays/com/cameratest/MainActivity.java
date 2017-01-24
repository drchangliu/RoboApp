package lukeshays.com.cameratest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Camera myCamera = null;
    private CameraPreview myPreview;
    private BitmapFactory.Options options=new BitmapFactory.Options();
    private boolean sizeChecked = false;
    static boolean Red = true;
    static boolean Green = false;
    static boolean Blue = false;
    static int colorDistance = 75;
    Button captureButtonGreen;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    /*@Override
    protected void onPause() {
        super.onPause();
        myCamera.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myCamera.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        myCamera = getCameraInstance();
    }*/

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
        setContentView(R.layout.activity_main);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }

        if(checkCameraHardware(getApplicationContext())){
            myCamera = getCameraInstance();
            if(myCamera != null) {
                boolean duh = true;
            }
        }
        if(myCamera != null){
            // Create our Preview view and set it as the content of our activity.
            myPreview = new CameraPreview(this, myCamera);
            myCamera.setDisplayOrientation(90);
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(myPreview);

            Button captureButtonRed = (Button) findViewById(R.id.button_capture_red);
            captureButtonRed.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Blue = false;
                            Red = true;
                            Green = false;
                            // get an image from the camera
                            myCamera.takePicture(null, null, mPicture);
                        }
                    }
            );

            captureButtonGreen = (Button) findViewById(R.id.button_capture_green);
            captureButtonGreen.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Blue = false;
                            Red = false;
                            Green = true;
                            // get an image from the camera
                            myCamera.takePicture(null, null, mPicture);
                        }
                    }
            );

            Button captureButtonBlue = (Button) findViewById(R.id.button_capture_blue);
            captureButtonBlue.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Blue = true;
                            Red = false;
                            Green = false;
                            // get an image from the camera
                            myCamera.takePicture(null, null, mPicture);
                        }
                    }
            );

            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
            seekBar.setProgress(75);
            seekBar.setMax(150);
            final TextView seekBarValue = (TextView) findViewById(R.id.textView);
            seekBarValue.setText("75");

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

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width less than or equal to the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
            if(halfHeight / inSampleSize > reqHeight || halfWidth / inSampleSize > reqWidth){
                inSampleSize *=2;
            }
        }

        return inSampleSize;
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

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
                    captureButtonGreen.setText(color);
                }
            }).findDominantColor(imageBitmap);
            myCamera.startPreview();
            myCamera.takePicture(null, null, mPicture);
        }
    };

}
