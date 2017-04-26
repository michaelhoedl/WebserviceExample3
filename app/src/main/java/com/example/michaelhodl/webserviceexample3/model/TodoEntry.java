package com.example.michaelhodl.webserviceexample3.model;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * This class represents a To Do entry.
 *
 * Created by michaelhodl on 19.04.17.
 */

public class TodoEntry {

    private int id;
    private String title;
    private String tododesc;
    private float estimatedeffort;
    private float usedtime;
    private int done;
    private Date createdate;
    private Date duedate;

    public TodoEntry() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTododesc() {
        return tododesc;
    }

    public void setTododesc(String tododesc) {
        this.tododesc = tododesc;
    }

    public float getEstimatedeffort() {
        return estimatedeffort;
    }

    public void setEstimatedeffort(float estimatedeffort) {
        this.estimatedeffort = estimatedeffort;
    }

    public float getUsedtime() {
        return usedtime;
    }

    public void setUsedtime(float usedtime) {
        this.usedtime = usedtime;
    }

    public int getDone() {
        return done;
    }

    public void setDone(int done) {
        this.done = done;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public Date getDuedate() {
        return duedate;
    }

    public void setDuedate(Date duedate) {
        this.duedate = duedate;
    }

    @Override
    public String toString() {
        return "TodoEntry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", tododesc='" + tododesc + '\'' +
                ", estimatedeffort=" + estimatedeffort +
                ", usedtime=" + usedtime +
                ", done=" + done +
                ", createdate=" + createdate +
                ", duedate=" + duedate +
                '}';
    }


    //----------------------------------------------------------------------------------------------


    // get the duedate as a string with a given dateformat
    public String getDuedateFormatted() {
        SimpleDateFormat dt1 = new SimpleDateFormat("dd.mm.yyyy"); // oder dieses format: yyyy-MM-dd'T'HH:mm:ss
        if(duedate != null)
            return dt1.format(duedate);
        else
            return null;
    }

    // set the duedate from a string with a given dateformat
    public void setDuedateAsString(String duedateAsString) {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if(duedateAsString != null && !duedateAsString.isEmpty()) {
            try {
                this.duedate = dt1.parse(duedateAsString);
            } catch (java.text.ParseException e){
                e.printStackTrace();
            }
        }
        Log.e(null, "duedate="+duedate+", id="+this.id);
    }


    // convert a string into a date with a given dateformat
    private Date string2date (String date, String format) throws ParseException
    {
        Date d = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            d = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    // convert a date into a string with a given dateformat
    private String date2string (Date date, String format) throws ParseException
    {
        String d = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            d = formatter.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }



}
