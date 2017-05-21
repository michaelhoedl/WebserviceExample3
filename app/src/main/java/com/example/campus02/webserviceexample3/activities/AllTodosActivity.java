package com.example.campus02.webserviceexample3.activities;

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

import com.example.campus02.webserviceexample3.utils.CompleteTodoAction;
import com.example.campus02.webserviceexample3.utils.DBHandler;
import com.example.campus02.webserviceexample3.utils.DeleteTodoAction;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;
import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.model.TodoListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class AllTodosActivity extends AppCompatActivity {

    private String              TAG = AllTodosActivity.class.getSimpleName();
    private ProgressDialog      pDialog;
    private ListView            lv;
    public static final String  EXTRA_MESSAGE2 = "com.example.campus02.webserviceexample3.MESSAGETODO";
    public static final String  EXTRA_MESSAGE3 = "com.example.campus02.webserviceexample3.MESSAGESESSION";
    private static String       url = "http://campus02win14mobapp.azurewebsites.net/Todo";
    private String              sessionid = null;
    private String              httpResponse = null;
    private AllTodosActivity    dma;

    private ArrayList<TodoEntry> alltodos;
    private TodoListAdapter      adapter;
    private DBHandler localDb = new DBHandler(this);


    @Override
    /**
     * die onCreate() Methode wird automatisch beim Starten der Activity aufgerufen.
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_todos);

        dma = this;
        alltodos = new ArrayList<TodoEntry>();

        // Das GUI Element "list" ermitteln und die ListView lv damit initialisieren.
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

    }

    @Override
    /**
     * Wenn ein Neues To Do in der CreateToDoActivity erstellt wurde (und die Activity mit finish() abgeschlossen wurde),
     * dann wechselt die Ansicht wieder zurueck zu derjenigen Activity, von der aus die CreateToDoActivity gestartet wurde,
     * also diese Activity (AllTodosActivity).
     * Und dann wird automatisch diese Methode onResume() aufgerufen.
     * Diese Methode macht ladet die Liste neu, da ein neues To Do erstellt wurde.
     *
     * Laut Android Workflow-Status wird zuerst die onCreate(), dann onStart(), dann onResume() aufgerufen.
     * Somit reicht es, wenn die runAsync() nur in der onResume() aufgerufen wird (und nicht auch in der onCreate).
     */
    protected void onResume() {
        super.onResume();

        // Neu Laden der Liste. // AsyncTask starten um Daten vom Webservice oder aus der Lokalen DB zu laden.
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

        // testweise ein paar daten ausgeben
        Log.e(TAG, "x= "+x);
        Log.e(TAG, "alltodos.size= "+alltodos.size());
        for(TodoEntry t : alltodos){
            Log.e(TAG, "--- t= "+t.toString());
        }
    }


    // ---------------------------------------------------------------------------------------------

    @Override
    /**
     * Diese Methode implementiert ein Kontext-Menue Pop-Up wenn man einen Listen-Eintrag laenger anklickt.
     * siehe auch: http://stackoverflow.com/questions/18632331/using-contextmenu-with-listview-in-android
     */
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.list) {

            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;

            // Die ID (todo_id) des ausgewaehlten Listen-Eintrages ermitteln. Nur zum Testen die ID ausgeben.
            TodoEntry e = (TodoEntry) lv.getItemAtPosition(acmi.position);
            String selectedFromList = e.getId()+"";
            Log.e(TAG, "contextmenu: selected list item todo_id: " + selectedFromList);

            // Zwei Optionen festlegen: 1 = Loeschen, 2 = Erledigen.
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // get the ID (todo_id) of the selected list item.
        TodoEntry e = (TodoEntry) lv.getItemAtPosition(info.position);
        String selectedFromList = e.getId()+"";


        // reagieren auf den ausgewaehlten menue-eintrag.
        switch (item.getItemId()) {
            //1 = delete action
            case 1:
                Log.d(TAG, "delete item pos=" + info.position+" = todo_id: " + selectedFromList);
                final DeleteTodoAction delaction =  new DeleteTodoAction(this, selectedFromList, sessionid);

                // eine Alert-Message, damit der Benutzer bestaetigen kann, dass er wirklich den Eintrag loeschen will.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    // wenn User auf Yes klickt, dann wird folgendes ausgefuehrt:
                    public void onClick(DialogInterface dialog, int which) {

                        // aufrufen der delete action, wo ein Http Service Call zur API durchgefuehrt wird oder der Eintrag aus der lokalen DB geloescht wird.
                        delaction.runDeleteAction();

                        // Liste leeren, dann neu laden der Daten und Liste befuellen.
                        adapter.clear();
                        runAsync();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Wenn User auf No klickt, passiert nichts und der Alert wird geschlossen.
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
                return true;
            // 2 = complete action
            case 2:
                Log.d(TAG, "complete item pos=" + info.position+" = todo_id: " + selectedFromList);
                final CompleteTodoAction complete =  new CompleteTodoAction(this,e);

                // aufrufen der complete action, wo ein Http Service Call zur API durchgefuehrt wird oder der Eintrag in der lokalen DB erledigt wird.
                complete.runCompleteTodoAction();

                // Liste leeren, dann neu laden der Daten und Liste befuellen.
                adapter.clear();
                runAsync();

                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Getters und Setters

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
     * Innere Private Klasse
     * Async Task macht die HTTP Anfragen und laedt die Daten als Json.
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        boolean isInternetConnected;
        HttpHandler sh;

        //necessary for exchanging data.
        AllTodosActivity caller;
        AsyncCaller(AllTodosActivity caller){
            this.caller = caller;
            sh = new HttpHandler();
            // ermitteln ob die App eine Internet-Verbindung hat
            isInternetConnected = sh.isNetworkAvailable(caller.getApplicationContext());

            // Initial-Werte fuer httpResponse und die alltodos Liste auf leer setzen.
            httpResponse = null;
            alltodos.clear();
        }

        @Override
        /**
         * diese Methode laeuft im UI Thread
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
         * Diese Methode laeuft in einem Hintergrundprozess (background thread).  Hier sollte man die UI nicht updaten.
         * Laenger laufende Tasks (http) sollten hier passieren.
         */
        protected Void doInBackground(Void... arg0) {
            // Wenn eine Internetverbindung besteht, dann lade Daten vom Webservice.
            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");

                // HTTP Header setzen:
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

                //just some logging
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
                            alltodos.add(mytodo);

                            // Hinzufuegen des TodoEntry Objectes zur lokalen DB:
                            localDb.addTodo(mytodo);

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

                // leeren des httpResponse und der ArrayList:
                httpResponse = null;
                alltodos.clear();

                // Die ToDos aus der Lokalen DB laden:
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
         * Diese Methode laeuft im UI Thread
         */
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            // testweise logging...
            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
            Log.e(TAG, " (onPostExecute) alltodos.size= "+alltodos.size());
            for(TodoEntry t : alltodos){
                Log.e(TAG, "(onPostExecute) --- t= "+t.toString());
            }

            // Den Custom List Adapter fuer die Liste setzen:
            adapter = new TodoListAdapter(caller.getApplicationContext(), alltodos);
            lv.setAdapter(adapter);
            Log.e(TAG,"lv.getAdapter().getCount()="+lv.getAdapter().getCount());
        }

    } // end private class AsyncCaller

    /**
     * Diese Methode ruft den AsyncTask auf
     */
    private void runAsync()
    {
        new AllTodosActivity.AsyncCaller(this).execute();
    }

    /**
     * Diese Methode wird bei Klick auf den Hinzufuegen-Button (+) aufgerufen.
     * @param view
     */
    public void addButtonClicked(View view){
        Log.e(TAG, "Add Button (FloatingActionButton) was clicked!");

        // Neue Ansicht (CreateToDoActivity) oeffnen um einen neuen TodoEintrages zu erstellen.
        // Es wird die session_id (die aus Login Ansicht MainActivity kommt) an die neue Activity uebergeben.
        Intent intentdetail = new Intent(dma, CreateToDoActivity.class);
        intentdetail.putExtra(EXTRA_MESSAGE3, sessionid); // Uebermitteln der the session_id.
        startActivity(intentdetail);
    }

    /**
     * Diese Methode wird bei Klick auf den Suchen-Button aufgerufen.
     * @param view
     */
    public void addButtonSearchClicked(View view){
        Log.e(TAG, "Add Button Search (FloatingActionButton) was clicked!");

        // Neue Ansicht (TodoSearchActivity) oeffnen um eine Suche ueber die TodoEintraege zu machen.
        // Es wird die session_id (die aus Login Ansicht MainActivity kommt) an die neue Activity uebergeben.
        Intent intentdetail = new Intent(dma, TodoSearchActivity.class);
        intentdetail.putExtra(EXTRA_MESSAGE3, sessionid); // Uebermitteln der the session_id.
        startActivity(intentdetail);
    }

}