package com.example.campus02.webserviceexample3.model;

/**
 * Created by andreas on 12.05.2017.
 */

public class UserEntry {
    private String mail;
    private String pwd;
    private String sessionKey;

    public UserEntry() {
    }

    public UserEntry(String mail, String pwd, String sessionKey) {
        this.mail = mail;
        this.pwd = pwd;
        this.sessionKey = sessionKey;
    }

    public String getMail() {
        return mail;
    }

    public String getPwd() {
        return pwd;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
