package com.robodoot.dr.RoboApp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.robodoot.dr.RoboApp.camera.CameraSourcePreview;
import com.robodoot.dr.RoboApp.camera.GraphicOverlay;
import com.robodoot.dr.facetracktest.R;
import com.robodoot.roboapp.ColorFinder;
import com.robodoot.roboapp.ColorTrackingActivity;
import com.robodoot.roboapp.ColorTrackingCamera;
import com.robodoot.roboapp.Direction;
import com.robodoot.roboapp.MainActivity;
import com.robodoot.roboapp.PololuVirtualCat;
import com.robodoot.roboapp.VirtualCat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import static com.google.android.gms.vision.face.Landmark.LEFT_EYE;
import static com.google.android.gms.vision.face.Landmark.RIGHT_EYE;

// Psphx imports

/**
 * Behavior mode activity. This is the fragment_camera preview activity of the app.
 * uses built in hardware functions to use the Android accelerometer data (SensorEventListener)
 */

//TODO: command not found always shows when using continuous speech
    //TODO: OnResume method seems broken for speech
    //TODO: OnResume also not implemented for passive face tracking
    //TODO: OnDestroy methods needed for both.

    //TODO: literally the 2 view elements in the xml are the problem
public class FdActivity extends Activity implements
    GestureDetector.OnGestureListener, SensorEventListener, RecognitionListener {
    // FUNCTION AND VARIABLE DEFINITIONS
    private Logger mFaceRectLogger;
    private Logger mSpeechTextLogger;
    private boolean initialized = false;

    //new camera variables start
    private CameraSource mCameraSource = null;
    private CameraSourcePreview FTPreview;
    private GraphicOverlay FTGraphicOverlay;
    //new facetracker variables end

    //distance approximation variables start
    private double AverageHumanEyeSeperation = 6.3; //centimeters
    //distance approximation variables end
    // Start ColorTracking variables
    private Camera myCamera = null;
    // TODO: COLORTRACKING DISPLAY START
    FrameLayout preview = null;
    private ColorTrackingCamera myPreview;
    // TODO: COLORTRACKING DISPLAY END
    private BitmapFactory.Options options = new BitmapFactory.Options();
    private boolean sizeChecked = false;
    ToggleButton toggleColorTracking;
    ToggleButton toggleFTview;
    Camera.PreviewCallback previewCallback;
    // End ColorTracking variables


    FaceDetector detector = null;
    private int frameNumber;

    private boolean cameraIsChecked = false;

    /* psphx1: Variable declarations */

    // The pocketsphinx recognizer
    private SpeechRecognizer recognizer;
    private static final String KWS_SEARCH = "wakeup";
    // Keyword we are using to start command listening
    private static final String START_LISTENING_STRING = "okay robo cat";
    // Used when getting permission to listen

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    private static final int PERMISSIONS_REQUEST_INTERNET=4;//don't have to explicitly ask for some reaseon
    private static final int PERMISSIONS_REQUEST_ACCESS_NETWORK_STATE=5;//don't have to explicitly ask for some reaseon
    private static final int PERMISSIONS_REQUEST_MULTIPLE=6;
    private static final String CAT_COMMANDS = "cat";

    /* End pocketsphinx variable declarations */

    private final int REQ_CODE_SPEECH_INPUT=100;
    private ArrayList<String> result;

    private GestureDetector gDetector;
    public enum CHAR {U, D, L, R}
    private static Vector<CHAR> psswd = new Vector<>();
    public static Vector<CHAR> entry = new Vector<>();

    private static final String TAG = "FdActivity";
    private static final int JAVA_DETECTOR = 0;

    private CatEmotion kitty;
    public enum Directions {UP, DOWN, LEFT, RIGHT, CENTER}
    private RelativeLayout frame;
    private Bitmap bmp;
    private Directions dir;


    private int IDcount;

    private int refreshRecognizer;

    private File mCascadeFile;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.1f;
    private int mAbsoluteFaceSize = 0;

    boolean[] filter;


    private double xCenter = -1;
    double yCenter = -1;

    //PololuHandler pololu;
    VirtualCat virtualCat;
    //PololuVirtualCat virtualCat;

    private TextView debug1;
    TextView debug2;
    private TextView debug3;
    private TextView tempTextView;

    private String tempText;

    // private variables for accelerometer declaration
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    // array of arrays to store the accelerometer data in x, y, z format
    public static ArrayList<ArrayList<String>> accData = new ArrayList<ArrayList<String>>();

    // Function to open menu activity
    public void openMenu(){
        Intent intent = new Intent(this, MainActivity.class);
        // sending accelerometer data to the fragment_camera preview activity
        intent.putExtra("accData", accData);
        startActivity(intent);
    }

    private String imageCaptureDirectory;

    //TODO: Analytics Code
    private com.google.android.gms.analytics.Tracker mTracker;

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        ArrayList<ArrayList<Integer>> similarID = new ArrayList<ArrayList<Integer>>();
        virtualCat = new PololuVirtualCat();

        Log.i(TAG, "Instantiated new " + this.getClass());

        mFaceRectLogger = new Logger("face_rect_log");
        mSpeechTextLogger = new Logger("speech_text_log");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // ACTIVITY LIFECYCLE METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fd);
        FTPreview = (CameraSourcePreview) findViewById(R.id.FTpreview);
        FTGraphicOverlay = (GraphicOverlay) findViewById(R.id.FTFaceOverlay);
        toggleFTview = (ToggleButton) findViewById(R.id.FTtoggle);
        FTPreview.setVisibility(View.INVISIBLE);
        preview = (FrameLayout) findViewById(R.id.camera_preview1);
        preview.setVisibility(View.INVISIBLE);

        previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                // camera is "loaded" and first preview is sent to the screen
                // do whatever you want to do
                myCamera.takePicture(null,null,mPicture);
            }
        };

        toggleFTview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    if(toggleColorTracking.isChecked()) preview.setVisibility(View.VISIBLE);
                    else FTPreview.setVisibility(View.VISIBLE);
                }
                else
                {
                    FTPreview.setVisibility(View.INVISIBLE);
                    preview.setVisibility(View.INVISIBLE);
                }
            }
        });


        //TODO: Analytics Code
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        // initializing accelerometer variables and registering listener (listening for movement)
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //debugging = true;

        psswd.add(CHAR.U); psswd.add(CHAR.U);  psswd.add(CHAR.D); psswd.add(CHAR.D);
        psswd.add(CHAR.L);  psswd.add(CHAR.R);  psswd.add(CHAR.L);  psswd.add(CHAR.R);

        gDetector = new GestureDetector(getApplicationContext(), this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Menu button on click action
        Button btnMenu = (Button) findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openMenu();
            }
        });

        ImageView[] arrows = new ImageView[4];
        arrows[0]=(ImageView)findViewById(R.id.arrow_up);
        arrows[1]=(ImageView)findViewById(R.id.arrow_right);
        arrows[2]=(ImageView)findViewById(R.id.arrow_down);
        arrows[3]=(ImageView)findViewById(R.id.arrow_left);
        //for (int i = 0; i < 4; i++) arrows[i].setVisibility(View.INVISIBLE);

        //Begin new face Tracking stuff



        //End new face tracking stuff

       // mOpenCvCameraView = (JavaCameraView) findViewById(R.id.fd_activity_surface_view);
        //mOpenCvCameraView.setCvCameraViewListener(this);

        kitty = new CatEmotion(this);
        kitty.pic=(ImageView)findViewById(R.id.image_place_holder);

        // psphx1: Pocketsphinx creation stuff
        ((TextView) findViewById(R.id.caption_text))
                .setText("Loading voice recognition...");
        // Check if user has given permission to record audio

        List<String> PermissionListTmp = new ArrayList<String>();

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            PermissionListTmp.add(Manifest.permission.CAMERA);
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            createCameraSource();
            startCameraSource();
        }
        // Get permissions for RoboCat
        int voicePermissions = 0;
        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            PermissionListTmp.add(Manifest.permission.RECORD_AUDIO);
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            voicePermissions++;
        }
        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            PermissionListTmp.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            voicePermissions++;
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            PermissionListTmp.add(Manifest.permission.INTERNET);
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            //TODO: What do we do if we already have permission?
        }

        permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_NETWORK_STATE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            PermissionListTmp.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            //TODO: What do we do if we already have permission?
        }

        // If external storage & record audio are enabled
        if (voicePermissions == 2){
            runRecognizerSetup();
        }
        if(PermissionListTmp.size()>0){
            String[] PermissionList = new String[PermissionListTmp.size()];
            PermissionList = PermissionListTmp.toArray(PermissionList);
            ActivityCompat.requestPermissions(this, PermissionList, PERMISSIONS_REQUEST_MULTIPLE);
        }
        //End New Face Tracker Code

        // ColorTracking Button Stuff Below
        // Switch should toggle between colortracking and facetracking
        toggleColorTracking = (ToggleButton) findViewById(R.id.switch1);
        toggleColorTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    // End facetracking
                    if (mCameraSource != null) {
                        mCameraSource.release();
                    }
                    detector = null;
                    FTPreview.setVisibility(View.INVISIBLE);
                    // Start colortracking
                    if(toggleFTview.isChecked()) preview.setVisibility(View.VISIBLE);
                    myCamera = Camera.open(1);
                    myCamera.setDisplayOrientation(90);
                    myPreview = new ColorTrackingCamera(getApplicationContext(), myCamera, previewCallback);
                    preview.addView(myPreview);
                    myCamera.startPreview();
                }
                else{
                    //End colorTracking
                    preview.removeAllViews();
                    myPreview = null;
                    if(myCamera != null){
                        myCamera.release();
                    }
                    preview.setVisibility(View.INVISIBLE);
                    // Start facetracking
                    if(toggleFTview.isChecked()) FTPreview.setVisibility(View.VISIBLE);
                    createCameraSource();
                    startCameraSource();
                }
            }
        });
        // End ColorTracking Button stuff

    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop the recognizer
        if (recognizer != null) {
            recognizer.cancel();
        }
        frameNumber = 0;
        // for accelerometer, also need to stop listening on pause
        senSensorManager.unregisterListener(this);
        FTPreview.stop();
    }

    @Override
    public void onResume() {
        //TODO: Analytics Code
        mTracker.setScreenName("Image~" + "Behavior Mode");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        // Restart recognizer
        if(recognizer != null){
            if (recognizer.getSearchName().equals(KWS_SEARCH))
                recognizer.startListening(KWS_SEARCH);
            else
                recognizer.startListening(CAT_COMMANDS, 10000);
        }

        virtualCat.onResume(getIntent(), this);

        super.onResume();
        if (!initialized) {
            initialized = true;
            virtualCat.resetHead();
        }

        //TODO: remove this
        //toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

        if(toggleColorTracking.isChecked()){
            myCamera = Camera.open(1);
            myPreview = new ColorTrackingCamera(getApplicationContext(), myCamera, previewCallback);
            preview = (FrameLayout) findViewById(R.id.camera_preview1);
            myCamera.setDisplayOrientation(90);
            preview.addView(myPreview);
            myCamera.startPreview();
        }
        else{
            startCameraSource();
        }

        entry.clear();

        String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss", Locale.US).format(new java.util.Date());
        imageCaptureDirectory = Environment.getExternalStorageDirectory().getPath() + "/RoboApp/" + timestamp;
        frameNumber = 0;

        //mFaceRectLogger.addRecordToLog("\n" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));

        // need to start listening again for movement for accelerometer on resume
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
        if(myCamera != null){
            myCamera.release();
        }
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int voicePermission = 0;
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource();
            } else {
                finish();
            }
        }

        else if(requestCode==PERMISSIONS_REQUEST_MULTIPLE){
            for(int i = 0; i<permissions.length; ++i){
                if(grantResults.length>i && grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    switch(permissions[i]){
                        case(Manifest.permission.CAMERA):
                            createCameraSource();
                        case(Manifest.permission.RECORD_AUDIO):
                            voicePermission++;
                        case(Manifest.permission.WRITE_EXTERNAL_STORAGE):
                            voicePermission++;
                    }
                }
            }
            if(voicePermission == 2){
                runRecognizerSetup();
            }
        }
    }


    // for logging accelerometer data
    @Override
    public void onSensorChanged(SensorEvent sensorEvent){
            Sensor mySensor = sensorEvent.sensor;
            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100){
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                String xs = Float.toString(sensorEvent.values[0]);
                String ys = Float.toString(sensorEvent.values[1]);
                String zs = Float.toString(sensorEvent.values[2]);

                 ArrayList<String> accPoint = new ArrayList<String>();

                accPoint.add(xs);
                accPoint.add(ys);
                accPoint.add(zs);

                accData.add(accPoint);

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    // for logging accelerometer data
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return gDetector.onTouchEvent(me);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    private void setTextFieldText(String message, TextView field)
    {
        tempTextView = field;
        tempText = message;
        boolean debugging = false;
        if(!debugging)return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempTextView.setAlpha(1f);
                tempTextView.setText(tempText);
                tempTextView.bringToFront();
            }
        });
    }

    private boolean onSwipe(Direction direction) {
        if(direction == Direction.right) {
            entry.add(CHAR.R);
            //((ImageView)findViewById(R.id.image_place_holder)).setImageResource(R.drawable.right);
        }
        else if(direction == Direction.left) {
            entry.add(CHAR.L);
            // ((ImageView)findViewById(R.id.image_place_holder)).setImageResource(R.drawable.left);
        }
        else if(direction == Direction.up) {
            entry.add(CHAR.U);
            // ((ImageView)findViewById(R.id.image_place_holder)).setImageResource(R.drawable.up);
        }
        else if(direction == Direction.down) {
            entry.add(CHAR.D);
            // ((ImageView)findViewById(R.id.image_place_holder)).setImageResource(R.drawable.down);
        }

        if(entry.equals(psswd)) {
            //((ImageView)findViewById(R.id.image_place_holder)).setImageResource(R.drawable.enter);
            //Code to switch activities to open Menu.
            //Intent intent = new Intent(this, MenuActivity.class);
            entry.clear();
            //showVideoFeed();
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }

        else if(entry.lastElement() != psswd.elementAt(entry.size()-1)) {
            entry.clear();
        }

        if(entry.size()>psswd.size()+2)
        {
            entry.clear();
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        /*
        Grab two events located on the plane at e1=(x1, y1) and e2=(x2, y2)
        Let e1 be the initial event
        e2 can be located at 4 different positions, consider the following diagram
        (Assume that lines are separated by 90 degrees.)


        \ A  /
        \  /
        D   e1   B
        /  \
        / C  \

        So if (x2,y2) falls in region:
        A => it's an UP swipe
        B => it's a RIGHT swipe
        C => it's a DOWN swipe
        D => it's a LEFT swipe

        */

        float x1 = e1.getX();
        float y1 = e1.getY();

        float x2 = e2.getX();
        float y2 = e2.getY();

        Direction direction = Direction.get(x1, y1, x2, y2);
        return onSwipe(direction);
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
       //TODO
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SPEECH DETECTION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Here we will start the voice recognition process.
     */

    // This function loads the pocketsphinx recognizer, allowing active listening
    private void runRecognizerSetup() {
        TextView loadingVoice = (TextView)findViewById(R.id.caption_text);
        loadingVoice.setAlpha(1.0f);
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(FdActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.caption_text))
                            .setText("Failed to init recognizer " + result);
                } else {
                    // Now we will start the keyword search, where we are listening for "Okay robo cat"...
                    TextView loadingVoice = (TextView)findViewById(R.id.caption_text);
                    loadingVoice.setAlpha(0.0f);
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();
    }
    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                //.setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setKeywordThreshold(1e-40f) // Threshold to tune for keyphrase to balance between false alarms and misses
                .setBoolean("-allphone_ci", true)  // Use context-independent phonetic search, context-dependent is too slow for mobile
                .getRecognizer();
        recognizer.addListener(this);

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, START_LISTENING_STRING);

        // Create grammar-based search for command recognition
        File catGrammar = new File(assetsDir, "commands.gram");
        recognizer.addGrammarSearch(CAT_COMMANDS, catGrammar);
    }

    /* This function switches the search we are using for pocketsphinx.
       The search accessed by passing in KWS_SEARCH is a keyword search,
       used to get the cat's attention.

       The other search is a grammatical search that only looks for specific commands.
     */
    private void switchSearch(String searchName) {
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 6000);
    }

    // Used to allow pocketsphinx to take over speech recognition
    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        // If we are done processing speech, go back to keyword search (Listening for "okay robo cat")
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }
    // React to KWS search
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        // So, if we say, "okay robo cat"...
        if (text.equals(START_LISTENING_STRING)){
            recognizer.stop();
            MediaPlayer meow = MediaPlayer.create(getApplicationContext(), R.raw.meow);
            meow.start();
            //noinspection StatementWithEmptyBody
            while(meow.isPlaying());
            meow.release();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    switchSearch(CAT_COMMANDS);
                }
            }, 300);

        }
    }

    // React to commands
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            doCommand(text);
        }
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    public float testCase(){
        return kitty.getScale();
    }

    private void doCommand(String result) {
        if (recognizer.getSearchName().equals(KWS_SEARCH)){
            // No point in displaying keyword command
            if(!result.equals("okay robo cat")){
                Context context = getApplicationContext();
                CharSequence commandToast = "Command: " + result;
                int duration = Toast.LENGTH_SHORT;

                Toast notFoundToast = Toast.makeText(context, commandToast, duration);
                notFoundToast.show();
            } else{
                Context context = getApplicationContext();
                CharSequence commandToast = "Listening...";
                int duration = Toast.LENGTH_SHORT;

                Toast notFoundToast = Toast.makeText(context, commandToast, duration);
                notFoundToast.show();
                return;
            }
            if (result.contains("home") || result.contains("straight")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Straight")
                        .build());
                virtualCat.resetHead();
            }
            else if (result.contains("good")) {
                //Make the cat happy.
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Good")
                        .build());
                kitty.smiledAt();
            }
            else if (result.contains("bad")) {
                //Make the cat mad.
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Bad")
                        .build());
                kitty.frownedAt();
            }
            else if (result.contains("stupid cat")){
                //Make cat disgusted
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Stupid")
                        .build());
                kitty.distgustedAt();
            }
            else if (result.contains("left")) {
                //Make the cat head move left
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Left")
                        .build());
                virtualCat.turnHeadLeft();
            }
            else if (result.contains("walk")||result.contains("walking") || result.contains("come") || result.contains("come here")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Walk")
                        .build());
                virtualCat.stepForward();
            }
            else if (result.contains("stand")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Stand")
                        .build());
                virtualCat.stand();
            }
            else if (result.contains("high five")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("high five")
                        .build());
                virtualCat.highFive();
            }
            else if (result.contains("right")) {
                //Make the cat head move right
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Right")
                        .build());
                virtualCat.turnHeadRight();
            }
            else if (result.contains("up")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Up")
                        .build());
                virtualCat.turnHeadUp();
            }
            else if (result.contains("down")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Down")
                        .build());
                virtualCat.turnHeadDown();
            }
            else if (result.contains("menu")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Menu")
                        .build());
                entry.clear();
                //showVideoFeed();
                Intent intent = new Intent(this, MainActivity.class);

                startActivity(intent);
            }
            else if (result.contains("i love you")) {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Love")
                        .build());
                kitty.loveMeCat();
            }
            else if (result.contains("color") && result.contains("tracking")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Color Track")
                        .build());
                toggleColorTracking.setChecked(true);
            }
            else if (result.contains("face") && result.contains("tracking")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Face Track")
                        .build());
                toggleColorTracking.setChecked(false);
            }
            else if (result.contains("red")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Red")
                        .build());
                ColorTrackingActivity.changeColor("red");
            }
            else if (result.contains("yellow")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Yellow")
                        .build());
                ColorTrackingActivity.changeColor("yellow");
            }
            else if (result.contains("blue")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Blue")
                        .build());
                ColorTrackingActivity.changeColor("blue");
            }
            else if (result.contains("activate") && result.contains("rufus") && result.contains("vision")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Activate rufus vision")
                        .build());
                toggleFTview.setChecked(true);
            }
            else if (result.contains("deactivate") && result.contains("rufus") && result.contains("vision")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Deactivate rufus vision")
                        .build());
                toggleFTview.setChecked(false);
            }
            else if (result.contains("stay")){
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Stay")
                        .build());
                //TODO implement this
            }
            else {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Command")
                        .setAction("Not found")
                        .build());
                Context context = getApplicationContext();
                CharSequence commandNotFound = "Command not found, given: " + result;
                int duration = Toast.LENGTH_SHORT;

                Toast notFoundToast = Toast.makeText(context, commandNotFound, duration);
                notFoundToast.show();
            }
        }
    }

    //BEGIN: Face Tracking Methods and Class
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(false) //track only biggest, most centered face
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS) //look for smile and eye positions
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new FaceTrackerFactory())
                        .build());

        //TODO: this works for a high res front camera with high framerate, adjust camera
        //to work with any size/ framerate

        //less than 1 megapixel is 1024, 768
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(3264, 2448)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                FTPreview.start(mCameraSource, FTGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            } catch (SecurityException e) {
                Log.e(TAG, "No Permission granted.", e);
            }
        }

        //Snackbar.make(FTGraphicOverlay, R.string.permission_camera_rationale,
        //      Snackbar.LENGTH_INDEFINITE)
        //    .show();
    }

    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public com.google.android.gms.vision.Tracker<Face> create(Face face) {
            return new MotionFaceTracker(FTGraphicOverlay);
        }
    }

    //TODO: Change Back to private when finished with unit testing
    public class MotionFaceTracker extends com.google.android.gms.vision.Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        PointF trackPosition;
        List<Landmark> facialFeatures;
        PointF rightEye;
        PointF leftEye;
        double eyeSeperationPixels;
        double pixelsPerCentimeter;
        PointF centerOfImage;

        double xOffsetFace;
        double yOffsetFace;
        double DistanceToFace;

        double thetax;
        double thetay;

        SparseArray<Face> allDetectedFaces;
        Boolean happiestFace = false;
        Face tmpFace;

        MotionFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
            centerOfImage=new PointF(mCameraSource.getPreviewSize().getHeight()/2.0f, mCameraSource.getPreviewSize().getWidth()/2.0f);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            allDetectedFaces=detectionResults.getDetectedItems();
            happiestFace=true;
            for(int i = 0, nsize = allDetectedFaces.size(); i < nsize; i++) {
                int tmpFaceID = allDetectedFaces.keyAt(i);
                tmpFace = allDetectedFaces.valueAt(i);
                if(tmpFaceID!=face.getId()){
                    if(tmpFace.getIsSmilingProbability()>face.getIsSmilingProbability()||tmpFace.getIsSmilingProbability()<25){
                        happiestFace=false;
                    }
                }
            }

            if(happiestFace) {
                trackPosition = trackFace(face);

                // if both rotation in x and y are less than 2.5 degrees, its "good enough"
                if (Math.abs(thetax) > 2.5 || Math.abs(thetay) > 2.5) {
                    virtualCat.lookToward(trackPosition);
                }

                //use happiness rating
                emotionalReaction(face.getIsSmilingProbability());
            }
            happiestFace=false;


        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }

        private double getEyeSeperation(Face face){
            facialFeatures = face.getLandmarks();
            for(int i = 0; i<facialFeatures.size(); ++i){
                if (facialFeatures.get(i).getType()==RIGHT_EYE){
                    rightEye=facialFeatures.get(i).getPosition();
                }
                if (facialFeatures.get(i).getType()==LEFT_EYE){
                    leftEye=facialFeatures.get(i).getPosition();
                }
            }
            Log.i(TAG, "EyeDistance: " + Double.toString(Math.sqrt(Math.pow(rightEye.x-leftEye.x, 2.0) + Math.pow(rightEye.y-leftEye.y, 2.0))));
            return Math.sqrt(Math.pow(rightEye.x-leftEye.x, 2.0) + Math.pow(rightEye.y-leftEye.y, 2.0));
        }

        private double approximateDistanceViaPixels(double eyeWidth){
            //note eyeWidth is based on a camera of 1024, 768
            //divide actual resolution by assumed to preserve meaning of function
            //double multiplier = 1024.0/((double)mCameraSource.getPreviewSize().getWidth());
            //Log.i(TAG, "eyeSeperationADJUSTED: " + Double.toString(eyeWidth*multiplier));
            //return -0.4028*eyeWidth*multiplier+89.705;
            return 5532.1*Math.pow(eyeWidth, -0.917);
        }

        private PointF trackFace(Face face){
            PointF result;

            eyeSeperationPixels=getEyeSeperation(face);
            DistanceToFace = approximateDistanceViaPixels(eyeSeperationPixels);
            float x = face.getPosition().x + face.getWidth() / 2.0f;
            float y = face.getPosition().y + face.getHeight() / 2.0f;
            xOffsetFace= ((centerOfImage.x-x)/eyeSeperationPixels)*AverageHumanEyeSeperation;
            yOffsetFace = ((y-centerOfImage.y)/eyeSeperationPixels)*AverageHumanEyeSeperation;
            thetax=Math.asin(xOffsetFace/(float)DistanceToFace);
            thetay=Math.asin(yOffsetFace/(float) DistanceToFace);
            thetax= Math.toDegrees(thetax)*1.5;
            thetay= Math.toDegrees(thetay);
            result = new PointF((float)thetax, (float)thetay);

            return result;
        }

        private double computeDis(PointF p1, PointF p2){
            return Math.sqrt(Math.pow(p1.x-p2.x, 2.0) + Math.pow(p1.y-p2.y, 2.0));
        }

        private void emotionalReaction(double smileProb){
            if(smileProb>=0.50){
                kitty.detectedSmile();
            }else{
                kitty.detectedFrown();
            }
        }

    }
    //END:Face Tracking Methods and Class

    // *********************** ColorTracking Utility Functions Below **********************
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

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            if(!sizeChecked) {
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data, 0, data.length, options);
                options.inSampleSize = calculateInSampleSize(options, 40, 30);
                options.inJustDecodeBounds = false;
                sizeChecked = true;
            }

            Bitmap imageBitmap = BitmapFactory.decodeByteArray(data , 0, data.length, options);
            new ColorFinder(new ColorFinder.CallbackInterface() {
                @Override
                public void onCompleted(String color) {
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    int x = width/2;
                    int y = height/2;
                    PointF trackPosition;
                    switch (color){
                        case "TL":
                            x = -50; //width/6;
                            y = -50; //height/6;
                            break;
                        case "TM":
                            x = 0; //width/2;
                            y = -50; //height/6;
                            break;
                        case "TR":
                            x = 50; //(width/6)*5;
                            y = -50; //height/6;
                            break;
                        case "ML":
                            x = -50; //width/6;
                            y = 0;//height/2;
                            break;
                        case "MM":
                            x = 0;//width/2;
                            y = 0;//height/2;
                            break;
                        case "MR":
                            x = 50;//(width/6)*5;
                            y = 0;//height/2;
                            break;
                        case "BL":
                            x = -50;//width/6;
                            y = 50;//(height/6)*5;
                            break;
                        case "BM":
                            x = 0;//width/2;
                            y = 50;//(height/6)*5;
                            break;
                        case "BR":
                            x = 50;//(width/6)*5;
                            y = 50;//(height/6)*5;
                            break;
                        default:
                            x = 0;
                            y = 0;
                            break;
                    }
                    trackPosition = new PointF(x, y);

                    virtualCat.lookToward(trackPosition);
                }
            }).findDominantColor(imageBitmap, true);
            myCamera.startPreview();
            myCamera.takePicture(null, null, mPicture);
        }
    };
    // *********** End ColorTracking Utility Functions **************
}

