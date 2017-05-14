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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.michaelhodl.webserviceexample3.R;
import com.example.michaelhodl.webserviceexample3.model.TodoEntry;
import com.example.michaelhodl.webserviceexample3.model.TodoListAdapter;
import com.example.michaelhodl.webserviceexample3.utils.DBHandler;
import com.example.michaelhodl.webserviceexample3.utils.DeleteTodoAction;
import com.example.michaelhodl.webserviceexample3.utils.HttpHandler;
import com.example.michaelhodl.webserviceexample3.utils.NameValuePair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class AllTodosActivity extends AppCompatActivity {

    private String TAG = AllTodosActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    public static final String EXTRA_MESSAGE2 = "com.example.michaelhodl.webserviceexample3.MESSAGETODO";
    public static final String EXTRA_MESSAGE3 = "com.example.michaelhodl.webserviceexample3.MESSAGESESSION";
    private static String url = "http://campus02win14mobapp.azurewebsites.net/Todo";
    private String sessionid = null;
    private String httpResponse = null;
    private boolean deleteIt = false;
    private AllTodosActivity dma;

    private ArrayList<TodoEntry> alltodos;
    private TodoListAdapter adapter;
    private DBHandler localDb = new DBHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_todos);

        dma = this;

        alltodos = new ArrayList<TodoEntry>();
        //adapter = new TodoListAdapter(AllTodosActivity.this, alltodos);

        lv = (ListView) findViewById(R.id.list);

        // add click-listener to list. Reacting to a click on a list item.
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {

                // get the ID (todo_id) of the selected list item.
                TodoEntry e = (TodoEntry) lv.getItemAtPosition(myItemInt);
                String selectedFromList = e.getId()+"";
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

        // bissl primitiver ansatz, um die problematik zu loesen
        //   dass der server ein bisschen zeit braucht um zu responden nachdem der HTTP call abgesetzt wurde...
        // Solange httpResponse nicht befuellt ist (mit dem json string, den der server liefert), warten.
        // Auch wenn httpResponse nie befuellt werden sollte, erstmal ca. 4 Sekunden (bzw. bis 4000 zaehlen) abwarten.
        // UPDATE: solange die Liste alltodos noch leer ist, warten. (weil: Liste kann entweder aus HTTP Request befuellt worden sein,
        //   oder via Lokaler SQLite DB.
        int x = 0;
        while(alltodos.size() == 0 /*httpResponse == null*/ && x <= 4000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x += 1;
        }

        Log.e(TAG, "x= "+x);
        Log.e(TAG, "alltodos.size= "+alltodos.size());
        for(TodoEntry t : alltodos){
            Log.e(TAG, "--- t= "+t.toString());
        }



        // nachfolgender code ist jetzt im doInBackground eingebaut. .. braucht man daher hier nicht.
        //if(httpResponse == "" || httpResponse == null)
        //{
        //    try {
        //        // get todos from the local database
        //        alltodos = localDb.getTodos(sessionid);
        //
        //        // test: log the todoentries which was loaded from local DB
        //        for(TodoEntry t : alltodos){
        //            Log.e(TAG, "--- t= "+t.toString());
        //        }
        //
        //        //TODO: show here the new list in GUI
        //    } catch (ParseException e)
        //    {
        //        e.printStackTrace();
        //    }
        //
        //} else {
           // // save the todos into the local db --> passiert im doInBackground
           // for(TodoEntry todo : alltodos) {
           //     todo.setSessionKey(sessionid);
           //     localDb.addTodo(todo);
           // }
        //} // end if(httpResponse == "" || httpResponse == null)


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

            // get the ID (todo_id) of the selected list item.
            TodoEntry e = (TodoEntry) lv.getItemAtPosition(acmi.position);
            String selectedFromList = e.getId()+"";
            Log.e(TAG, "contextmenu: selected list item todo_id: " + selectedFromList);

            // defining two menu options for delete and complete
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

        // get the ID (todo_id) of the selected list item.
        TodoEntry e = (TodoEntry) lv.getItemAtPosition(info.position);
        String selectedFromList = e.getId()+"";


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
                            // at first, clear the list. Then reload the data and fill the list again.
                            adapter.clear();
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

        boolean isInternetConnected;
        HttpHandler sh;

        //necessary for exchanging data.
        AllTodosActivity caller;
        AsyncCaller(AllTodosActivity caller){
            this.caller = caller;
            sh = new HttpHandler();
            isInternetConnected = sh.isNetworkAvailable(caller.getApplicationContext());

            // initially set httpResponse and the alltodos list to be empty
            httpResponse = null;
            alltodos.clear();
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
            // if internet connection is available, get data from the webserver
            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

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
                String jsonStr = sh.makeMyServiceCall(url, "GET", headers, null, null);

                caller.setHttpResponse(jsonStr);

                //just some logging
                Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
                Log.e(TAG, "Response from url (httpResponse): " + httpResponse);
                Log.e(TAG, "jsonStr.length: " + jsonStr.length());


                if (jsonStr != null && jsonStr != "") {
                    try {

                        // Getting JSON Array node. see: http://stackoverflow.com/questions/17441246/org-json-jsonarray-cannot-be-converted-to-jsonobject
                        JSONArray todos = new JSONArray(jsonStr); //jsonObj.getJSONArray("contacts");
                        Log.e(TAG, "JSONArray todos.length: " + todos.length());

                        // looping through all To Do entries within the Json Array
                        for (int i = 0; i < todos.length(); i++) {

                            // extract one Json Object from the Json Array
                            JSONObject c = todos.getJSONObject(i);

                            // extract the attributes/values from the Json Object
                            int _id = c.getInt("id");
                            String _name = c.getString("name");
                            String _description = c.getString("description");
                            float _estimatedEffort = (float) c.getDouble("estimatedEffort");
                            float _usedTime = (float) c.getDouble("usedTime");
                            boolean _done = c.getBoolean("done");
                            String _duedate = c.getString("dueDate");

                            // create a TodoEntry object and set the data.
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

                            // adding the entry to the list
                            alltodos.add(mytodo);

                            // save the todos into local DB
                            localDb.addTodo(mytodo);

                            //Log.e(TAG, "i=" + i + ", id=" + _id + ", name=" + _name + ", description=" + _description + ", done=" + _done);
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
                } // end if (jsonStr != null && jsonStr != "")
            } // else: if no internet connection is available
            else {
                Log.e(TAG, "--- no internet connection! ---");
                httpResponse = null;
                alltodos.clear();

                // get todos from the local database
                try {
                    alltodos = localDb.getTodos(sessionid);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            } // end if(isInternetConnected)
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

            /* // not needed, because a custom list adapter with TodoEntry Objects is used.
                ListAdapter adapter = new SimpleAdapter(
                        AllTodosActivity.this, todoList,            // todoList was a ArrayList<HashMap<String, String>>
                    R.layout.list_item, new String[]{"id", "name",
                    "description"}, new int[]{R.id.todoid,
                    R.id.todoname, R.id.tododesc});
            */



            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());

            Log.e(TAG, " (onPostExecute) alltodos.size= "+alltodos.size());
            for(TodoEntry t : alltodos){
                Log.e(TAG, "(onPostExecute) --- t= "+t.toString());
            }

            // set our custom list adapter.
            adapter = new TodoListAdapter(caller.getApplicationContext(), alltodos);
            lv.setAdapter(adapter);
            Log.e(TAG,"lv.getAdapter().getCount()="+lv.getAdapter().getCount());
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
