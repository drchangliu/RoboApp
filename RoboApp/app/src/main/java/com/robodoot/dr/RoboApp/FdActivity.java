package com.robodoot.dr.RoboApp;

import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.robodoot.dr.facetracktest.R;
import com.robodoot.roboapp.Direction;
import com.robodoot.roboapp.MainActivity;
import com.robodoot.roboapp.PololuVirtualCat;
import com.robodoot.roboapp.VirtualCat;

// Psphx imports
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import static android.widget.Toast.makeText;

/**
 * Behavior mode activity. This is the main activity of the app.
 * uses built in hardware functions to use the Android accelerometer data (SensorEventListener)
 */

//TODO: command not found always shows when using continuous speech
    //TODO: OnResume method seems broken for speech
    //TODO: OnResume also not implemented for passive face tracking
    //TODO: OnDestroy methods needed for both.

public class FdActivity extends Activity implements GestureDetector.OnGestureListener, SensorEventListener, RecognitionListener {
    // FUNCTION AND VARIABLE DEFINTIONS
    private Logger mFaceRectLogger;
    private Logger mSpeechTextLogger;
    boolean initialized = false;

    //new camera variables start
    private CameraSource mCameraSource = null;
    //new facetracker variables end

    private int frameNumber;

    private boolean cameraIsChecked = false;

    /* psphx1: Variable declarations */

    // The pocketsphinx recognizer
    private SpeechRecognizer recognizer;
    private static final String KWS_SEARCH = "wakeup";
    // Keyword we are using to start command listening
    private static final String START_LISTENING_STRING = "okay rufus";
    // Used when getting permission to listen
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private static final int PERMISSIONS_REQUEST_CAMERA = 2;
    private static final int PERMISSIONS_REQUEST_MULTIPLE=3;
    private static final String CAT_COMMANDS = "cat";

    /* End pocketsphinx variable declarations */

    private ImageButton btnSpeak;
    private Button btnMenu;
    private final int REQ_CODE_SPEECH_INPUT=100;
    private ArrayList<String> result;
    private static final String good = "good";
    private static final String bad = "bad";
    private static final String cry = "cry";
    private static final String disgust = "stupid cat";

    private GestureDetector gDetector;
    public enum CHAR {U, D, L, R}
    private static Vector<CHAR> psswd = new Vector<>();
    public static Vector<CHAR> entry = new Vector<>();

    private static final String TAG = "FdActivity";
    private static final int JAVA_DETECTOR = 0;

    private CatEmotion kitty;
    public enum Directions {UP, DOWN, LEFT, RIGHT, CENTER}
    private ImageView[] arrows;
    private RelativeLayout frame;
    private Bitmap bmp;
    private Directions dir;


    private int IDcount;
    private ArrayList<ArrayList<Integer>> SimilarID;

    private int refreshRecognizer;

    private File mCascadeFile;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.1f;
    private int mAbsoluteFaceSize = 0;

    boolean[] filter;


    private double xCenter = -1;
    double yCenter = -1;

    private boolean debugging = false;
    private boolean trackingGreen = false;
    private boolean trackingRed = false;

    //PololuHandler pololu;
    VirtualCat virtualCat;
    //PololuVirtualCat virtualCat;

    private TextView debug1;
    TextView debug2;
    private TextView debug3;
    TextView tempTextView;

    private String tempText;

    // private variables for accelerometer declatation
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    // array of arrays to store the accelerometer data in x, y, z format
    private ArrayList<ArrayList<String>> accData = new ArrayList<ArrayList<String>>();

