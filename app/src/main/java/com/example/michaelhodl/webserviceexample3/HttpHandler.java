package com.example.michaelhodl.webserviceexample3;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by Ravi Tamada on 01/09/16.
 * based on: www.androidhive.info
 *
 * good example: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
 */
public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() {
    }

    /**
     * Method to convert an Input Stream from the Webservice into a String.
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    /**
     *
     * Ein Webservice aufrufen, wobei man dynamisch die Url, die Methode, die Header und die Parameter angeben kann.
     *
     * @param reqUrl
     * @param reqMethod
     * @param headers
     * @param params
     * @return
     */
    public String makeMyServiceCall(String reqUrl,
                                    String reqMethod,
                                    ArrayList<NameValuePair> headers,
                                    ArrayList<NameValuePair> params ) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(reqMethod);


            // add parameters
            String combinedParams = "";
            if (params!=null)
            {
                combinedParams += "?";
                for (NameValuePair p : params)
                {
                    String paramString = p.getName() + "=" + URLEncoder.encode((String) p.getValue(),"UTF-8");
                    if (combinedParams.length() > 1)
                        combinedParams += "&" + paramString;
                    else
                        combinedParams += paramString;
                }
            }

            // add headers
            if (headers!=null)
            {
                for (NameValuePair h : headers)
                    conn.setRequestProperty(h.getName(), (String) h.getValue());
            }


            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            response = convertStreamToString(in);
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
        return response;
    }

}