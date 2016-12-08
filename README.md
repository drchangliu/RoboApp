# RoboApp Fall 2016
## Members
* Kurtis Davis
* Alex Vild
* Luke Shays
* Quintin Fettes
* Seattle Ruzek
* Zach Taylor



# RoboApp readme file from last year's project
## Team project for CS 4560

![alt tag](https://scan.coverity.com/projects/8169/badge.svg?flat=1)

The project, led by <a href="https://www.ohio.edu/engineering/about/people/profiles.cfm?profile=zhuj">Dr. Jim Zhu</a>, has the goal of creating a sentient robotic bobcat.
The end product will be a robotic bobcat which can navigate an environment based on sight.  The cat, called Robocat from here, will be able to find food, in the form of a charging station, as well as form affinities for people it is familiar with.  Robocat, beyond visual input, will also  accept audio input.  Robocat will be able to recognize and react to  its own name as well as several other phrases and the volume of the audio.

##Current State
RoboCat is able to accurately track faces. This is done at all times while the app is at the behavior screen. 
While connected to the cat, the cat will turn its "head" to center the biggest, most centered face
in its camera view. Note: this is currently set to work with the front camera of a phone shooting
a 1024x768 image at 30fps. These values can be modified in createCameraSource() in fdActivity.java.


RoboCat is able to respond to voice commands after the touch of a button.  it also currently has basic color recognition and facial detection provided through <a href="http://opencv.org/">OpenCV.</a>

Also currently available in the app are activities which allow terminal contact with the pololu, for log purposes, and to manually send values to the servos (The servos are what allow the bobcat to maneuver through the environment).

##Organization of Repo
`./doc` contains the documentation of all scripts written for FaceTrackTest

`./RoboApp` is the actual Roboapp and contains all code.
Color Tracking and Face Tracking: located in `./RoboApp/app/src/main/java/robodoot/RoboApp/FdActivity.java`

`./legacy_files` contains inherited files from previous teams with little use or no known use. See README in that directory

`./proj_doc` contains all non-code documents relating to our software development cycle. See README in that directory

`./FaceTracker` is a seperate app, composed of started and custom code, written to track multiple faces in a video feed. Also incorporated into the main RoboApp and is able to be used. Currently, it detects whether each eye is open and uses facial markers to determine happiness.

`./BatteryInformation` is another standalone app used to detect the level of battery charge on the Android device, it is already incorporated into the main RoboApp.
