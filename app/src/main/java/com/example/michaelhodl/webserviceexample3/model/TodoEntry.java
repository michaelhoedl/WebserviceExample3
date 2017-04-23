package com.example.michaelhodl.webserviceexample3.model;

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
}
