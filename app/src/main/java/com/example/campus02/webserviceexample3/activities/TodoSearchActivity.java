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

import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;

import java.util.ArrayList;

public class TodoSearchActivity extends AppCompatActivity {

    private String TAG = TodoSearchActivity.class.getSimpleName();
    private String sessionid = null;
    private String httpResponse = null;
    private ProgressDialog pDialog;

    private EditText txtESearch;
    private String searchStr = null;

    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_search);

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

    // buttonSearch onClick
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

            // -------------
            // Noch offen:
            // HttpResponse in TodoEntry Objekten abspeichern und darstellen
            // -------------
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
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        TodoSearchActivity caller;
        AsyncCaller(TodoSearchActivity caller){
            this.caller = caller;
        }

        @Override
        //this method will be running on UI thread
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TodoSearchActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        //this method will be running on background thread so dont update UI from here
        //do your long running http tasks here, you dont want to pass argument and u can access the parent class variable url over here
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // set headers
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h1 = new NameValuePair();
            h1.setName("session");
            h1.setValue(caller.getSessionid());
            NameValuePair h2 = new NameValuePair();
            h2.setName("str");
            h2.setValue(caller.getSearchStr());
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue("application/json");
            headers.add(h1);
            headers.add(h2);
            headers.add(h3);


            // Making a request to url and getting response
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null, null); //sh.makeServiceCall(url);
            // fill the httpResponse with the json string. The httpResponse basically just is the session_id returned by the server.
            caller.setHttpResponse(jsonStr);

            Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
            Log.e(TAG, "Response from url (httpResponse): " + httpResponse);

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

}