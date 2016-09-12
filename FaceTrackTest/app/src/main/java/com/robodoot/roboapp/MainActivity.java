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
import android.widget.Toast;

import com.robodoot.dr.facetracktest.R;

public class MainActivity extends FragmentActivity implements
        NavigationDrawerCallbacks, HomeFragment.OnFragmentInteractionListener,
        ReadmeFragment.OnFragmentInteractionListener,
        CompTestFragment.OnFragmentInteractionListener,
        ConsoleFragment.OnFragmentInteractionListener{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Toolbar mToolbar;

    public static int log_count = 0;
    public static final int LOG_SIZE = 100;
    public static String[] logEntries = new String[LOG_SIZE]; // This value may need to be changed so all entries fit on screen





    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments




        Fragment fragment = null;
        Intent intent = null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        switch(position) {
            /*case 1:
                fragment = new CompTestFragment();
                Toast.makeText(this, "Unit Testing", Toast.LENGTH_SHORT).show();
                break;*/
            /*case 0:
                fragment = new HomeFragment();
                break;*/
            case 0:
                fragment = new ConsoleFragment();
                //Toast.makeText(this, "Console", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                this.finish();
                break;
            case 2:
                intent = new Intent("com.robodoot.dr.RoboApp.ColorTrackingActivity");
                break;
            case 3:
                log_console("Readme Displayed");
                fragment = new ReadmeFragment();
                break;
            //case 5:
              //  intent = new Intent("com.robodoot.dr.RoboApp.Readme");
              //  break;
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
                Log.d("TAG", "Creating New Component Test: " + stredittext);
                CompTestFragment cn = new CompTestFragment();
                cn.setArguments(bndle);
                transaction.replace(R.id.container, cn);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        }
    }
/*
    public void AddMessage(View view) {
        TextView disp = (TextView)findViewById(R.id.sendtext);
        CharSequence curr = disp.getText();
        CharSequence msg = ((EditText)findViewById(R.id.message)).getText();
        String newText = curr.toString() + "\n" + msg.toString();
        char[] newT = newText.toCharArray();
        disp.setText(newT, 0, newT.length);
        ((EditText)findViewById(R.id.message)).setText("");
        return;
    } */


    public void log_console(String in){
        logEntries[log_count++%LOG_SIZE]=in;
        return;
    }
}


