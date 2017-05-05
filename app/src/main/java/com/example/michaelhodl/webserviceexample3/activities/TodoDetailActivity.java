package com.example.michaelhodl.webserviceexample3.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.michaelhodl.webserviceexample3.R;
import com.example.michaelhodl.webserviceexample3.model.TodoEntry;
import com.example.michaelhodl.webserviceexample3.utils.HttpHandler;
import com.example.michaelhodl.webserviceexample3.utils.NameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TodoDetailActivity extends AppCompatActivity {

    private String TAG = TodoDetailActivity.class.getSimpleName();
    private String todoid;
    private String sessionid = null;
    private String httpResponse = null;
    private ProgressDialog pDialog;
    private TodoEntry mytodo;

    // EditText Elemente
    private EditText txtEName;
    private EditText txtEDescription;
    private EditText txtEDeadline;
    private EditText txtEEstimatedEffort;
    private EditText txtEActualEffort;

    // Variablen die mit den Inhalten aus den EditText Elementen befuellt werden bei Klick auf Button "Save"
    private String  txtVName;
    private String  txtVDescription;
    private Date txtVDeadline;
    private float   txtVEstimatedEffort;
    private float   txtVActualEffort;

    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        todoid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE2);
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

        // build the url for the HTTP request to get a specific to do entry, i.e.: http://campus02win14mobapp.azurewebsites.net/Todo/3
        url = url+todoid;

        // test logging...
        Log.d(TAG, "url = "+url);
        Log.d(TAG, "todo id = "+todoid+", session id = "+sessionid);


        //start the asynctask to retrieve the data from webservice
        AsyncTask bla = new TodoDetailActivity.AsyncCaller(this).execute();

        // wait a little bit ... just to ensure that the HTTP request was processed completely.
        int x = 0;
        while(mytodo == null && x < 4000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x += 1;
        }
        Log.e(TAG, "x= " + x);
        Log.d(TAG, "mytodo.tostring="+mytodo.toString());

        // fill the data into the fields
        TextView tvid = (TextView) findViewById(R.id.tvTodoId);
        tvid.setText(mytodo.getId()+"");
        EditText etname = (EditText) findViewById(R.id.txtName);
        etname.setText(mytodo.getTitle());
        EditText etdesc = (EditText) findViewById(R.id.txtDescription);
        etdesc.setText(mytodo.getTododesc());
        EditText etestimatedeffort = (EditText) findViewById(R.id.txtEstimatedEffort);
        etestimatedeffort.setText(mytodo.getEstimatedeffort()+"");
        EditText etactualeffort = (EditText) findViewById(R.id.txtActualEffort);
        etactualeffort.setText(mytodo.getUsedtime()+"");

        // ... datum und andere felder evtl noch ergaenzen ....
        EditText eduedate = (EditText) findViewById(R.id.txtDeadline);
        eduedate.setText(mytodo.getDuedateFormatted());

        CheckBox cErledigt = (CheckBox) findViewById(R.id.cbCompleted);
        cErledigt.setChecked(mytodo.getDoneBoolean());

    }

    /**
     * Auslagern des Auslesens der EditText-Felder in eine eigene Methode.
     * Wird aufgerufen bei Klick auf den "Save" Button.
     */
    public void readDataFromFields(View view){
        // Objektzuweisung, Anzeigen und Auslesen Text

        txtEName = (EditText)
                this.findViewById(R.id.txtName);
        txtVName = txtEName.getText().toString();


        txtEDescription = (EditText)
                this.findViewById(R.id.txtDescription);
        txtVDescription = txtEDescription.getText().toString();


        // Datum aus dem Text extrahieren:
        txtEDeadline = (EditText)
                this.findViewById(R.id.txtDeadline);
        DateFormat dformat = new SimpleDateFormat("dd.mm.yyyy", Locale.getDefault());
        String datumstring = txtEDeadline.getText().toString();
        if(datumstring != null && !datumstring.isEmpty()) {
            try {
                txtVDeadline = dformat.parse(datumstring);
            }catch(java.text.ParseException e){
                datumstring = null; // falls das parsen nicht funktioniert...ins log schreiben
                Log.e(TAG, "Date parsing error: "+datumstring);
            }
        }


        txtEEstimatedEffort = (EditText)
                this.findViewById(R.id.txtEstimatedEffort);
        String float1string = txtEEstimatedEffort.getText().toString();
        if(float1string != null && !float1string.isEmpty()) {
            txtVEstimatedEffort = Float.valueOf(float1string);
        }

        txtEActualEffort = (EditText)
                this.findViewById(R.id.txtActualEffort);
        String float2string = txtEActualEffort.getText().toString();
        if(float2string != null && !float2string.isEmpty()) {
            txtVActualEffort = Float.valueOf(float2string);
        }

        // neues TodoEntry Objekt mit den Daten aus den EditText-Feldern erstellen:
        //mytodo = new TodoEntry();
        mytodo.setTitle(txtVName);
        mytodo.setTododesc(txtVDescription);
        mytodo.setEstimatedeffort(txtVEstimatedEffort);
        mytodo.setUsedtime(txtVActualEffort);
        mytodo.setDuedate(txtVDeadline);

        // im Log ausgeben dass der Save Button geklickt wurde, und auch gleich das gerade erstellte Objekt ausgeben.
        Log.e(TAG, "Save Button was clicked");
        Log.e(TAG, "mytodo="+mytodo.toString());
    }



    // ---------------------------------------------------------------------------------------------

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



    // ---------------------------------------------------------------------------------------------

    /**
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        //necessary for exchanging data.
        TodoDetailActivity caller;
        AsyncCaller(TodoDetailActivity caller){
            this.caller = caller;
        }

        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TodoDetailActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        /**
         * this method will be running on background thread so dont update UI from here
         * do your long running http tasks here, you dont want to pass argument and u can access the parent class variable url over here
         */
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // set headers
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h2 = new NameValuePair();
            h2.setName("session");
            h2.setValue(caller.getSessionid());
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue(new String("application/json"));
            headers.add(h2);
            headers.add(h3);


            // Making a request to url and getting response as a string
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null, null);

            caller.setHttpResponse(jsonStr);

            //just some logging
            Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
            Log.e(TAG, "Response from url (httpResponse): " + httpResponse);
            Log.e(TAG, "jsonStr.length: " + jsonStr.length());


            if (jsonStr != null) {
                try {

                    JSONObject jsonObj; // = new JSONObject(jsonStr);
                    jsonObj = new JSONObject(jsonStr);
                    Log.e(TAG, "jsonObj.length: " + jsonObj.length());

                    // extract the attributes/values from the Json Object
                    int id                  = jsonObj.getInt("id");
                    String name             = jsonObj.getString("name");
                    String description      = jsonObj.getString("description");
                    float estimatedEffort   = (float) jsonObj.getDouble("estimatedEffort");
                    float usedTime          = (float) jsonObj.getDouble("usedTime");
                    boolean done            = jsonObj.getBoolean("done");
                    String duedate          = jsonObj.getString("dueDate");


                    // create a TodoEntry object and set the data.
                    caller.mytodo = new TodoEntry();
                    caller.mytodo.setId(id);
                    caller.mytodo.setTitle(name);
                    caller.mytodo.setTododesc(description);
                    caller.mytodo.setEstimatedeffort(estimatedEffort);
                    caller.mytodo.setUsedtime(usedTime);
                    if (done){
                        caller.mytodo.setDone(1);
                    } else {
                        caller.mytodo.setDone(0);
                    }
                    // ... evt. noch das datum einbauen...
                    caller.mytodo.setDuedateAsString(duedate);

                    Log.e(TAG, "jsonObj: id="+id+", name="+name+", description="+description);
                    Log.e(TAG, "jsonObj: mytodo.tostring="+caller.mytodo.toString());

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
            } else { // if no Json was returned from the Server, output an error message.
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

            } // end if
            return null;
        } // end doInBackground


        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
        }

    } // end private class AsyncCaller


}
