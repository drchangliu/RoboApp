package com.robodoot.roboapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.robodoot.dr.RoboApp.AnalyticsApplication;
import com.robodoot.dr.facetracktest.R;

public class MainActivity extends FragmentActivity implements
        NavigationDrawerCallbacks, HomeFragment.OnFragmentInteractionListener,
        ReadmeFragment.OnFragmentInteractionListener,
        CompTestFragment.OnFragmentInteractionListener,
        ConsoleFragment.OnFragmentInteractionListener,
        AccelerometerFragment.OnFragmentInteractionListener{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    public static int log_count = 0;
    public static final int LOG_SIZE = 100;
    public static String[] logEntries = new String[LOG_SIZE]; // This value may need to be changed so all entries fit on screen

    //TODO: Analytics Code
    private com.google.android.gms.analytics.Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO: Analytics Code
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);



        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer), mToolbar);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new HomeFragment());
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        //TODO: Analytics Code
        mTracker.setScreenName("Image~" + "Behavior Mode");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the fragment_camerapreview content by replacing fragments

        Fragment fragment = null;
        Intent intent = null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch(position) {
            case 0:
                fragment = new ConsoleFragment();
                //Toast.makeText(this, "Console", Toast.LENGTH_SHORT).show();
                //TODO: Analytics Code
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Open Console")
                        .build());
                break;
            case 1:
                this.finish();
                //TODO: Analytics Code
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Return to Behavior Mode")
                        .build());
                break;
            case 2:
                log_console("Face Tracking");
                //TODO: Analytics Code
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Open View Face Tracking Mode")
                        .build());
                intent = new Intent("com.google.android.gms.samples.vision.face.facetracker.FaceTrackerActivity");
                break;
            case 3:
                log_console("Readme Displayed");
                //TODO: Analytics Code
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("View Readme")
                        .build());
                fragment = new ReadmeFragment();
                break;
            case 4:
                log_console("Accelerometer Data Displayed");
                //TODO: Analytics Code
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Open Acellerometer Log")
                        .build());
                fragment = new AccelerometerFragment();
                break;
            default:
                break;

        }

        if (intent != null) {
            startActivity(intent);
        }
        else if (fragment != null) {
            transaction.replace(R.id.container, fragment);
            //transaction.addToBackStack(null);
            transaction.commit();
        }
        else {
            //Toast.makeText(this, "No associated fragment for the selected menu item.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen())
            mNavigationDrawerFragment.closeDrawer();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String stredittext = data.getStringExtra("edittextvalue");
                Bundle bndle = new Bundle();
                bndle.putString("txt", stredittext);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                CompTestFragment cn = new CompTestFragment();
                cn.setArguments(bndle);
                transaction.replace(R.id.container, cn);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }


    public void log_console(String in){
        logEntries[log_count++%LOG_SIZE]=in;
        return;
    }
}


