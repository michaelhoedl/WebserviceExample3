package com.example.campus02.webserviceexample3.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TodoSearchActivity extends AppCompatActivity {

    private String TAG = TodoSearchActivity.class.getSimpleName();
    private String sessionid = null;
    private String httpResponse = null;
    private ProgressDialog pDialog;

    private EditText txtESearch;
    private String searchStr = null;
    private ArrayList<TodoEntry> foundTodos;

    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/search/";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_search);
        this.foundTodos = new ArrayList<TodoEntry>();

        // Anzeigen eines Zurück-Buttons in der Statusleiste der App.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Getting the SessionID from AllTodosActivity
        Intent intent = getIntent();
        this.sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

        // Logging the SessionID
        Log.d(TAG, "SessionID = " + sessionid);
    }

    @Override
    /**
     * bei Klick auf den Zurück-Button in der App-Statusleiste
     * wird die aktuelle Activity mit finish() geschlossen
     * und man kommt zu jener Activity zurück, von der aus diese Activity gestartet wurde.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // searchButtonClicked
    public void sendMessage(View view) {

        // Save SearchString
        this.searchStr = ((EditText) findViewById(R.id.editTextSearch)).getText().toString();
        Log.e(TAG, "SearchString: " + searchStr);
        url = url + searchStr;

        if(searchStr.isEmpty()) {
            showMyAlert("No Data entered", "Please enter a Search-Text");
        }
        else {
            // AsyncTask / WebserviceCall
            AsyncTask async = new TodoSearchActivity.AsyncCaller(this).execute();
            Log.e(TAG, "status (im sendMessage): " + async.getStatus());

            // bissl primitiver ansatz, um die problematik zu loesen
            // dass der server ein bisschen zeit braucht um zu responden nachdem der HTTP call abgesetzt wurde...
            // Solange httpResponse nicht befuellt ist (mit dem json string, den der server liefert), warten.
            // Auch wenn httpResponse nie befuellt werden sollte, erstmal ca. 4 Sekunden (bzw. bis 4000 zaehlen) abwarten.
            // UPDATE: solange die Liste alltodos noch leer ist, warten. (weil: Liste kann entweder aus HTTP Request befuellt worden sein,
            // oder via Lokaler SQLite DB.
            int x = 0;
            while (httpResponse == null && x <= 4000) {
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

    private void showMyAlert(String s, String s1) {
        AlertDialog alertDialog = new AlertDialog.Builder(TodoSearchActivity.this).create();
        alertDialog.setTitle(s);
        alertDialog.setMessage(s1);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    /**
     * Innere private Klasse
     * Async Task macht die HTTP Anfragen und laedt die Daten als Json.
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        boolean isInternetConnected;
        HttpHandler sh;

        TodoSearchActivity caller;
        AsyncCaller(TodoSearchActivity caller) {
            this.caller = caller;
            sh = new HttpHandler();

            // Internet-Verbindung prüfen
            isInternetConnected = sh.isNetworkAvailable(caller.getApplicationContext());

            // Initialwerte: httpResponse zurücksetzen
            httpResponse = null;
            foundTodos.clear();
        }

        @Override
        /**
         * diese Methode laeuft im UI Thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TodoSearchActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        /**
         * Diese Methode laeuft in einem Hintergrundprozess (background thread).  Hier sollte man die UI nicht updaten.
         * Laenger laufende Tasks (http) sollten hier passieren.
         */
        protected Void doInBackground(Void... arg0) {
            // Wenn eine Internetverbindung besteht, dann lade Daten vom Webservice.
            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                // HTTP Header setzen
                ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
                NameValuePair h2 = new NameValuePair();
                h2.setName("session");
                h2.setValue(caller.getSessionid());
                NameValuePair h3 = new NameValuePair();
                h3.setName("Accept");
                h3.setValue("application/json");
                headers.add(h2);
                headers.add(h3);

                // Sende eine GET Anfrage an den Webservice an die URl mit den definierten Headern und erhalte einen Json String als Response.
                String jsonStr = sh.makeMyServiceCall(url, "GET", headers, null, null);
                caller.setHttpResponse(jsonStr);
                Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
                Log.e(TAG, "Response from url (httpResponse): " + httpResponse);
                Log.e(TAG, "jsonStr.length: " + jsonStr.length());

                // Wenn der Json String aus der HTTP Anfrage nicht leer ist, dann passiert folgendes:
                if (jsonStr != null && !jsonStr.isEmpty() && !jsonStr.equals("")) {
                    try {
                        // Wandle den Json String in ein JSON Array um.
                        // siehe auch: http://stackoverflow.com/questions/17441246/org-json-jsonarray-cannot-be-converted-to-jsonobject
                        JSONArray todos = new JSONArray(jsonStr);
                        Log.e(TAG, "JSONArray todos.length: " + todos.length());

                        // Durchlaufe alle Eintraege im Json Array:
                        for (int i = 0; i < todos.length(); i++) {

                            // Extrahiere ein Json Object aus dem Json Array:
                            JSONObject c = todos.getJSONObject(i);

                            // Extrahiere die Attribute/Werte aus dem aktuellen Json Object:
                            int     _id                 = c.getInt("id");
                            String  _name               = c.getString("name");
                            String  _description        = c.getString("description");
                            float   _estimatedEffort    = (float) c.getDouble("estimatedEffort");
                            float   _usedTime           = (float) c.getDouble("usedTime");
                            boolean _done               = c.getBoolean("done");
                            String  _duedate            = c.getString("dueDate");

                            // Erstelle ein TodoEntry Objekt und befuelle es mit den Daten aus dem Json Object:
                            TodoEntry mytodo = new TodoEntry();
                            mytodo.setId(_id);
                            mytodo.setTitle(_name);
                            mytodo.setTododesc(_description);
                            mytodo.setEstimatedeffort(_estimatedEffort);
                            mytodo.setUsedtime(_usedTime);
                            if (_done) {
                                mytodo.setDone(1);
                            } else {
                                mytodo.setDone(0);
                            }
                            mytodo.setDuedateAsString(_duedate);
                            mytodo.setSessionKey(sessionid);

                            // Hinzufuegen des TodoEntry Objektes zur ArrayList:
                            foundTodos.add(mytodo);

                            // Hinzufuegen des TodoEntry Objectes zur lokalen DB:
                            // localDb.addTodo(mytodo);

                            Log.e(TAG, "i="+i+", todo="+mytodo.toString());
                        }
                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                } else { // wenn kein Json vom Server zurueckgeliefert wurde, dann eine Fehlermeldung ausgeben.
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } // end if (jsonStr != null && jsonStr != "")

            } // else: wenn keine Internetverbindung verfuegbar ist, dann aus lokaler DB auslesen:
            else {
                Log.e(TAG, "--- no internet connection! ---");
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

            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
            Log.e(TAG, " (onPostExecute) alltodos.size= "+foundTodos.size());
        }

    } // end private class AsyncCaller


    // Getters and Setters
    public String getHttpResponse() {
        return httpResponse;
    }
    public void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }
    public String getSessionid() {
        return sessionid;
    }
    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
    public String getSearchStr() {
        return searchStr;
    }
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }
    public ArrayList<TodoEntry> getFoundTodos() { return foundTodos; }
    public void setFoundTodos(ArrayList<TodoEntry> foundTodos) { this.foundTodos = foundTodos; }

}