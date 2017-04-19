package com.example.michaelhodl.webserviceexample3.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;

public class TodoDetailActivity extends AppCompatActivity {

    private String TAG = TodoDetailActivity.class.getSimpleName();
    private String todoid;
    private String sessionid = null;
    private String httpResponse = null;
    private ProgressDialog pDialog;
    private TodoEntry mytodo;

    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);


        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        todoid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE2);
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

        // build the url for the HTTP request to get a specific todo-entry, i.e.: http://campus02win14mobapp.azurewebsites.net/Todo/3
        url = url+todoid;
        Log.d(TAG, "url = "+url);

        // just for testing... output the id.
        //TextView t3 = (TextView) findViewById(R.id.tvTodoId);
        //t3.setText("todo id = "+todoid+", session id = "+sessionid);
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

        // ... datum und andere felder evtl noch ergaenzen .... und ggf. das befuellen der felder in eigene methode auslagern.

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
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null);

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
                    // ... datum fehlt noch...


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
