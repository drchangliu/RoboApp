package com.robodoot.dr.RoboApp;

/**
 * Created by john on 3/24/16.
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;

import android.os.Environment;
import android.util.Log;

/**
 * @author Rakesh.Jha
 * Date - 07/10/2013
 * Definition - Logger file use to keep Log info to external SD with the simple method
 * Modified by Alex Bagnall
 */

public class Logger {
    private String mLogName;
    private boolean append = true;

    static boolean isExternalStorageAvailable;
    static boolean isExternalStorageWriteable;
    static String state;
    static String directory;

    public Logger(String logName) {
        mLogName = logName; init();
    }
    public Logger(String logName, boolean app) {
        mLogName = logName; append = app; init();
    }

    private void init() {
        state = Environment.getExternalStorageState();
        directory = Environment.getExternalStorageDirectory().getPath() + "/RoboApp";

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            isExternalStorageAvailable = isExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            isExternalStorageAvailable = true;
            isExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            isExternalStorageAvailable = isExternalStorageWriteable = false;
        }
    }

    public void addRecordToLog(String message) {
        File dir = new File(directory);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if(!dir.exists()) {
                Log.d("Dir created ", "Dir created ");
                dir.mkdirs();
            }

            File logFile = new File(directory + "/" + mLogName + ".txt");

            if (!logFile.exists())  {
                try  {
                    Log.d("File created ", "File created ");
                    logFile.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            BufferedWriter buf = null;
            try {
                //BufferedWriter for performance, true to set append to file flag
                buf = new BufferedWriter(new FileWriter(logFile, append));

                buf.write(message);
                //buf.append(message);
                buf.newLine();
                buf.flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                if (buf != null) buf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String[] ReadLines() {
        File dir = new File(directory);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (!dir.exists()) {
                return null;
            }

            File logFile = new File(directory + "/" + mLogName + ".txt");

            if (!logFile.exists()) {
                return null;
            }

            ArrayList<String> lines = new ArrayList<>();

            try {
                BufferedReader br = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
                br.close();
            } catch (Exception e) {
                return null;
            }

            return lines.toArray(new String[lines.size()]);
        }
        return null;
    }
}