package com.cs48.g15.notehub;

/**
 * Created by Delin Sun on 2/25/2018.
 */

public class PDF {
    public String filename;
    public String tag;
    public String description;
    public int year;
    public int month;

    public PDF(){}

    public PDF(String filename, String tag, String description, int year, int month){
        this.filename = filename;
        this.tag = tag;
        this.description = description;
        this.year = year;
        this.month = month;
    }
}
