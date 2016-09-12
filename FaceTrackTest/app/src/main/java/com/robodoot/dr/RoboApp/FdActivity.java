package com.robodoot.dr.RoboApp;

import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import android.content.ActivityNotFoundException;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.widget.ImageButton;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.media.MediaRecorder;

import com.robodoot.dr.facetracktest.R;
import com.robodoot.roboapp.ColorValues;
import com.robodoot.roboapp.Direction;
import com.robodoot.roboapp.ImageUtil;
import com.robodoot.roboapp.MainActivity;
import com.robodoot.roboapp.Person;
import com.robodoot.roboapp.PololuVirtualCat;
import com.robodoot.roboapp.VirtualCat;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_face.*;

import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.Vector;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

/**
 * Behavior mode activity. This is the main activity of the app. It uses OpenCV/JavaCV for face
 * detection and color tracking. Image processing occurs in the {@link #onCameraFrame} method.
 */
public class FdActivity extends Activity implements GestureDetector.OnGestureListener, CvCameraViewListener2 {
    private Logger mFaceRectLogger;
    private Logger mSpeechTextLogger;
    boolean initialized = false;

    private ArrayList<opencv_core.Mat> framesForVideo = new ArrayList<opencv_core.Mat>();
    private int frameNumber;

    private boolean cameraIsChecked = false;

    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT=100;
    private ArrayList<String> result;
    private static final String good = "good";
    private static final String bad = "bad";

    private GestureDetector gDetector;
    public enum CHAR {U, D, L, R}
    private static Vector<CHAR> psswd = new Vector<>();
    public static Vector<CHAR> entry = new Vector<>();

    private static final String TAG = "FdActivity";
    private static final int JAVA_DETECTOR = 0;

    private CatEmotion kitty;
    public enum Directions {UP, DOWN, LEFT, RIGHT, CENTER}
    private Mat mRgba;
    private Mat mRgbaForColorTracking;
    private Mat mGray;
    private Mat tempMat1;
    private MatOfRect faces;
    private MatOfRect smiles;
    private ImageView[] arrows;
    private RelativeLayout frame;
    private Bitmap bmp;
    private Directions dir;

    private Rect[] FavFaceLocationBuffer;
    private Rect[] FaceLocationBuffer;
    private Mat[]  FaceMatBuffer;
    private Mat[]  EigenMats;
    private int IDcount;
    private ArrayList<Scalar> UserColors;
    private ArrayList<ArrayList<Mat>> TrainingSets;
    private FaceRecognizer faceRecognizer;

    private ArrayList<Person> peopleLastCameraFrame;
    private ArrayList<Person> peopleThisCameraFrame;
    private ArrayList<ArrayList<Integer>> SimilarID;

    private int refreshRecognizer;

    private File mCascadeFile;
    private CascadeClassifier mJavaDetectorFace;
    private CascadeClassifier mJavaDetectorSmile;

    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.1f;
    private int mAbsoluteFaceSize = 0;

    private JavaCameraView mOpenCvCameraView;
    boolean[] filter;

