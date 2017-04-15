package com.example.michaelhodl.webserviceexample3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DisplayMessageActivity extends AppCompatActivity {

    private String TAG = DisplayMessageActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> todoList;


    private static String url = "http://campus02win14mobapp.azurewebsites.net/Todo";

    private String sessionid = null;
    private String httpResponse = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        todoList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        sessionid = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);


        //start the asynctask to retrieve the data from webservice
        AsyncTask bla = new DisplayMessageActivity.AsyncCaller(this).execute();
    }


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




    /**
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        //necessary for exchanging data.
        DisplayMessageActivity caller;
        AsyncCaller(DisplayMessageActivity caller){
            this.caller = caller;
        }

        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(DisplayMessageActivity.this);
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


            // Making a request to url and getting response
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null);

            caller.setHttpResponse(jsonStr);

            //just some logging
            Log.e(TAG, "Response from url: " + jsonStr);
            Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
            Log.e(TAG, "Response from url (httpResponse): " + httpResponse);
            Log.e(TAG, "jsonStr.length: " + jsonStr.length());


            if (jsonStr != null) {
                try {

                    //dont use jsonobject for this use case ...
                    //check: http://stackoverflow.com/questions/17441246/org-json-jsonarray-cannot-be-converted-to-jsonobject
                    /*
                    JSONObject jsonObj; // = new JSONObject(jsonStr);
                    jsonObj = new JSONObject(jsonStr);

                    Log.e(TAG, "jsonObj.length: " + jsonObj.length());
                    */


                    // Getting JSON Array node
                    JSONArray todos = new JSONArray(jsonStr); //jsonObj.getJSONArray("contacts");

                    Log.e(TAG, "todos.length: " + todos.length());

                    // looping through All To Do entries
                    for (int i = 0; i < todos.length(); i++) {
                        JSONObject c = todos.getJSONObject(i);
                        String id = c.getString("id");
                        String name = c.getString("name");
                        String description = c.getString("description");

                        Log.e(TAG, "i="+i+", id="+id+", name="+name+", description="+description);

                        // tmp hash map for a single entry
                        HashMap<String, String> task = new HashMap<>();

                        // adding each child node to HashMap key => value
                        task.put("id", id);
                        task.put("name", name);
                        task.put("description", description);

                        // adding the entry to the list
                        todoList.add(task);

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
            } else {
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
            /**
             * Updating parsed JSON data into ListView
             * */

                ListAdapter adapter = new SimpleAdapter(
                        DisplayMessageActivity.this, todoList,
                    R.layout.list_item, new String[]{"id", "name",
                    "description"}, new int[]{R.id.todoid,
                    R.id.todoname, R.id.tododesc});

            lv.setAdapter(adapter);

            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
        }

    } // end private class AsyncCaller


}
