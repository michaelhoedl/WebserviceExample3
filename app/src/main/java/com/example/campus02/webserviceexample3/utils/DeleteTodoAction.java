package com.example.campus02.webserviceexample3.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.example.campus02.webserviceexample3.activities.AllTodosActivity;
import com.example.campus02.webserviceexample3.model.SyncTodoEntry;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by andreas on 21.04.2017.
 */

public class DeleteTodoAction {

    // wird für Logging genutzt
    private String TAG = DeleteTodoAction.class.getSimpleName();
    private ProgressDialog pDialog;
    // Dialog von dem die Delete Action aufgerufen wird
    private AllTodosActivity mainDialog;
    // Attribute welche für das Löschen gebraucht werden
    private String todoId;
    private String sessionId;
    private String httpResponse = null;
    private String url;
    // Instanz des Datenbank-Handlers für die lokale Datenbank
    private DBHandler localDb;

    /**
     * Konstruktor mit den Parametern, welche gebraucht werden, um die delete action im AllTodosActivity auszuführen
     * @param mainDialog
     * @param todoId
     * @param sessionId
     */
    public DeleteTodoAction(AllTodosActivity mainDialog, String todoId, String sessionId) {
        this.mainDialog = mainDialog;
        this.todoId = todoId;
        this.sessionId = sessionId;

        localDb = new DBHandler(mainDialog);
    }

    public void runDeleteAction() {
        runDelete();
    }

    private void runDelete() {
        // AsyncTask starten um Todo vom Webservice oder aus der Lokalen DB zu Löschen.
        runAsync();

    }

    /**
     * Async task class delete todo by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        DeleteTodoAction caller;
        boolean isInternetConnected;
        HttpHandler sh;

        AsyncCaller(DeleteTodoAction caller){
            this.caller = caller;
            sh = new HttpHandler();
            //Prüfen ob eine Internetverbindung besteht
            isInternetConnected = sh.isNetworkAvailable(mainDialog.getApplicationContext());
        }

        @Override
        //this method will be running on UI thread
        protected void onPreExecute() {
            super.onPreExecute();

            // Progress Dialog wird angezeigt
            pDialog = new ProgressDialog(mainDialog);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        //this method will be running on background thread so dont update UI from here
        //do your long running http tasks here, you dont want to pass argument and u can access the parent class variable url over here
        protected Void doInBackground(Void... arg0) {

            // headers, welche für das Löschen in der API nötig sind - nur der Session Key und das Datentyp-Accept, zusammenbauen
            ArrayList<NameValuePair> headers = new ArrayList<>();
            // header für die Session Key
            NameValuePair h1 = new NameValuePair();
            h1.setName("session");
            h1.setValue(sessionId);
            // header für Datentyp-Accept
            NameValuePair h2 = new NameValuePair();
            h2.setName("Accept");
            h2.setValue("text/plain");
            headers.add(h1);
            headers.add(h2);

            // die todoId zur URL hinzufügen, da diese auf in der API benötigt wird
            url = "http://campus02win14mobapp.azurewebsites.net/Todo/" + todoId;

            // wenn eine Internetverbindung besteht wird das Löschen in der API ausgeführt
            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                // Delete Request an die API mit der URL (TodoId) und den Headers
                String jsonStr = sh.makeMyServiceCall(url, "DELETE", headers, null, null);

                 // httpResponse wird gefürt mit den response (JSON) von der API. Ist der httpRespone null gab es einen Fehler am Server; leer ist ok
                caller.setHttpResponse(jsonStr);

                // Logging für Json-String und httpResponse
                Log.e(TAG, "Response from url (jsonStr) delete action: " + jsonStr);
                Log.e(TAG, "Response from url (httpResponse) delete action: " + httpResponse);
                return null;
            } // zweiter Fall - keine Internetverbindung besteht
            else {

                try {
                    // Headers als String für das Speichern des SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist
                    String headersForLocalDb = "";
                    for (NameValuePair nvp : headers){
                        headersForLocalDb +=  nvp.getName()+":"+(String)nvp.getValue()+";";
                    }

                    // Object eines SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist (Id wird von Datenbank automatisch vergeben, dadurch nicht im Konstruktor)
                    // params und jsonString sind beim Delete nicht nötig, dadurch wird ein Leerstring übergeben
                    SyncTodoEntry syncEntry = new SyncTodoEntry(url, "DELETE", headersForLocalDb, "", "");

                    Log.e(TAG, "syncEntry= " + syncEntry.toString());

                    // Datenbankfunktion für das Insert der SyncTodoEntry
                    localDb.addSyncTodoEntry(syncEntry);
                    // Datenbankfunktion für das Delete des Todos
                    localDb.deleteTodo(todoId, sessionId);
                    // Datenbankfunktion für das Select der Todos nach dem Löschen
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

            Log.e(TAG, "status: (im onPostExecute) delete action: " + this.getStatus());
        }

    } // end private class AsyncCaller

    private void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }

    private void runAsync()
    {
        new AsyncCaller(this).execute();

        // bissl primitiver ansatz, um die problematik zu loesen
        //   dass der server ein bisschen zeit braucht um zu responden nachdem der HTTP call abgesetzt wurde...
        // Solange httpResponse nicht befuellt ist (mit dem json string, den der server liefert), warten.
        // Auch wenn httpResponse nie befuellt werden sollte, erstmal ca. 4 Sekunden (bzw. bis 4000 zaehlen) abwarten.
        int x = 0;
        while(httpResponse == null && x < 4000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x += 1;
        }

        Log.e(TAG, "x= " + x);
        Log.e(TAG, "httpResponse: " + httpResponse);
    }
}