    private Size stds = new Size(80,80);

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

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {

                    try {
                        InputStream is = null;
                        FileOutputStream os = null;
                        File cascadeDir = null;
                        try {
                            // load cascade file from application resources
                            is = getResources().openRawResource(
                                    R.raw.lbpcascade_frontalface); //opens resource for openCV cascade classifier
                                                                    //classifier is trained with afew hundred examples then can be applied to a region of interest
                                                                    // outputs 1 if object is likely to have object, 0 otherwise
                                                                    // see http://docs.opencv.org/2.4/modules/objdetect/doc/cascade_classification.html
                            cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            mCascadeFile = new File(cascadeDir,
                                    "lbpcascade_frontalface.xml");
                            os = new FileOutputStream(mCascadeFile);

                            byte[] buffer = new byte[4096]; // a temporary buffer to facilitate IO
                            filter = new boolean[5];
                            int bytesRead;
                                while ((bytesRead = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, bytesRead);
                                }
                        }
                        finally {
                            if (is != null) is.close();
                            if (os != null) os.close();
                            //if (cascadeDir != null) cascadeDir.delete();
                        }

                        InputStream isS = null;
                        File cascadeDirS;
                        File cascadeFileS = null;
                        FileOutputStream osS = null;
                        try {
                            // --------------------------------- load smile
                            // classificator -----------------------------------
                            isS = getResources().openRawResource(
                                    R.raw.haarcascade_smile);
                            cascadeDirS = getDir("cascadeS",
                                    Context.MODE_PRIVATE);
                            cascadeFileS = new File(cascadeDirS,
                                    "haarcascade_smile.xml");
                            osS = new FileOutputStream(cascadeFileS);

                            byte[] bufferS = new byte[4096];
                            int bytesReadS;
                            while ((bytesReadS = isS.read(bufferS)) != -1) {
                                osS.write(bufferS, 0, bytesReadS);
                            }
                        }
                        finally {
                            if (isS != null) isS.close();
                            if (osS != null) osS.close();
                        }

                        mJavaDetectorFace = new CascadeClassifier(
                                mCascadeFile.getAbsolutePath());
                        if (mJavaDetectorFace.empty()) {
                            mJavaDetectorFace = null;
                        } else


                        mJavaDetectorSmile = new CascadeClassifier(
                                cascadeFileS.getAbsolutePath());
                        if (mJavaDetectorSmile.empty()) {
                            mJavaDetectorSmile = null;
                        }
                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private String timestamp;
    private String imageCaptureDirectory;

    private ColorValues redValues = null, greenValues = null, blueValues = null;

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        peopleThisCameraFrame = new ArrayList<Person>();
        peopleLastCameraFrame = new ArrayList<Person>();
        SimilarID = new ArrayList<ArrayList<Integer>>();
        //pololu = new PololuHandler();
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
        setContentView(R.layout.activity_fd);

        //debugging = true;

        psswd.add(CHAR.U); psswd.add(CHAR.U);  psswd.add(CHAR.D); psswd.add(CHAR.D);
        psswd.add(CHAR.L);  psswd.add(CHAR.R);  psswd.add(CHAR.L);  psswd.add(CHAR.R);

        gDetector = new GestureDetector(getApplicationContext(), this);
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        arrows = new ImageView[4];
        arrows[0]=(ImageView)findViewById(R.id.arrow_up);
        arrows[1]=(ImageView)findViewById(R.id.arrow_right);
        arrows[2]=(ImageView)findViewById(R.id.arrow_down);
        arrows[3]=(ImageView)findViewById(R.id.arrow_left);
        //for (int i = 0; i < 4; i++) arrows[i].setVisibility(View.INVISIBLE);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        kitty = new CatEmotion(this);
        kitty.pic=(ImageView)findViewById(R.id.image_place_holder);

        debug1 = (TextView)findViewById(R.id.debugText1);
        debug2 = (TextView)findViewById(R.id.debugText2);
        debug3 = (TextView)findViewById(R.id.debugText3);

        debug1.setAlpha(0f);
        debug2.setAlpha(0f);
        debug3.setAlpha(0f);

        mOpenCvCameraView.setAlpha(0f);
        mOpenCvCameraView.bringToFront();

        String[] lines;
        if ((lines = new Logger("red_color_values", false).ReadLines()) != null
                && lines.length > 0) {
            redValues = new ColorValues(lines[0]);
        }
        if ((lines = new Logger("green_color_values", false).ReadLines()) != null
                && lines.length > 0) {
            greenValues = new ColorValues(lines[0]);
        }
        if ((lines = new Logger("color_values", false).ReadLines()) != null
                && lines.length > 0) {
            blueValues = new ColorValues(lines[0]);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        //record(imageCaptureDirectory);
        frameNumber = 0;
    }

    @Override
    public void onResume() {
        //pololu.onResume(getIntent(), this);
        virtualCat.onResume(getIntent(), this);

        super.onResume();
        //pololu.home();
        if (!initialized) {
            initialized = true;
            virtualCat.resetHead();
        }
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);

        entry.clear();
        //showVideoFeed();

        timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        imageCaptureDirectory = Environment.getExternalStorageDirectory().getPath() + "/RoboApp/" + timestamp;
        frameNumber = 0;

        mFaceRectLogger.addRecordToLog("\n" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MISC
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent me) {
//        if(me.getAction()==MotionEvent.ACTION_BUTTON_PRESS&&!cameraIsChecked)
//        {
//            cameraIsChecked = !cameraIsChecked;
//            mOpenCvCameraView.setAlpha(0.8f);
//            mOpenCvCameraView.bringToFront();
//        }
//        else if(me.getAction()==MotionEvent.ACTION_BUTTON_RELEASE&&cameraIsChecked)
//        {
//            cameraIsChecked = !cameraIsChecked;
//            mOpenCvCameraView.setAlpha(0f);
//
//
//        }

        return gDetector.onTouchEvent(me);
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    private void record(String directory) {
        opencv_core.Mat[] frames = new opencv_core.Mat[framesForVideo.size()];
        framesForVideo.toArray(frames);

        String path = directory + "/output" + System.currentTimeMillis() + ".mp4";
        File file = new File(path).getAbsoluteFile();
        file.getParentFile().mkdirs();

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(path, 200, 150);

        try {
            recorder.setVideoCodec(13); // CODEC_ID_MPEG4 //CODEC_ID_MPEG1VIDEO
            // //http://stackoverflow.com/questions/14125758/javacv-ffmpegframerecorder-properties-explanation-needed

            recorder.setFrameRate(10); // This is the frame rate for video. If you really want to have good video quality you need to provide large set of images.
            recorder.setPixelFormat(0); // PIX_FMT_YUV420P

            recorder.start();
            OpenCVFrameConverter frameConverter = new OpenCVFrameConverter.ToMat();
            for (int i = 0; i < frames.length; i++) {
                Frame f = frameConverter.convert(frames[i]);
                recorder.record(f);
            }
            recorder.stop();
        } catch (Exception e) {
            e.printStackTrace();
            for (opencv_core.Mat f : framesForVideo) {
                f.release();
            }
            framesForVideo.clear();
        }

        for (opencv_core.Mat f : framesForVideo) {
            f.release();
        }
        framesForVideo.clear();
    }

    private final void saveMat(String path, Mat mat) {
        File file = new File(path).getAbsoluteFile();
        file.getParentFile().mkdirs();
        try {
            int cols = mat.cols();
            byte[] data = new byte[(int) mat.total() * mat.channels()];
            mat.get(0, 0, data);
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
                oos.writeObject(cols);
                oos.writeObject(data);
                oos.close();
            }
        } catch (IOException | ClassCastException ex) {
            System.err.println("ERROR: Could not save mat to file: " + path);
        }
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

    private void showVideoFeed()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOpenCvCameraView.setAlpha(1.0f);
                debugging = true;
            }
        });
    }

    private void hideVideoFeed()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOpenCvCameraView.setAlpha(0.0f);
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

            //intent.putExtra("pololu", pololu);

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
        if(mOpenCvCameraView.getAlpha()>0.5f)
            mOpenCvCameraView.setAlpha(0.0f);
        else
            mOpenCvCameraView.setAlpha(0.80f);
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
     * Capture speech input from microphone.
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

    /**
     * This is called after speech recognition. The recognized words come in as a list of strings
     * and are processed to make the cat perform actions or change emotion state.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if  (resultCode==RESULT_OK && null!=data) {
                    //Insert ArrayList stuff
                    result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mSpeechTextLogger.addRecordToLog(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date()));
                    ArrayList<String> tmpList = new ArrayList<>();
                    for (int i=0; i< result.size(); i++) {
                        String tmp = result.get(i);
                        Log.w("WORDS1", tmp);
                        if(tmp.contains(" ")) {
                            tmpList.addAll(Arrays.asList(tmp.split(" ")));
                        }
                        mSpeechTextLogger.addRecordToLog(result.get(i));
                    }
                    result.addAll(tmpList);
                    for (int i=0; i<result.size(); i++) {
                        String tmp = result.get(i);
                        Log.w("WORDS2", tmp);
                    }
                    //Make a call to analyze the words and update cat's mood.
                    if (result.contains("home") || result.contains("straight")) {
                        virtualCat.resetHead();
                    }
                    if (result.contains("good")) {
                        //Make the cat happy.
                        kitty.smiledAt();
                    }
                    if (result.contains("bad")) {
                        //Make the cat mad.
                        kitty.frownedAt();
                    }
                    if (result.contains("left")) {
                        //Make the cat head move left
                        virtualCat.turnHeadLeft();
                    }
                    if (result.contains("walk")||result.contains("walking") || result.contains("come")) {
                        virtualCat.stepForward();
                    }
                    //if (result.contains("right") || result.contains("write")) {
                    // does this work? we'll see
                    if (!Collections.disjoint(result, Arrays.asList("right", "write", "white"))) {
                        //Make the cat head move right
                        virtualCat.turnHeadRight();
                    }
                    if (result.contains("green")) {
                        trackingGreen = true;
                        trackingRed = false;
                    }
                    if (result.contains("red")) {
                        trackingGreen = false;
                        trackingRed = true;
                    }
                    if (result.contains("blue")) {
                        trackingGreen = trackingRed = false;
                    }
                    if (result.contains("up")) {
                        virtualCat.turnHeadUp();
                    }
                    if (result.contains("down")) {
                        virtualCat.turnHeadDown();
                    }
                    if (result.contains("menu")) {
                        entry.clear();
                        //showVideoFeed();
                        Intent intent = new Intent(this, MainActivity.class);

                        startActivity(intent);
                    }
                    //Clear the arrayList for the next time a button is pressed.
                    result.clear();
                }
                break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERAL/SHARED OPENCV
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * initialize stuff related to opencv
     */
    public void onCameraViewStarted(int width, int height) {
        TextView loading = (TextView)findViewById(R.id.LoadingText);
        loading.setAlpha(1.0f);
        mGray = new Mat();
        mRgba = new Mat();
        mRgbaForColorTracking = new Mat();
        tempMat1 = new Mat();
        faces = new MatOfRect();
        smiles = new MatOfRect();
        FavFaceLocationBuffer = new Rect[5];
        for(int i = 0;i<5;i++)FavFaceLocationBuffer[i]=new Rect(new Point(0,0),new Size(1,1));
        FaceLocationBuffer = new Rect[15];
        for(int i = 0;i<15;i++)FaceLocationBuffer[i]=new Rect(new Point(0,0),new Size(1,1));
        FaceMatBuffer  = new Mat[15];
        for(int i = 0;i<15;i++)FaceMatBuffer[i]=new Mat();
        EigenMats = new Mat[10];
        for(int i = 0;i<10;i++)EigenMats[i]=new Mat();
        IDcount = 1;
        UserColors = new ArrayList<Scalar>();
        UserColors.add(0, new Scalar(0, 0, 0));
        TrainingSets = new ArrayList<ArrayList<Mat>>();
        TrainingSets.add(0,new ArrayList<Mat>());
        faceRecognizer = opencv_face.createFisherFaceRecognizer();
        //kitty.pic.setVisibility(View.GONE);
        //loadTestFaces();
        refreshRecognizer=0;
        entry.clear();

        loading.setAlpha(0f);
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        mRgbaForColorTracking.release();
        tempMat1.release();
        faces.release();
        smiles.release();
        for(int i = 0;i<9;i++)EigenMats[i].release();
        for(int i = 0;i<12;i++)FaceMatBuffer[i].release();
    }

    /**
     * Process a video frame (do face detection, color tracking)
     * @param inputFrame the image to process
     * @return a possibly modified inputFrame to be displayed
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        /*peopleThisCameraFrame.clear();

        try {
            inputFrame.rgba().copyTo(mRgba);
            inputFrame.gray().copyTo(mGray);

            Core.flip(mRgba.t(), mRgba, 0);
            Core.flip(mGray.t(), mGray, 0);

            if (mAbsoluteFaceSize == 0) {
                int height = mGray.rows();
                if (Math.round(height * mRelativeFaceSize) > 0) {
                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
                }
            }

            // detect faces
            if (mJavaDetectorFace != null)
                mJavaDetectorFace.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

            Rect biggestFace = new Rect(new Point(0, 0), new Size(1, 1));

            Rect[] facesArray = faces.toArray();

            // for each face
            for (int i = 0; i < facesArray.length; i++) {
                // calculate center position of face
                xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
                yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
                Point center = new Point(xCenter, yCenter);

                Rect r = facesArray[i];

                // make mat from submat containing only the face
                mGray.submat(r).copyTo(tempMat1);
                // equalize the grayscale values (stretch them out)
                Imgproc.equalizeHist(tempMat1, tempMat1);

                // attempt to recognize the face
                int recoID = checkForRecognition(tempMat1);

                // convert from grayscale to rgba
                Imgproc.cvtColor(tempMat1, tempMat1, Imgproc.COLOR_GRAY2RGBA);

                if (recoID < 21) {
                    // copy the submat we were working with back into the area it occupied in the original mat (the current frame)
                    tempMat1.copyTo(mRgba.submat(r));
                    // draw a red rectangle around the face
                    Imgproc.rectangle(mRgba, r.br(), r.tl(), new Scalar(255, 0, 0), 8);
                    // keep track of biggest face
                    if (r.size().area() > biggestFace.size().area()) biggestFace = r;
                } else {
                    // calculate mouth rect
                    Point mouthPt1 = new Point(r.x + r.width / 10, r.y + r.height / 2 + r.height / 10);
                    Point mouthPt2 = new Point(r.x + r.width - r.width / 10, r.y + r.height - r.height / 10);
                    Rect mouthRect = new Rect(mouthPt1, mouthPt2);

                    //Imgproc.rectangle(mRgba, mouthRect.br(), mouthRect.tl(), new Scalar(0, 255, 0), 4);

                    // detect smiles
                    if (mJavaDetectorSmile != null)
                        mJavaDetectorSmile.detectMultiScale(mGray.submat(mouthRect), smiles, 1.1, 6, 0, new Size(mouthRect.width * 0.6, mouthRect.height * 0.6), new Size());
                    Rect[] smileArray = smiles.toArray();
                    boolean smiling = false;
                    // if detected any smiles, set smiling
                    if (smileArray.length > 0) smiling = true;

                    // flip the mouth rect
                    Core.flip(mGray.submat(mouthRect), mGray.submat(mouthRect), -1);
                    // detect flipped smiles (frowns)
                    if (mJavaDetectorSmile != null)
                        mJavaDetectorSmile.detectMultiScale(mGray.submat(mouthRect), smiles, 1.05, 4, 0, new Size(mouthRect.width * 0.4, mouthRect.height * 0.4), new Size());
                    Rect[] frownArray = smiles.toArray();

                    // if detected any frowns, set frowning if not smiling
                    boolean frowning = frownArray.length > 0 && !smiling;

                    // add person
                    peopleThisCameraFrame.add(new Person(recoID, r, smiling, frowning));
                    //setTextFieldText(Integer.toString(recoID),debug1);
                }
            }

            if (biggestFace.size().area() != 1) {
                Rect r = biggestFace;
                Point mouthPt1 = new Point(r.x + r.width / 10, r.y + r.height / 2 + r.height / 10);
                Point mouthPt2 = new Point(r.x + r.width - r.width / 10, r.y + r.height - r.height / 10);
                Rect mouthRect = new Rect(mouthPt1, mouthPt2);
                //find the number of smiles

                Imgproc.rectangle(mRgba, mouthRect.br(), mouthRect.tl(), new Scalar(0, 255, 0), 4);

                if (mJavaDetectorSmile != null)
                    mJavaDetectorSmile.detectMultiScale(mGray.submat(mouthRect), smiles, 1.4, 3, 0, new Size(mouthRect.width * 0.6, mouthRect.height * 0.4), new Size());

                Rect[] smileArray = smiles.toArray();

                if (smileArray.length > 0) kitty.smiledAt();
                if (adjustFaceBuffer(biggestFace)) {

                    addNewUser();
                }

                //if (peopleThisCameraFrame.size() == 0) trackFavFace(biggestFace);
            }

            if (peopleThisCameraFrame.size() > 0) {
                ArrayList<Integer> IDsToCheck = new ArrayList<Integer>();

                for (int i = 0; i < peopleThisCameraFrame.size(); i++) {
                    for (int j = 0; j < peopleLastCameraFrame.size(); j++) {
                        peopleThisCameraFrame.get(i).checkID(SimilarID, UserColors);
                        peopleThisCameraFrame.get(i).checkSimilar(peopleLastCameraFrame.get(j),SimilarID, UserColors);
                    }
                    kitty.lookedAt(peopleThisCameraFrame.get(i).ID, peopleThisCameraFrame.get(i).smiling, peopleThisCameraFrame.get(i).frowning);
                    Scalar color = UserColors.get(peopleThisCameraFrame.get(i).ID);
                    Imgproc.rectangle(mRgba, peopleThisCameraFrame.get(i).face.br(), peopleThisCameraFrame.get(i).face.tl(), color, 8);

                    IDsToCheck.add(peopleThisCameraFrame.get(i).ID);
                }

                *//*int favID = kitty.getFavPerson(IDsToCheck);
                Person favPerson = peopleThisCameraFrame.get(0);

                if (favID > 20) {
                    for (int i = 0; i < peopleThisCameraFrame.size(); i++) {
                        if (peopleThisCameraFrame.get(i).ID == favID) {
                            favPerson = peopleThisCameraFrame.get(i);
                            trackFavFace(favPerson.face);
                        }
                    }
                }*//*

                peopleLastCameraFrame.clear();
                peopleLastCameraFrame.addAll(peopleThisCameraFrame);
                kitty.reCalcFace();
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception " + e.getMessage());
            System.gc();
            return null;
        }*/

        inputFrame.rgba().copyTo(mRgba);
        Core.flip(mRgba.t(), mRgba, 0);

        //saveMat(imageCaptureDirectory + "/image_" + (frameNumber++) + ".jpg", mRgba);
        //framesForVideo.add(ImageUtil.CopyMatToIplImage(mRgba));
        //framesForVideo.add(ImageUtil.OpenCVMatToJavaCVMat(mRgba));

        if (trackingRed) {
            Point relRedObjectPos = trackColor(inputFrame, redValues);
            reactToRedObject(relRedObjectPos);
        }
        if (trackingGreen) {
            Point relGreenObjectPos = trackColor(inputFrame, greenValues);
            reactToGreenObject(relGreenObjectPos);
//        if (relGreenObjectPos != null) {
//            double len = Math.sqrt(Math.pow(relGreenObjectPos.x, 2) + Math.pow(relGreenObjectPos.y, 2));
//            if (len == 0.0) return mRgba;
//            Point norm = relGreenObjectPos;
//            setTextFieldText("pX = " + norm.x, debug1);
//            setTextFieldText("pY = " + norm.y, debug2);
//            //Log.i(TAG, "object is " + relGreenObjectPos.x + ", " + relGreenObjectPos.y + " from the center");
//        }
        }
        /*Point relBlueObjectPos = trackColor(inputFrame, blueValues);
        reactToBlueObject(relBlueObjectPos);*/

        return mRgba;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // COLOR TRACKING
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Make the cat react to a red object.
     * @param relRedObjectPos The relative position of the red object.
     */
    private void reactToRedObject(Point relRedObjectPos) {
        if (relRedObjectPos == null) return;

        Log.i(TAG, "red rel pos: " + relRedObjectPos);
        virtualCat.lookAwayFrom(relRedObjectPos);
    }

    /**
     * Make the cat react to a green object.
     * @param relGreenObjectPos The relative position of the green object.
     */
    private void reactToGreenObject(Point relGreenObjectPos) {
        if (relGreenObjectPos == null) return;

        Log.i(TAG, "green rel pos: " + relGreenObjectPos);
        //virtualCat.lookToward(new Point(relGreenObjectPos.y, relGreenObjectPos.x));
        virtualCat.lookToward(relGreenObjectPos);
    }

    /**
     * Track an object by color.
     * @param inputFrame The image to process.
     * @param cv The min/max HSV values to see.
     * @return a Point position of object or null if no object found normalized to range [-0.5, 0.5]
     */
    private Point trackColor(CameraBridgeViewBase.CvCameraViewFrame inputFrame, ColorValues cv) {
        if (cv == null)
            return null;
        Mat imgThresholded = null;
        Point objectCoords = null;
        try {
            inputFrame.rgba().copyTo(mRgbaForColorTracking);

            // resize to transpose dimensions because reasons. not necessary with opencv 3.1.0
            Imgproc.resize(mRgbaForColorTracking, mRgbaForColorTracking, mRgbaForColorTracking.t().size());

            imgThresholded = new Mat();
            Imgproc.cvtColor(mRgbaForColorTracking, imgThresholded, Imgproc.COLOR_RGB2HSV); //Convert the captured frame from BGR to HSV

            Core.inRange(imgThresholded, new Scalar(cv.lowH, cv.lowS, cv.lowV), new Scalar(cv.highH, cv.highS, cv.highV), imgThresholded); //Threshold the image

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
                    // compute relative position of the object
                    objectCoords = new Point();
                    // all weird because the image is transposed and resized
                    objectCoords.y = -(posX - mRgbaForColorTracking.width() / 4.0f) / (mRgbaForColorTracking.width() / 2.0f);
                    objectCoords.x = -(posY - mRgbaForColorTracking.height() / 2.0f) /  mRgbaForColorTracking.height();

                    Log.i(TAG, "I SEE A COLOR OBJECT");
                }
            }

        } catch (Exception e) {
            Log.i(TAG, "Exception " + e.getMessage());
        }

        if (imgThresholded != null) {
            // free imgThresholded resources
            imgThresholded.release();
        }

        return objectCoords;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FACE DETECTION
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int checkForRecognition(Mat face)
    {
        Random rand = new Random();
        if (IDcount<=21)return 0;
        Mat check = face.clone();
        Imgproc.resize(check, check, stds);
        opencv_core.Mat temp = ImageUtil.convert(check);
        int ID = faceRecognizer.predict(temp);

        if(ID>20) {
            face.copyTo(TrainingSets.get(ID).get(rand.nextInt(TrainingSets.get(ID).size())));
//            refreshRecognizer++;
//            if(refreshRecognizer>100){
//
//                resetRecognizer();
//                refreshRecognizer=0;
//            }

            return ID;
        }
        return 0;

    }

    /*private void trackFavFace(Rect faceRect) {
        mFaceRectLogger.addRecordToLog(faceRect.x + ", " + faceRect.y + ", " + faceRect.width + ", " + faceRect.height);

        int sumX = faceRect.x + faceRect.width / 2;
        int sumY = faceRect.y + faceRect.height / 2;
        for (int i = FavFaceLocationBuffer.length - 1; i > 0; i--) {

            FavFaceLocationBuffer[i] = FavFaceLocationBuffer[i - 1];
            sumX = sumX + FavFaceLocationBuffer[i].x + FavFaceLocationBuffer[i].width / 2;
            sumY = sumY + FavFaceLocationBuffer[i].y + FavFaceLocationBuffer[i].height / 2;

        }

        FavFaceLocationBuffer[0] = faceRect;

        if (FavFaceLocationBuffer[FavFaceLocationBuffer.length - 1].size().area() < 2) return;
        int AvgX = sumX / FavFaceLocationBuffer.length;
        int AvgY = sumY / FavFaceLocationBuffer.length;

        double pX = (double) AvgX / (double) mRgba.width();
        double pY = (double) AvgY / (double) mRgba.height();

        if (pX < 0.42) pololu.cameraYawSpeed(0.5f - (float) pX);
        else if (pY < 0.42) pololu.cameraPitchSpeed(-0.5f + (float)pY);
        else if (pX > 0.58) pololu.cameraYawSpeed(0.5f - (float)pX);
        else if (pY > 0.58) pololu.cameraPitchSpeed(-0.5f + (float) pY);

        setTextFieldText("pX = " + pX + "   " + (0.5f - (float) pX), debug1);
        setTextFieldText("pY = " + pY + "   " + (0.5f - (float) pY), debug2);
        //else pololu.stopNeckMotors();
    }*/

    public boolean adjustFaceBuffer(Rect faceRect)
    {
        int sumX = faceRect.x+faceRect.width/2;
        int sumY = faceRect.y+faceRect.height/2;
        int sumA = (int)faceRect.size().area();
        for(int i=FaceLocationBuffer.length-1;i>0;i--)
        {
            FaceMatBuffer[i - 1].copyTo(FaceMatBuffer[i]);
            FaceLocationBuffer[i]= FaceLocationBuffer[i-1];
            sumX = sumX + FaceLocationBuffer[i].x+FaceLocationBuffer[i].width/2;
            sumY = sumY + FaceLocationBuffer[i].y+FaceLocationBuffer[i].height/2;
            sumA = sumA + (int)FaceLocationBuffer[i].size().area();
        }

        FaceLocationBuffer[0]=faceRect;
        mRgba.submat(faceRect).copyTo(FaceMatBuffer[0]);
        Imgproc.cvtColor(FaceMatBuffer[0], FaceMatBuffer[0], Imgproc.COLOR_RGB2GRAY);

        if(FaceLocationBuffer[FaceLocationBuffer.length-1].size().area()<2)return false;
        int AvgX = sumX/ FaceLocationBuffer.length;
        int AvgY = sumY/ FaceLocationBuffer.length;
        int AvgA = sumA/ FaceLocationBuffer.length;

        int count = 0;
        for(int i=0;i< FaceLocationBuffer.length;i++)
        {
            double dist = Math.sqrt(Math.pow(FaceLocationBuffer[i].x+FaceLocationBuffer[i].width/2-AvgX,2)+Math.pow(FaceLocationBuffer[i].y+FaceLocationBuffer[i].height/2-AvgY,2));
            if (dist<20) {
                double areaChange = Math.abs(FaceLocationBuffer[i].size().area() / AvgA - 1);
                if (areaChange < 0.15)
                    count++;
            }
        }

        int j=0;
        if (count>=EigenMats.length) {
            for (int i = 0; i < FaceLocationBuffer.length; i++) {
                if (j>=EigenMats.length) break;
                if (Math.sqrt(Math.pow(FaceLocationBuffer[i].x+FaceLocationBuffer[i].width/2 - AvgX, 2) + Math.pow(FaceLocationBuffer[i].y+FaceLocationBuffer[i].height/2 - AvgY, 2)) < 20)
                    if (Math.abs(FaceLocationBuffer[i].size().area() / AvgA - 1) < 0.15) {
                        FaceMatBuffer[i].copyTo(EigenMats[j]);
                        j++;
                    }
            }

            for(int i = 0;i<FaceLocationBuffer.length;i++)
            {
                FaceMatBuffer[i].release();
                FaceMatBuffer[i]=new Mat();
                FaceLocationBuffer[i]=new Rect(new Point(0,0),new Size(1,1));
            }

            return true;
        }

        return false;
    }

    /*public void turnCamera(Directions d)
    {
        dir = d;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<4;i++)arrows[i].setVisibility(View.INVISIBLE);

                switch (dir) {
                    case UP:
                        arrows[0].setVisibility(View.VISIBLE);
                        pololu.cameraPitchSpeed(0.05f);
                        break;
                    case RIGHT:
                        pololu.cameraYawSpeed(-0.05f);
                        arrows[1].setVisibility(View.VISIBLE);
                        break;
                    case DOWN:
                        pololu.cameraPitchSpeed(-0.05f);
                        arrows[2].setVisibility(View.VISIBLE);
                        break;
                    case LEFT:
                        pololu.cameraYawSpeed(0.05f);
                        arrows[3].setVisibility(View.VISIBLE);
                        break;
                    default:
                        pololu.stopNeckMotors();
                }
            }
        });
    }*/

    private void addNewUser() {
        Random rand = new Random();
        UserColors.add(IDcount, new Scalar(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)));
        Log.i(TAG, "Adding New User with color " + UserColors.get(IDcount).toString() + " and ID " + IDcount);

        int count = 0;

        TrainingSets.add(IDcount, new ArrayList<Mat>());

        for(int i=0; i<EigenMats.length;i++)
        {
            Imgproc.resize(EigenMats[i], EigenMats[i], stds);
            TrainingSets.get(IDcount).add(i, EigenMats[i].clone());

        }
        int k = 0;

        for(int i=1;i<TrainingSets.size();i++)
        {
            for(int j=0;j<TrainingSets.get(i).size();j++)
            {
                count++;
            }
        }

        opencv_core.MatVector TrainingMats = new opencv_core.MatVector(count);
        opencv_core.Mat labels = new opencv_core.Mat(count,1,opencv_core.CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();

        for(int i=1;i<TrainingSets.size();i++)
        {
            for(int j=0;j<TrainingSets.get(i).size();j++)
            {
                Imgproc.resize(TrainingSets.get(i).get(j),TrainingSets.get(i).get(j),stds);
                opencv_core.Mat temp = ImageUtil.convert(TrainingSets.get(i).get(j));

                if (temp == null) {
                    Log.i(TAG, "null image in training set");
                    continue;
                }

                TrainingMats.put(k,temp);
                labelsBuf.put(k,i);
                k++;
            }
        }
        //faceRecognizer.clear();

        faceRecognizer.train(TrainingMats, labels);

        IDcount++;
    }

    private void loadTestFaces() {
        // for each set
        for (int i = 1; i <= 20; i++) {
            TrainingSets.add(i, new ArrayList<Mat>());

            Log.d(TAG, "Processing person " + i);

            // for each image in set
            for(int j = 0; j <= 4; j++) {
                // load image bitmap
                Bitmap load = ImageUtil.GetBitmapFromContextAssets(getApplicationContext(), "FaceCases/" + i + "-" + j + ".jpg");

                // convert to opencv mat
                TrainingSets.get(i).add(j, new Mat());
                Utils.bitmapToMat(load, TrainingSets.get(i).get(j));
                Imgproc.cvtColor(TrainingSets.get(i).get(j), TrainingSets.get(i).get(j), Imgproc.COLOR_RGB2GRAY);
                Imgproc.equalizeHist(TrainingSets.get(i).get(j), TrainingSets.get(i).get(j));

                // detect faces
                if (mJavaDetectorFace != null)
                    mJavaDetectorFace.detectMultiScale(TrainingSets.get(i).get(j), faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

                // find biggest face
                Rect biggestFace = new Rect(new Point(0, 0), new Size(1, 1));
                Rect[] facesArray = faces.toArray();
                if (facesArray.length > 0) {
                    for (int k = 0; k < facesArray.length; k++) {
                        Rect r = facesArray[k];
                        if (r.size().area() > biggestFace.size().area())
                            biggestFace = r;
                    }

                    // replace the image containing the face with its subimage containing only the face
                    TrainingSets.get(i).get(j).submat(biggestFace).copyTo(TrainingSets.get(i).get(j));

                    Imgproc.resize(TrainingSets.get(i).get(j), TrainingSets.get(i).get(j), stds);
                }
                else
                {
                    TrainingSets.get(i).get(j).submat(0, 80, 0, 80).copyTo(TrainingSets.get(i).get(j));
                }

                bmp = null;
                Mat m = TrainingSets.get(i).get(j);
                // pretty sure this is intentional (rows and cols swapped)
                Mat tmp = new Mat (m.cols(), m.rows(), CvType.CV_8UC1, new Scalar(4));
                try {
                    Imgproc.cvtColor(m, tmp, Imgproc.COLOR_GRAY2RGBA);
                    bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(tmp, bmp);
                }
                catch (CvException e){Log.d("Exception", e.getMessage());}
            }
            UserColors.add(IDcount, new Scalar(0, 0, 0));
            IDcount++;
        }
    }
}