    // Function to open menu activity
    public void openMenu(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private String timestamp;
    private String imageCaptureDirectory;

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        SimilarID = new ArrayList<ArrayList<Integer>>();
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

        // initializing accelerometer variables and registering listener (listening for movement)
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //debugging = true;

        psswd.add(CHAR.U); psswd.add(CHAR.U);  psswd.add(CHAR.D); psswd.add(CHAR.D);
        psswd.add(CHAR.L);  psswd.add(CHAR.R);  psswd.add(CHAR.L);  psswd.add(CHAR.R);

        gDetector = new GestureDetector(getApplicationContext(), this);
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Speech button click action
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        // Menu button on click action
        btnMenu = (Button) findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                openMenu();
            }
        });

        arrows = new ImageView[4];
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
        debug1 = (TextView)findViewById(R.id.debugText1);
        debug2 = (TextView)findViewById(R.id.debugText2);
        debug3 = (TextView)findViewById(R.id.debugText3);

        debug1.setAlpha(0f);
        debug2.setAlpha(0f);
        debug3.setAlpha(0f);

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
    }
    //New Face Tracker Code

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
                    if (permissions[i].equals(Manifest.permission.CAMERA)) {
                        createCameraSource();
                    } else if(permissions[i].equals(Manifest.permission.RECORD_AUDIO)){
                        voicePermission++;
                    } else if(permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        voicePermission++;
                    }
                }
            }
            if(voicePermission == 2){
                runRecognizerSetup();
            }
        }
    }

    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true) //track only biggest, most centered face
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS) //look for smile and eye positions
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new FaceTrackerFactory())
                        .build());

        //TODO: this works for a high res front camera with high framerate, adjust camera
        //to work with any size/ framerate
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    private void startCameraSource() {
        if (mCameraSource != null) {
            try {
                mCameraSource.start();
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            } catch(SecurityException e){
                Log.e(TAG, "No Permission granted.", e);
            }
        }
    }
    //End New Face Tracker Code

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

                //Log.w("Accelerometer", "(" + xs + ", " + ys + ", " + zs + ")");
                ArrayList<String> accDataSingle = new ArrayList<String>();
                accDataSingle.add(xs);
                accDataSingle.add(ys);
                accDataSingle.add(zs);
                accData.add(accDataSingle);
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

    @Override
    public void onPause() {
        Log.d("Pause", "onPause() called");
        for (ArrayList<String> it : accData){
            Log.d("Accelerometer", " " + it);
        }
        super.onPause();
        // Stop the recognizer
        if (recognizer != null) {
            recognizer.cancel();
        }
        /*if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();*/
        //record(imageCaptureDirectory);
        frameNumber = 0;
        // for accelerometer, also need to stop listening on pause
        senSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        virtualCat.onResume(getIntent(), this);


        super.onResume();;
        if (!initialized) {
            initialized = true;
            virtualCat.resetHead();
        }

        // Restart recognizer
        if(recognizer != null) {
            runRecognizerSetup();
        }

        startCameraSource();

        entry.clear();

        timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
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
        if (mCameraSource != null) {
            mCameraSource.release();
        }
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
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }


    // This function loads the pocketsphinx recognizer, allowing active listening
    private void runRecognizerSetup() {
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
                    // Now we will start the keyword search, where we are listening for "Okay rufus"...
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
                .setKeywordThreshold(1e-45f) // Threshold to tune for keyphrase to balance between false alarms and misses
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
            recognizer.startListening(searchName, 10000);
    }

    // Used to allow pocketsphinx to take over speech recognition
    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onEndOfSpeech() {
        // If we are done processing speech, go back to keyword search (Listening for "okay rufus")
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);
    }
    // React to KWS search
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        // So, if we say, "okay rufus"...
        if (text.equals(START_LISTENING_STRING)){
            MediaPlayer meow = MediaPlayer.create(getApplicationContext(), R.raw.meow);
            meow.start();
            while(meow.isPlaying());
            meow.stop();
            meow.release();
            switchSearch(CAT_COMMANDS);
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

    void doCommand (String result) {
        if (result.contains("home") || result.contains("straight")) {
            virtualCat.resetHead();
        }
        else if (result.contains("good")) {
            //Make the cat happy.
            kitty.smiledAt();
        }
        else if (result.contains("bad")) {
            //Make the cat mad.
            kitty.frownedAt();
        }
        else if (result.contains("cry")){
            //Make the cat cry
            kitty.cryingAt();
        }
        else if (result.contains("stupid cat")){
            //Make cat disgusted
            kitty.distgustedAt();
        }
        else if (result.contains("left")) {
            //Make the cat head move left
            virtualCat.turnHeadLeft();
        }
        else if (result.contains("walk")||result.contains("walking") || result.contains("come")) {
            virtualCat.stepForward();
        }
        //if (result.contains("right") || result.contains("write")) {
        // does this work? we'll see
        else if (result.contains("right")) {
            //Make the cat head move right
            virtualCat.turnHeadRight();
        }
        else if (result.contains("green")) {
            trackingGreen = true;
            trackingRed = false;
        }
        else if (result.contains("red")) {
            trackingGreen = false;
            trackingRed = true;
        }
        else if (result.contains("blue")) {
            trackingGreen = trackingRed = false;
        }
        else if (result.contains("up")) {
            virtualCat.turnHeadUp();
        }
        else if (result.contains("down")) {
            virtualCat.turnHeadDown();
        }
        else if (result.contains("menu")) {
            entry.clear();
            //showVideoFeed();
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }
        else if (result.contains("love")) {
            kitty.loveMeCat();
        }
        else if (result.contains("find") && result.contains("me")){
            Intent intent = new Intent("com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity");
            startActivity(intent);
        }
        else {
            Context context = getApplicationContext();
            CharSequence commandNotFound = "Command not found!";
            int duration = Toast.LENGTH_SHORT;

            Toast notFoundToast = Toast.makeText(context, commandNotFound, duration);
            notFoundToast.show();
        }
    }


    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new MotionFaceTracker();
        }
    }

    private class MotionFaceTracker extends Tracker<Face> {
        PointF trackPosition;

        MotionFaceTracker() {
            super();
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            super.onNewItem(faceId, item);
            //openMenu();
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            super.onUpdate(detectionResults, face);

            //use happiness rating
            float happiness = face.getIsSmilingProbability();
            if (happiness>0.75f){
                kitty.detectedSmile();
            }else if (happiness<0.1f){
                kitty.detectedFrown();
            }
            //end use happiness rating

            float x = -1*face.getPosition().x + face.getWidth() / 2;
            float y = face.getPosition().y + face.getHeight() / 2 - 512;
            //middle not quite 512, works for now
            //TODO: 512 is set for the preview size above, take the hardcoded number out

            trackPosition=new PointF(x, y);

            //if distance from center is < half a face size
            // done so that "good enough" scales for faces at multiple distances
            if( Math.sqrt(Math.pow(x, 2.0)+Math.pow(y, 2.0))>Math.pow(face.getWidth()/2, 2)+Math.pow(face.getHeight()/2, 2)){
                virtualCat.lookToward(trackPosition);
            }
        }
    }
}

