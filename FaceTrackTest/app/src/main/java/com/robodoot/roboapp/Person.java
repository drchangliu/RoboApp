package com.robodoot.roboapp;

import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.ArrayList;

/**
 * Created by alex on 3/14/16.
 */
public class Person {
    public Rect face;
    public int ID;
    public boolean smiling;
    public boolean frowning;

    public Person(int _ID, Rect _face, boolean _smiling, boolean _frowning){
        ID = _ID;
        face = _face;
        smiling = _smiling;
        frowning = _frowning;
    }

    public void checkID(ArrayList<ArrayList<Integer>> SimilarID, ArrayList<Scalar> UserColors)
    {
        if(this.ID>=SimilarID.size())
        {
            for(int i=SimilarID.size()-1; i<=this.ID;i++)
            {
                SimilarID.add(new ArrayList<Integer>());

            }
        }
        if(SimilarID.get(this.ID).size()>0) {
            int minID = SimilarID.get(this.ID).get(0);
            for (int i = 0; i < SimilarID.get(this.ID).size(); i++) {

                if(SimilarID.get(this.ID).get(i)<minID)
                {
                    minID = SimilarID.get(this.ID).get(i);
                }

            }
            if(this.ID!=minID) {
                if(!UserColors.get(this.ID).equals(UserColors.get(minID)))
                {
                    UserColors.get(this.ID).set(UserColors.get(minID).val);
                }
                this.ID = minID;
            }
        }


    }

    public boolean checkSimilar(Person other, ArrayList<ArrayList<Integer>> SimilarID, ArrayList<Scalar> UserColors){

        if(other.ID==this.ID)return true;

        if(Math.max(other.ID, this.ID)>=SimilarID.size())
        {
            for(int i=SimilarID.size()-1; i<=Math.max(other.ID, this.ID);i++)
            {
                SimilarID.add(new ArrayList<Integer>());

            }
        }
        if(SimilarID.get(this.ID).size()>0) {
            for (int i = 0; i < SimilarID.get(this.ID).size(); i++) {
                if(SimilarID.get(this.ID).get(i)==other.ID)
                {
                    this.ID = Math.min(this.ID, other.ID);
                    other.ID = this.ID;
                    return true;
                }

            }
        }
        if(Util.checkSimilarRect(this.face, other.face))
        {
            String temp = "combined IDs " + this.ID + " and " + other.ID;
            //setTextFieldText(temp, debug3);

            UserColors.get(Math.max(this.ID, other.ID)).set(UserColors.get(Math.min(this.ID, other.ID)).val);
            SimilarID.get(this.ID).add(other.ID);
            if(!SimilarID.get(other.ID).contains(this.ID))SimilarID.get(other.ID).add(this.ID);

            this.ID = Math.min(this.ID, other.ID);
            other.ID = this.ID;

            return true;
        }

        return false;

    }
}
