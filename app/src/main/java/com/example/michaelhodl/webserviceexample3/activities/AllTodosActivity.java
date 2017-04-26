package com.example.michaelhodl.webserviceexample3.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.michaelhodl.webserviceexample3.R;
import com.example.michaelhodl.webserviceexample3.utils.DeleteTodoAction;
import com.example.michaelhodl.webserviceexample3.utils.HttpHandler;
import com.example.michaelhodl.webserviceexample3.utils.NameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AllTodosActivity extends AppCompatActivity {

    private String TAG = AllTodosActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> todoList;
    public static final String EXTRA_MESSAGE2 = "com.example.michaelhodl.webserviceexample3.MESSAGETODO";
    public static final String EXTRA_MESSAGE3 = "com.example.michaelhodl.webserviceexample3.MESSAGESESSION";
    private static String url = "http://campus02win14mobapp.azurewebsites.net/Todo";
    private String sessionid = null;
    private String httpResponse = null;
    private boolean deleteIt = false;
    private AllTodosActivity dma;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_todos);

        dma = this;

        todoList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        // add click-listener to list. Reacting to a click on a list item.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {

                // get the ID (todo_id) of the selected item.
                String selectedFromList;
                HashMap myhm = (HashMap) lv.getItemAtPosition(myItemInt);
                selectedFromList = (String) myhm.get("id");
                Log.e(TAG, "selected list item todo_id: " + selectedFromList);

                // open new view (TodoDetailActivity) to display the details of the selected list item.
                // send the session_id (which we got from the login view) and the selected todo_id.
                Intent intentdetail = new Intent(dma, TodoDetailActivity.class);
                intentdetail.putExtra(EXTRA_MESSAGE2, selectedFromList); // we have to send the todo_id.
                intentdetail.putExtra(EXTRA_MESSAGE3, sessionid); // we have to send the session_id.
                startActivity(intentdetail);
            }
        });


        // register the List View for context menu pop-up when long-clicking on a list item. see also method onCreateContextMenu below.
        registerForContextMenu(lv);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        sessionid = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        //start the asynctask to retrieve the data from webservice
        runAsync();
    }


    // ---------------------------------------------------------------------------------------------

    @Override
    /**
     * This Method implements a context menu pop-up when long-clicking on a list item.
     * see: http://stackoverflow.com/questions/18632331/using-contextmenu-with-listview-in-android
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list) {
            ListView lv1 = (ListView) v;
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // get the ID (todo_id) of the selected item.
            String selectedFromList;
            HashMap myhm = (HashMap) lv1.getItemAtPosition(acmi.position);
            selectedFromList = (String) myhm.get("id");
            Log.e(TAG, "contextmenu: selected list item todo_id: " + selectedFromList);

            menu.add(menu.NONE,1,1,"Delete ToDo");
            menu.add(menu.NONE,2,2,"Complete ToDo");
        }
    }

    @Override
    /**
     * reacting to the selected option from the context menu on the list item long-click.
     * see: http://stackoverflow.com/questions/18632331/using-contextmenu-with-listview-in-android
     */
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // get the ID (todo_id) of the selected item.
        HashMap myhm = (HashMap) lv.getItemAtPosition(info.position);
        String selectedFromList = (String) myhm.get("id");

        // reagieren auf den ausgewaehlten menue-eintrag.
        switch (item.getItemId()) {
            //delete action
            case 1:
                Log.d(TAG, "delete item pos=" + info.position+" = todo_id: " + selectedFromList);
                final DeleteTodoAction delaction =  new DeleteTodoAction(this, selectedFromList, sessionid);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // runs the delete action, where the Http Service Call to the API will be done
                        deleteIt = delaction.runDeleteAction();

                        // if the delete was successful, the list of the todo will be reloaded with a service call to the api
                        if(deleteIt) {
                            runAsync();
                        }
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // if the user says no, nothing will be done and the dialog is closed again
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case 2:
                Log.d(TAG, "complete item pos=" + info.position+" = todo_id: " + selectedFromList);
                // ... hier kommt der code fuers erledigen hin...
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
        AllTodosActivity caller;
        AsyncCaller(AllTodosActivity caller){
            this.caller = caller;
        }

        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(AllTodosActivity.this);
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

                    //dont use jsonobject for this use case ...
                    //check: http://stackoverflow.com/questions/17441246/org-json-jsonarray-cannot-be-converted-to-jsonobject
                    /*
                    JSONObject jsonObj; // = new JSONObject(jsonStr);
                    jsonObj = new JSONObject(jsonStr);
                    */

                    // Getting JSON Array node
                    JSONArray todos = new JSONArray(jsonStr); //jsonObj.getJSONArray("contacts");
                    Log.e(TAG, "todos.length: " + todos.length());
                    todoList = new ArrayList<>();

                    // looping through all To Do entries within the Json Array
                    for (int i = 0; i < todos.length(); i++) {

                        // extract one Json Object from the Json Array
                        JSONObject c = todos.getJSONObject(i);

                        // extract the attributes/values from the Json Object
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
            /**
             * Updating parsed JSON data into ListView
             * */
                ListAdapter adapter = new SimpleAdapter(
                        AllTodosActivity.this, todoList,
                    R.layout.list_item, new String[]{"id", "name",
                    "description"}, new int[]{R.id.todoid,
                    R.id.todoname, R.id.tododesc});

            lv.setAdapter(adapter);

            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
        }

    } // end private class AsyncCaller

    private void runAsync()
    {
        new AllTodosActivity.AsyncCaller(this).execute();
    }

    /**
     * method which is performed when the Add-Button (+) is clicked.
     * @param view
     */
    public void addButtonClicked(View view){
        Log.e(TAG, "Add Button (FloatingActionButton) was clicked!");

        // open new view (TodoDetailActivity) to display the details of the selected list item.
        // send the session_id (which we got from the login view) and the selected todo_id.
        Intent intentdetail = new Intent(dma, CreateToDoActivity.class);
        intentdetail.putExtra(EXTRA_MESSAGE3, sessionid); // we have to send the session_id.
        startActivity(intentdetail);
    }

}
