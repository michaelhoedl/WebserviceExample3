package com.example.campus02.webserviceexample3.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.example.campus02.webserviceexample3.activities.TodoDetailActivity;
import com.example.campus02.webserviceexample3.model.SyncTodoEntry;
import com.example.campus02.webserviceexample3.model.TodoEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by michaelhodl on 21.05.17.
 */

public class UpdateTodoAction {

    private String TAG = UpdateTodoAction.class.getSimpleName();
    private ProgressDialog pDialog;
    private TodoDetailActivity mainDialog;
    private String todoId;
    private String sessionId;
    private String httpResponse = null;
    private String url;
    private TodoEntry mytodo;
    private DBHandler localDb;

    public UpdateTodoAction(TodoDetailActivity mainDialog, String mysession, TodoEntry e) {
        this.mainDialog = mainDialog;
        this.todoId = e.getId()+"";
        this.sessionId = mysession;
        this.mytodo = e;
        localDb = new DBHandler(mainDialog);
    }


    /**
     * Diese Methode ruft den AsyncTask auf
     */
    private void runAsync()
    {
        new UpdateTodoAction.AsyncCaller(this).execute();
    }

    public boolean runUpdateAction() {
        return runUpdate();
    }

    private boolean runUpdate() {
        runAsync();
        return httpResponse != null;
    }

    /**
     * Async task class
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        UpdateTodoAction caller;
        boolean isInternetConnected;
        HttpHandler sh;

        AsyncCaller(UpdateTodoAction caller){
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

            // set headers
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

            url = "http://campus02win14mobapp.azurewebsites.net/Todo";

            // Create a JSON Object out of the TodoEntry Object which was created from input data from the EditText-Fields.
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("id",todoId);
                jsonObject.put("name",caller.mytodo.getTitle());
                jsonObject.put("description",caller.mytodo.getTododesc());
                jsonObject.put("estimatedEffort",caller.mytodo.getEstimatedeffort());
                jsonObject.put("usedTime",caller.mytodo.getUsedtime());
                jsonObject.put("dueDate",caller.mytodo.getDuedateFormatted());
                jsonObject.put("done",caller.mytodo.getDone());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String str = jsonObject.toString();
            Log.e(TAG, "--- str= "+str);

            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                // Making a complete request to url and getting response
                // Aufruf um Daten an Datenbank zu übergeben
                String jsonStr = sh.makeMyServiceCall(url, "POST", headers, null, str);
                // fill the httpResponse with the json string. If the response is null there was a problem at the server, if it is empty the request was successful
                httpResponse = jsonStr;

                Log.e(TAG, "Update Response from url (jsonStr) update action: " + jsonStr);
                Log.e(TAG, "Update Response from url (httpResponse) update action: " + httpResponse);
                return null;
            } // else: if no internet connection is available
            else {

                try {
                    // Headers als String für das Speichern des SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist
                    String headersForLocalDb = "";
                    for (NameValuePair nvp : headers){
                        headersForLocalDb +=  nvp.getName()+":"+(String)nvp.getValue()+";";
                    }

                    // Object eines SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist (Id wird von Datenbank automatisch vergeben, dadurch nicht im Konstruktor)
                    // params und jsonString sind beim Delete nicht nötig, dadurch wird ein Leerstring übergeben
                    SyncTodoEntry syncEntry = new SyncTodoEntry(url, "POST", headersForLocalDb, "", str);
                    syncEntry.setSession(sessionId);

                    Log.e(TAG, "syncEntry= " + syncEntry.toString());

                    // Datenbankfunktion für das Insert der SyncTodoEntry
                    localDb.addSyncTodoEntry(syncEntry);
                    // Datenbankfunktion für das Update des Todos
                    localDb.updateTodo(todoId, sessionId, mytodo);
                    // Datenbankfunktion für das Select der Todos nach dem Updaten
                    localDb.getTodos(sessionId);
                } catch (ParseException e) {
                    // Parse Exception kann bei den Datenbankfunktionen geworfen werden
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
