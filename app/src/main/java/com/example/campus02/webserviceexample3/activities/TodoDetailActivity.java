package com.example.campus02.webserviceexample3.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campus02.webserviceexample3.model.SyncTodoEntry;
import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.utils.DBHandler;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;
import com.example.campus02.webserviceexample3.utils.UpdateTodoAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    // Instanz des Datenbank-Handlers für die lokale Datenbank
    private DBHandler localDb;

    EditText eduedate;
    Calendar mcurrentDate;

    /**
     * Konstruktor
     */
    public TodoDetailActivity() {
        localDb = new DBHandler(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // Anzeigen eines Zurück-Buttons in der Statusleiste der App.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        todoid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE2);
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

        // die URL für den HTTP Request zusammenbauen, z.B.: http://campus02win14mobapp.azurewebsites.net/Todo/3
        url += todoid;

        // test logging...
        Log.d(TAG, "url = "+url);
        Log.d(TAG, "todo id = "+todoid+", session id = "+sessionid);


        // AsyncTask starten um Details eines Todos vom Webservice oder aus der Lokalen DB anzuzeigen.
        runAsync();

        // Die Felder der Detailansicht ermitteln:
        TextView tvid = (TextView) findViewById(R.id.tvTodoId);
        EditText etname = (EditText) findViewById(R.id.txtName);
        EditText etdesc = (EditText) findViewById(R.id.txtDescription);
        EditText etestimatedeffort = (EditText) findViewById(R.id.txtEstimatedEffort);
        EditText etactualeffort = (EditText) findViewById(R.id.txtActualEffort);
        eduedate = (EditText) findViewById(R.id.txtDeadline);
        CheckBox cErledigt = (CheckBox) findViewById(R.id.cbCompleted);

        // die Felder der Detail-Ansicht nur befüllen, wenn das mytodo Objekt nicht null ist:
        if (mytodo != null) {
            tvid.setText(mytodo.getId() + "");
            etname.setText(mytodo.getTitle());
            etdesc.setText(mytodo.getTododesc());
            etestimatedeffort.setText(mytodo.getEstimatedeffort() + "");
            etactualeffort.setText(mytodo.getUsedtime() + "");
            eduedate.setText(mytodo.getDuedateFormatted());
            cErledigt.setChecked(mytodo.getDoneBoolean());
        }


        // Datum (DueDate) via DatePicker auswählen:
        mcurrentDate = Calendar.getInstance();
        eduedate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mYear;
                int mMonth;
                int mDay;
                mYear=mcurrentDate.get(Calendar.YEAR);
                mMonth=mcurrentDate.get(Calendar.MONTH);
                mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(TodoDetailActivity.this, new DatePickerDialog.OnDateSetListener() {
                    // wenn ein Datum via DatePicker ausgewählt wurde, dann zeige das Datum im EditText-Feld an:
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        mcurrentDate.set(Calendar.YEAR, selectedyear);
                        mcurrentDate.set(Calendar.MONTH, selectedmonth);
                        mcurrentDate.set(Calendar.DAY_OF_MONTH, selectedday);
                        updateFieldWithDate();
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();  }
        });

    }

    /**
     * Setzt das via DatePicker ausgewählte Datum ins EditText-Feld.
     */
    private void updateFieldWithDate() {
        String myFormat = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);
        eduedate.setText(sdf.format(mcurrentDate.getTime()));

        // wenn ausgewähltes Datum (DueDate) vor dem aktuellen Datum liegt, dann setze roten Hintergrund fürs Feld.
        try {
            if (sdf.parse(eduedate.getText().toString()).before(new Date()) ) {
                eduedate.setBackgroundColor(Color.rgb(255, 77, 77));
            } else {
                eduedate.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

    /**
     * Auslagern des Auslesens der EditText-Felder in eine eigene Methode.
     * Wird aufgerufen bei Klick auf den "Save" Button.
     */
    public void readDataFromFields(View view){
        // Objektzuweisung, Anzeigen und Auslesen Text

        final UpdateTodoAction updateaction;

        txtEName = (EditText)
                this.findViewById(R.id.txtName);
        txtVName = txtEName.getText().toString();

        txtEDescription = (EditText)
                this.findViewById(R.id.txtDescription);
        txtVDescription = txtEDescription.getText().toString();


        // Datum aus dem Text extrahieren:
        txtEDeadline = (EditText)
                this.findViewById(R.id.txtDeadline);
        DateFormat dformat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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

        // Durchführen des Updates
        updateaction = new UpdateTodoAction(this, sessionid, mytodo);
        updateaction.runUpdateAction();

        // Nachdem ein neuer ToDo-Eintrag angelegt wurde, wird die Create-Ansicht geschlossen und wieder die Übersicht mit allen Einträgen angezeigt.
        // Mit finish() wird die aktuelle Activity geschlossen und wieder die Activity angezeigt, von der man gekommen ist.
        // (Im Hintergrund arbeitet hier ein Stack)
        finish();

        // im Log ausgeben dass der Save Button geklickt wurde, und auch gleich das gerade erstellte Objekt ausgeben.
        Log.e(TAG, "Save Button was clicked");
        Log.e(TAG, "mytodo="+mytodo.toString());
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
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        //necessary for exchanging data.
        TodoDetailActivity caller;
        HttpHandler sh;
        boolean isInternetConnected;

        AsyncCaller(TodoDetailActivity caller){
            this.caller = caller;
            sh = new HttpHandler();
            //Prüfen ob eine Internetverbindung besteht
            isInternetConnected = sh.isNetworkAvailable(caller.getApplicationContext());
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

            // set headers
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h2 = new NameValuePair();
            h2.setName("session");
            h2.setValue(caller.getSessionid());
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue("application/json");
            headers.add(h2);
            headers.add(h3);

            // wenn eine Internetverbindung besteht wird das Löschen in der API ausgeführt
            if(isInternetConnected) {

                // Making a request to url and getting response as a string
                String jsonStr = sh.makeMyServiceCall(url, "GET", headers, null, null);

                caller.setHttpResponse(jsonStr);

                //just some logging
                Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
                Log.e(TAG, "Response from url (httpResponse): " + httpResponse);
                Log.e(TAG, "jsonStr.length: " + jsonStr.length());


                if (jsonStr != null) {
                    try {

                        JSONObject jsonObj = new JSONObject(jsonStr);
                        Log.e(TAG, "jsonObj.length: " + jsonObj.length());

                        // extract the attributes/values from the Json Object
                        int id = jsonObj.getInt("id");
                        String name = jsonObj.getString("name");
                        String description = jsonObj.getString("description");
                        float estimatedEffort = (float) jsonObj.getDouble("estimatedEffort");
                        float usedTime = (float) jsonObj.getDouble("usedTime");
                        boolean done = jsonObj.getBoolean("done");
                        String duedate = jsonObj.getString("dueDate");
                        String createdate = jsonObj.getString("createDate");


                        // create a TodoEntry object and set the data.
                        caller.mytodo = new TodoEntry();
                        caller.mytodo.setId(id);
                        caller.mytodo.setTitle(name);
                        caller.mytodo.setTododesc(description);
                        caller.mytodo.setEstimatedeffort(estimatedEffort);
                        caller.mytodo.setUsedtime(usedTime);
                        if (done) {
                            caller.mytodo.setDone(1);
                        } else {
                            caller.mytodo.setDone(0);
                        }
                        caller.mytodo.setDuedateAsString(duedate);
                        caller.mytodo.setCreatedateAsString(createdate);

                        Log.e(TAG, "jsonObj: id=" + id + ", name=" + name + ", description=" + description);
                        Log.e(TAG, "jsonObj: mytodo.tostring=" + caller.mytodo.toString());

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
            } else // ansonsten, wenn keine Internetverbindung besteht, dann Daten aus Lokaler DB auslesen:
            {
                // leeren des httpResponse:
                httpResponse = null;

                // Laden des ToDos aus der Lokalen DB:
                try {
                    caller.mytodo = localDb.getTodoById(sessionid, todoid);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
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


    /**
     * Diese Methode ruft den AsyncTask auf
     */
    private void runAsync()
    {
        new TodoDetailActivity.AsyncCaller(this).execute();

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
        Log.d(TAG, "mytodo.tostring="+ ((mytodo != null) ? mytodo.toString() : "(mytodo is null)"));
    }


}
