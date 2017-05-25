package com.example.campus02.webserviceexample3.model;

import android.text.TextUtils;
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
    private String sessionKey;

    public TodoEntry() {
    }


    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
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

    public Boolean getDoneBoolean() {
        return done == 1;
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
                ", sessionKey=" + sessionKey +
                '}';
    }


    //----------------------------------------------------------------------------------------------


    /**
     * get the createdate as a string with a given dateformat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
     * @return
     */
    public String getCreatedateFormatted() {
        SimpleDateFormat dt1 = new SimpleDateFormat(/*"dd.MM.yyyy"*/ "yyyy-MM-dd'T'HH:mm:ss.SSS"); // oder dieses format: yyyy-MM-dd'T'HH:mm:ss
        if(createdate != null)
            return dt1.format(createdate);
        else
            return null;
    }

    /**
     * set the createdate from a string with a given dateformat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
     * for example, we get a date like "2017-04-23T16:05:07.3" from the webservice.
     * @param createdateAsString
     */
    public void setCreatedateAsString(String createdateAsString) {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        if(!TextUtils.isEmpty(createdateAsString)) {
            try {
                this.createdate = dt1.parse(createdateAsString);
            } catch (java.text.ParseException e){
                e.printStackTrace();
            }
        }
        Log.e(null, "setCreatedateAsString: createdate="+createdate+", id="+this.id);
    }

    /**
     * get the duedate as a string with a given dateformat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
     * @return
     */
    public String getDuedateFormatted() {
        SimpleDateFormat dt1 = new SimpleDateFormat(/*"dd.MM.yyyy"*/ "yyyy-MM-dd'T'HH:mm:ss.SSS"); // oder dieses format: yyyy-MM-dd'T'HH:mm:ss
        if(duedate != null)
            return dt1.format(duedate);
        else
            return null;
    }

    /**
     * set the duedate from a string with a given dateformat ("yyyy-MM-dd'T'HH:mm:ss.SSS")
     * for example, we get a date like "2017-04-23T16:05:07.3" from the webservice.
     * @param duedateAsString
     */
    public void setDuedateAsString(String duedateAsString) {
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        if(!TextUtils.isEmpty(duedateAsString)) {
            try {
                this.duedate = dt1.parse(duedateAsString);
            } catch (java.text.ParseException e){
                e.printStackTrace();
            }
        }
        Log.e(null, "setDuedateAsString: duedate="+duedate+", id="+this.id);
    }


    /**
     * convert a string into a date with a given dateformat
     * @param date
     * @param format
     * @return
     * @throws ParseException
     */
    public Date string2date (String date, String format) throws ParseException
    {
        Date d = new Date();
        if (!TextUtils.isEmpty(date)) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                d = formatter.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return d;
    }


    /**
     * convert a date into a string with a given dateformat
     * @param date
     * @param format
     * @return
     * @throws ParseException
     */
    private String date2string (Date date, String format) throws ParseException
    {
        String d = "";
        if(date != null ) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(format);
                d = formatter.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return d;
    }



}
