package com.example.campus02.webserviceexample3.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.example.campus02.webserviceexample3.activities.AllTodosActivity;
import com.example.campus02.webserviceexample3.model.SyncTodoEntry;


import java.util.ArrayList;

/**
 * Created by michaelhodl on 28.05.17.
 */

public class SyncHelper {

    private String TAG = SyncHelper.class.getSimpleName();
    private ProgressDialog pDialog;
    private AllTodosActivity mainDialog;
    private String sessionId;
    private String httpResponse = null;
    private String url;
    private DBHandler localDb;
    private SyncTodoEntry synctodo;

    public SyncHelper(AllTodosActivity mainDialog, String mysession) {
        this.mainDialog = mainDialog;
        this.sessionId = mysession;
        localDb = new DBHandler(mainDialog);
    }

    public void setSynctodo(SyncTodoEntry synctodo) {
        this.synctodo = synctodo;
    }

    public SyncTodoEntry getSynctodo() {
        return synctodo;
    }

    public String getHttpResponse() {
        return httpResponse;
    }

    /**
     * Diese Methode ruft den AsyncTask auf
     */
    private void runAsync()
    {
        new SyncHelper.AsyncCaller(this).execute();
    }

    public boolean runSyncAction() {
        return runSync();
    }

    private boolean runSync() {
        runAsync();
        return httpResponse != null;
    }


    /**
     * Async task class
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        SyncHelper caller;
        boolean isInternetConnected;
        HttpHandler sh;

        AsyncCaller(SyncHelper caller){
            this.caller = caller;
            sh = new HttpHandler();
            isInternetConnected = sh.isNetworkAvailable(mainDialog.getApplicationContext());
        }

        @Override
        //this method will be running on UI thread
        protected void onPreExecute() {
            super.onPreExecute();

            // Showing progress dialog
            pDialog = new ProgressDialog(mainDialog);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        //this method will be running on background thread so dont update UI from here
        //do your long running http tasks here, you dont want to pass argument and u can access the parent class variable url over here
        protected Void doInBackground(Void... arg0) {

            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                // Extrahieren der NameValuePairs für die Header aus dem String.
                // Die Headers sind im String so vorhanden: "'Content-Type:application/json;session:xxxx;Accept:application/json;'"
                ArrayList<NameValuePair> a1 = new ArrayList<NameValuePair>();
                String headers = caller.getSynctodo().getHeaders();
                if (!TextUtils.isEmpty(headers)) {
                    String[] data1 = headers.split(";");
                    for (String s : data1) {
                        NameValuePair np1 = new NameValuePair();
                        String[] s1 = s.split(":");
                        np1.setName(s1[0]);
                        np1.setValue(s1[1]);
                        a1.add(np1);
                    }
                }

                // Extrahieren der NameValuePairs für die Params aus dem String.
                ArrayList<NameValuePair> a2 = new ArrayList<NameValuePair>();
                String parameters = caller.getSynctodo().getParams();
                if(!TextUtils.isEmpty(parameters)) {
                    String[] data2 = parameters.split(";");
                    for (String s : data2) {
                        NameValuePair np2 = new NameValuePair();
                        String[] s2 = s.split(":");
                        np2.setName(s2[0]);
                        np2.setValue(s2[1]);
                        a1.add(np2);
                    }
                }

                // Webservice Aufruf mit den Daten aus dem SyncTodoEntry
                // Aufruf um Daten an Datenbank zu übergeben
                String jsonStr = sh.makeMyServiceCall(
                        caller.getSynctodo().getUrl(),
                        caller.getSynctodo().getCmd(),
                        a1,
                        a2,
                        caller.getSynctodo().getJsonPostStr()
                );
                // fill the httpResponse with the json string. If the response is null there was a problem at the server, if it is empty the request was successful
                httpResponse = jsonStr;

                Log.e(TAG, "Update Response from url (jsonStr) update action: " + jsonStr);
                Log.e(TAG, "Update Response from url (httpResponse) update action: " + httpResponse);
                return null;
            }
            return null;
        }

        @Override
        //this method will be running on UI thread
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Log.e(TAG, "status: (im onPostExecute) complete action: " + this.getStatus());
        }

    } // end private class AsyncCaller


}
