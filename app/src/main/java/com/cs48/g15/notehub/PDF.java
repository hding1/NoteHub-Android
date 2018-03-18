
package com.cs48.g15.notehub;

/**
 * Created by Delin Sun on 2/25/2018.
 */

public class PDF {
    public String filename;
    public String tag;
    public String description;
    public String url;
    public int year;
    public int month;
    public int day;

    public PDF(){}

    public PDF(String filename, String tag, String description, String url, int year, int month, int day){
        this.filename = filename;
        this.tag = tag;
        this.description = description;
        this.url = url;
        this.year = year;
        this.month = month;
        this.day = day;
    }
}
