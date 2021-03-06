package com.example.campus02.webserviceexample3.utils;

/**
 * Created by michaelhodl on 12.04.17.
 *
 * This class is used to represent a "name value pair" entry.
 * This is useful for setting the headers for the HTTP requests
 *   (for example, a HTTP header "Accept" could have a value "text/plain").
 * (Alternatively you could use a HashMap with key/value.)
 */

public class NameValuePair {
    private String name;
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "[" + name + ":" + value + "]";
    }

}