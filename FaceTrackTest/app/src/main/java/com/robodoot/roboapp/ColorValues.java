package com.robodoot.roboapp;

/**
 * Created by alex on 4/19/16.
 */
public class ColorValues {
    public int lowH;
    public int highH;
    public int lowS;
    public int highS;
    public int lowV;
    public int highV;
    public ColorValues(int lH, int hH, int lS, int hS, int lV, int hV) {
        lowH = lH; highH = hH; lowS = lS;
        highS = hS; lowV = lV; highV = hV;
    }
    public ColorValues(String s) {
        String[] tokens = s.split(" ");
        if (tokens.length != 6) {
            throw new IllegalArgumentException("need string containing 6 integer values");
        }
        try {
            lowH = Integer.parseInt(tokens[0]); highH = Integer.parseInt(tokens[1]);
            lowS = Integer.parseInt(tokens[2]); highS = Integer.parseInt(tokens[3]);
            lowV = Integer.parseInt(tokens[4]); highV = Integer.parseInt(tokens[5]);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("need string containing 6 integer values");
        }
    }
}