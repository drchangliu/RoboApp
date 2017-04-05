# RoboApp Fall 2016
## Members
* Kurtis Davis
* Alex Vild
* Luke Shays
* Quintin Fettes
* Seattle Ruzek
* Zach Taylor


[Click here to view in the Google Play Store](https://play.google.com/store/apps/details?id=com.robodoot.dr.facetracktest)


# RoboApp readme file
## Team project for CS 4560/4561 (2016-2017)

![alt tag](https://scan.coverity.com/projects/8169/badge.svg?flat=1)

The project, led by <a href="https://www.ohio.edu/engineering/about/people/profiles.cfm?profile=zhuj">Dr. Jim Zhu</a>, has the goal of creating a sentient robotic bobcat.
The end product will be a robotic bobcat which can navigate an environment based on sight.  The cat, called Robocat from here, will be able to find food, in the form of a charging station, as well as form affinities for people it is familiar with.  Robocat, beyond visual input, will also  accept audio input.  Robocat will be able to recognize and react to  its own name as well as several other phrases and the volume of the audio.

## Current State
RoboCat is able to accurately track faces. This is done at all times while the app is at the behavior screen.
While connected to the cat, the cat will turn its "head" to center the biggest, most centered face
in its camera view. Note: this is currently set to work with the front camera of a phone shooting
a 1024x768 image at 30fps. These values can be modified in createCameraSource() in fdActivity.java.

RoboCat can respond to voice commands through the [PocketSphinx API](https://github.com/cmusphinx/pocketsphinx). Additional info regarding the installation and use of this API can be found in /doc/pocketsphinx-voice-recognition.

Also currently available in the app are activities which allow terminal contact with the pololu, for log purposes, and to manually send values to the servos (The servos are what allow the bobcat to maneuver through the environment).

Color Tracking is currently under dvevelopment and will be available soon. The menu can be accessed but is not functional.

## Installation and Running the Application
To build and run this application, you should do the following:
1. `git clone` the master branch of this repo.
2. Download the latest version of [Android Studio](https://developer.android.com/studio/index.html)
3. Open Android studio, and open an existing project
4. Open `RoboApp/RoboApp` inside Android Studio. Make sure the app you open in android studio is **not** just simply the RoboApp directory you just cloned, but rather the RoboApp project file within this application, as otherwise the app will not run. RoboApp the repo is composed of several different android applications that are injected into the main RoboApp app, located at RoboApp/RoboApp.
5. Build and Run the project within android studio either on to your android device or on to a virtual device.

## Getting Started
The majority of the code base takes place in the FdActivity.java file, located at `RoboApp/RoboApp/app/src/main/java/com/robodoot`. This is the best place to start to try to get a handle on the application. From this file, you can see when the other classes are brought in, such as CatEmotion.java, which supplies auxilary functions for dealing with how the cat's mood is displayed.

Additional help for parts of the application can be found in the `./doc` and `./proj_doc` directories.

## Organization of Repo
`./doc` contains legacy documentation that was used when OpenCV was still part of the application. It also includes documentation for using the PocketSphinx voice recognizer to do active listening.

`./RoboApp` is the actual Roboapp and contains all code. **This is the application that is meant to be run on the phone and uploaded to the google store.**.
Color Tracking and Face Tracking: located in `./RoboApp/app/src/main/java/robodoot/RoboApp/FdActivity.java`

`./legacy_files` contains inherited files from previous teams with little use or no known use. See README in that directory

`./proj_doc` contains all non-code documents relating to our software development cycle. See README in that directory

`./FaceTracker` is a seperate app, composed of started and custom code, written to track multiple faces in a video feed. Also incorporated into the main RoboApp and is able to be used. Currently, it detects whether each eye is open and uses facial markers to determine happiness.

`./BatteryInformation` is another standalone app used to detect the level of battery charge on the Android device, it is already incorporated into the main RoboApp.

## Privacy Policy
We require access to these permissions within the application:
`Camera` For facial recognition and color tracking. We do not store any of this data for our own use, the camera just gives information to the code for the cat to track

`Microphone` Pocketsphinx is used within this application to provide always on voice listening. We do not log or store the data that is heard, we just take the commands as strings and use them in our code.

`Storage` Storing data such as accelerometer data or voice commands to the phone for debugging purposes.
