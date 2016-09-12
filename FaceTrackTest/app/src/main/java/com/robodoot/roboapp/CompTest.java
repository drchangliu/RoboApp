package com.robodoot.roboapp;

/**
 * Created by John on 11/17/2015.
 */
public class CompTest{

    private long id;
    private String title;
    private String description;


    public String getTitle(){ return title; }
    public String getDesc(){ return description; }
    public long getId(){ return id; }

    public void setTitle    (String title)       { this.title = title; }
    public void setDesc     (String description) { this.description = description; }
    public void setId       (long insertId)      { this.id = insertId; }

}
