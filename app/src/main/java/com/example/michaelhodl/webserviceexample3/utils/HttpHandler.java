package com.example.michaelhodl.webserviceexample3.utils;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


/**
 * Created by Ravi Tamada on 01/09/16.
 * based on: www.androidhive.info
 * good example: http://www.androidhive.info/2012/01/android-json-parsing-tutorial/
 *
 * Modified by Michael Hoedl
 *
 * This class is used to handle the HTTP requests to/from the werbservice (GET, POST, DELETE, ...)
 */
public class HttpHandler {

    private static final String TAG = HttpHandler.class.getSimpleName();

    public HttpHandler() { }

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
    } // end convertStreamToString


    /**
     *
     * Ein Webservice aufrufen, wobei man dynamisch die Url, die Methode, die Header und die Parameter angeben kann.
     * Wenn POST Request, dann kann auch der POST Inhalt als String (im JSON Format) uebergeben werden.
     *
     * @param reqUrl: the URL of the webservice
     * @param reqMethod: the HTTP method (GET, POST, DELETE, ...)
     * @param headers: the headers for the HTTP request
     * @param params: the parameters (which will be included in the Url, for example "...?parameter=value&parameter2=value2")
     * @param jsonstringpost: the (text) content for the POST request (as a string in json format).
     * @return
     */
    public String makeMyServiceCall( String reqUrl,
                                     String reqMethod,
                                     ArrayList<NameValuePair> headers,
                                     ArrayList<NameValuePair> params,
                                     String jsonstringpost
                                    ) {
        String response = "";
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(reqMethod);

            // add parameters to the header in the HTTP request
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

            // if HTTP POST:
            if (reqMethod.equals("POST")){
                conn.setDoInput(true);
                conn.setDoOutput(true);

                byte[] outputBytes = jsonstringpost.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "HTTP_OK, "+responseCode);

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    Log.e(TAG, "False - responseCode="+responseCode);
                    response = "";
                }
            }
            else if (!reqMethod.equals("POST")) {
                // read the response if it is not a POST request
                InputStream in = new BufferedInputStream(conn.getInputStream());
                response = convertStreamToString(in);
            } // end if

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
    } // end makeMyServiceCall

}