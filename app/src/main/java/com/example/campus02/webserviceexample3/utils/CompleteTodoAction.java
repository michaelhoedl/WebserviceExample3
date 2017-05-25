package com.example.campus02.webserviceexample3.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.example.campus02.webserviceexample3.activities.AllTodosActivity;
import com.example.campus02.webserviceexample3.model.TodoEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by Markus on 25.04.2017.
 */

public class CompleteTodoAction {
    // Wird für Logging verwendet
    private String TAG = CompleteTodoAction.class.getSimpleName();
    private ProgressDialog pDialog;
    //Dialog, von dem CompleteTodoAction aufgerufen wird
    private AllTodosActivity mainDialog;
    //Attribute, die für Done Setzen benötigt wrden
    private String todoId;
    private String sessionId;
    private String httpResponse = null;
    private String url;
    private String todoName;
    private String todoDescription;
    // Instanz des Datenbank-Handlers für die lokale Datenbank
    private DBHandler localDb;


    //Konstruktor, Parameter für das Ausführen der CompleteTodoAction werden gesetzt
    // Parameter werden aus der übergebenen ToDo von AllTodosActivity, ausgelesen

    public CompleteTodoAction(AllTodosActivity mainDialog, TodoEntry e) {
        this.mainDialog = mainDialog;
        this.todoId = e.getId()+"";
        this.sessionId = e.getSessionKey();
        this.todoName = e.getTitle();
        this.todoDescription = e.getTododesc();
        this.localDb = new DBHandler(mainDialog);
    }

    public boolean runCompleteTodoAction() {
        return runComplete();
    }

    private boolean runComplete() {

        // AsyncTask starten um Todo vom Webservice oder aus der Lokalen DB auf Erledigt zu setzen.
        runAsync();
        return httpResponse != null;
    }

    private void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }


    private void runAsync()
    {
        new AsyncCaller(this).execute();
    }

    /**
     * Async task class complete todo by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        CompleteTodoAction caller;
        boolean isInternetConnected;
        HttpHandler sh;

        AsyncCaller(CompleteTodoAction caller){
            this.caller = caller;
            sh = new HttpHandler();
            //Prüfen ob eine Internetverbindung besteht
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

            // headers - für das Erledigt setzten wird nur der Session Key und der Datentyp benötigt
            // --------- folgend wird der header zusammengebau
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h1 = new NameValuePair();
            h1.setName("Content-Type");
            h1.setValue("application/json");
            NameValuePair h2 = new NameValuePair();
            h2.setName("session");
            h2.setValue(sessionId);
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue("application/json");
            headers.add(h1);
            headers.add(h2);
            headers.add(h3);

            // URL von Todo hinzufügen, wird auf der API benötigt
            url = "http://campus02win14mobapp.azurewebsites.net/Todo";

            // Prüft ob Internetverbindung besteht, wenn ja - Complete setzen wird auf der API durchgeführt
            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                    // Create a JSON Object out of the TodoEntry Object which was created from input data from the EditText-Fields.
                    JSONObject jsonObject = new JSONObject();

                    // Zusammensetzung für Notwendige Dateninformatin, damit die ToDo auf done gesetzt werden kann.
                    //  id, name, decrioption sind notwendige Felder
                    // mit "done",1 wird die ToDo als erledigt an die DB übergeben
                   // Hintergrundfarbe für erledigte ToDo's wird in der Klasse model/TodoListAdapter.java ab Zeile 57- 59 gesetzt
                    try {
                        jsonObject.put("id",todoId);
                        jsonObject.put("name",todoName);
                        jsonObject.put("description",todoDescription);
                        jsonObject.put("done",1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String str = jsonObject.toString();
                 Log.e(TAG, "--- str= "+str);


                // Making a complete request to url and getting response
                // Aufruf um Daten an Datenbank zu übergeben
                String jsonStr = sh.makeMyServiceCall(url, "POST", headers, null, str);//sh.makeServiceCall(url);
                // fill the httpResponse with the json string. If the response is null there was a problem at the server, if it is empty the request was successful

                Log.e(TAG, "Complete Response from url (jsonStr) complete action: " + jsonStr);
                Log.e(TAG, "Complete Response from url (httpResponse) complete action: " + httpResponse);
                return null;
            }
            // Wenn keine Internetverbindung besteht, wird Erledigt setzten auf der lokalen DB durchgeführt
            else {

                try {
                    localDb.completeTodo(todoId, sessionId);
                    localDb.getTodos(sessionId);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
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
