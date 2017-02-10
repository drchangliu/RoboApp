package com.roboapp.batteryinformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;


public class BatteryActivity extends AppCompatActivity {
    private Context mContext;

    private TextView mTextViewInfo;
    private TextView mTextViewPercentage;
    private ProgressBar mProgressBar;

    private int mProgressStatus = 0;
    private float percentage;

    public int getPercentage(){
        return mProgressStatus;
    }

    /*
        BroadcastReceiver
            Base class for code that will receive intents sent by sendBroadcast().

            You can either dynamically register an instance of this class with
            Context.registerReceiver() or statically publish an implementation through
            the <receiver> tag in your AndroidManifest.xml.
    */
    // Initialize a new BroadcastReceiver instance
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*
                BatteryManager
                    The BatteryManager class contains strings and constants used for values in the
                    ACTION_BATTERY_CHANGED Intent, and provides a method for querying battery
                    and charging properties.
            */
            /*
                public static final String EXTRA_SCALE
                    Extra for ACTION_BATTERY_CHANGED: integer containing the maximum battery level.
                    Constant Value: "scale"
            */
            // Get the battery scale
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
            // Display the battery scale in TextView
            mTextViewInfo.setText("Battery Scale : " + scale);

            /*
                public static final String EXTRA_LEVEL
                    Extra for ACTION_BATTERY_CHANGED: integer field containing the current battery
                    level, from 0 to EXTRA_SCALE.

                    Constant Value: "level"
            */
            // get the battery level
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
            // Display the battery level in TextView
            mTextViewInfo.setText(mTextViewInfo.getText() + "\nBattery Level : " + level);

            // Calculate the battery charged percentage
            percentage = level/ (float) scale;
            // Update the progress bar to display current battery charged percentage
            mProgressStatus = (int)((percentage)*100);

            // Show the battery charged percentage text inside progress bar
            mTextViewPercentage.setText("" + mProgressStatus + "%");

            // Show the battery charged percentage in TextView
            mTextViewInfo.setText(mTextViewInfo.getText() +
                    "\nPercentage : "+ mProgressStatus + "%");

            // Display the battery charged percentage in progress bar
            mProgressBar.setProgress(mProgressStatus);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request window feature action bar
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the application context
        mContext = getApplicationContext();

        /*
            IntentFilter
                Structured description of Intent values to be matched. An IntentFilter can match
                against actions, categories, and data (either via its type, scheme, and/or path) in
                an Intent. It also includes a "priority" value which is used to order multiple
                matching filters.

                IntentFilter objects are often created in XML as part of a package's
                AndroidManifest.xml file, using intent-filter tags.
        */
        /*
            public IntentFilter (String action)
                New IntentFilter that matches a single action with no data. If no data
                characteristics are subsequently specified, then the filter will only match intents
                that contain no data.

            Parameters
                action : The action to match, i.e. Intent.ACTION_MAIN.
        */
        // Initialize a new IntentFilter instance
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        /*
            public abstract Intent registerReceiver (BroadcastReceiver receiver, IntentFilter filter)
                Register a BroadcastReceiver to be run in the main activity thread. The receiver will
                be called with any broadcast Intent that matches filter, in the main application thread.

                The system may broadcast Intents that are "sticky" -- these stay around after the
                broadcast as finished, to be sent to any later registrations. If your IntentFilter
                matches one of these sticky Intents, that Intent will be returned by this function
                and sent to your receiver as if it had just been broadcast.

        Parameters
            receiver : The BroadcastReceiver to handle the broadcast.
            filter : Selects the Intent broadcasts to be received.
        Returns
            The first sticky intent found that matches filter, or null if there are none.
        */
        // Register the broadcast receiver
        mContext.registerReceiver(mBroadcastReceiver,iFilter);

        // Get the widgets reference from XML layout
        mTextViewInfo = (TextView) findViewById(R.id.tv_info);
        mTextViewPercentage = (TextView) findViewById(R.id.tv_percentage);
        mProgressBar = (ProgressBar) findViewById(R.id.pb);
    }
}