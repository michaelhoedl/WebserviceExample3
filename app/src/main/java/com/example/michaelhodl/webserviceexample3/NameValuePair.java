package com.example.michaelhodl.webserviceexample3;

/**
 * Created by michaelhodl on 12.04.17.
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