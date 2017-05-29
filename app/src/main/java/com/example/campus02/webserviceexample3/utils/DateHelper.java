package com.example.campus02.webserviceexample3.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by michaelhodl on 29.05.17.
 */

public final class DateHelper {

    private DateHelper() {
    }

    /**
     * Man bekommt als Parameter ein Datum als String übergeben.
     * Diese Datum ist in einem Format wie z.B. "2017-05-25T11:38:45.117"
     * Es sollen nun nur die ersten 19 Zeichen zurückgegeben werden,
     * also nur bis zu den Sekunden. Die Milisekunden interessieren uns somit nicht.
     * @param datestring
     * @return
     */
    public static String prepareDateString(String datestring){
        String s = "";
        if(!TextUtils.isEmpty(datestring) && !datestring.equals("null")){
            s = datestring.substring(0,19);
        }
        return s;
    }

    /**
     * Datum zu String umwandeln,
     * und im Format "yyyy-MM-dd'T'HH:mm:ss" zurückliefern
     * @param mydate
     * @return
     */
    public static String date2string (Date mydate){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String s = "";
        if (mydate != null) {
            s = dt1.format(mydate);
        } else
            s = "";
        return s;
    }

    /**
     * String (im Format "yyyy-MM-dd'T'HH:mm:ss") zu einem Datum umwandeln
     * @param mystring
     * @return
     */
    public  static Date string2date (String mystring){
        SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date d = new Date();
        if(!TextUtils.isEmpty(mystring) && !mystring.equals("null") && !mystring.equals("")) {
            try {
                // zuvor den String auf 19 Zeichen kürzen, damit er wirklich im gewünschten Format "yyyy-MM-dd'T'HH:mm:ss" ist
                d = dt1.parse(prepareDateString(mystring));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else
            d = null;

        return d;
    }

    /**
     * String wird in einfachem Format ("dd.MM.yyyy") übergeben,
     * und zu einem Datum umgewandelt
     * @param mystring
     * @return
     */
    public static Date string2dateSimple(String mystring) {
        SimpleDateFormat dt1 = new SimpleDateFormat("dd.MM.yyyy");
        Date d = new Date();

        if(!TextUtils.isEmpty(mystring) && !mystring.equals("null") && !mystring.equals("")) {
            try {
                d = dt1.parse(mystring);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else
            d = null;

        return d;
    }

    /**
     * Wandelt ein Datum um zu einem String in einem einfachen Format ("dd.MM.yyyy")
     * @param mydate
     * @return
     */
    public static String date2stringSimple(Date mydate){
        SimpleDateFormat dt1 = new SimpleDateFormat("dd.MM.yyyy");
        String s = "";
        if(mydate != null) {
            s = dt1.format(mydate);
        } else
            s = "";
        return s;
    }



}
