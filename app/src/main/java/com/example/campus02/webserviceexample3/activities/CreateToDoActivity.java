package com.example.campus02.webserviceexample3.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.campus02.webserviceexample3.model.SyncTodoEntry;
import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.utils.DBHandler;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateToDoActivity extends AppCompatActivity {

    private String TAG = CreateToDoActivity.class.getSimpleName();
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
    private Date    txtVDeadline;
    private float   txtVEstimatedEffort;
    private float   txtVActualEffort;

    // HTTP POST auf diese Url, um ein neues To Do zu erzeugen.
    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/";

    private CreateToDoActivity dma;

    Calendar mcurrentDate;

    private DBHandler localDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);

        // Anzeigen eines Zurück-Buttons in der Statusleiste der App.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dma = this;
        localDb = new DBHandler(dma);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

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

        txtEName = (EditText) this.findViewById(R.id.txtName);
        txtVName = txtEName.getText().toString();

        txtEDescription = (EditText) this.findViewById(R.id.txtDescription);
        txtVDescription = txtEDescription.getText().toString();

        // Datum aus dem Text extrahieren:
        txtEDeadline = (EditText) this.findViewById(R.id.txtDeadline);
        DateFormat dformat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String datumstring = txtEDeadline.getText().toString();
        if(!TextUtils.isEmpty(datumstring)) {
            try {
                txtVDeadline = dformat.parse(datumstring);
            }catch(java.text.ParseException e){
                datumstring = null; // falls das parsen nicht funktioniert...ins log schreiben
                Log.e(TAG, "Date parsing error: "+datumstring);
            }
        }

        txtEEstimatedEffort = (EditText) this.findViewById(R.id.txtEstimatedEffort);
        String float1string = txtEEstimatedEffort.getText().toString();
        if(!TextUtils.isEmpty(float1string)) {
            txtVEstimatedEffort = Float.valueOf(float1string);
        }

        txtEActualEffort = (EditText) this.findViewById(R.id.txtActualEffort);
        String float2string = txtEActualEffort.getText().toString();
        if(!TextUtils.isEmpty(float2string)) {
            txtVActualEffort = Float.valueOf(float2string);
        }


        // Datum (DueDate) via DatePicker auswählen:  ... geht hier aber irgendwie noch nicht ?!? ...
        mcurrentDate = Calendar.getInstance();
        txtEDeadline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mYear;
                int mMonth;
                int mDay;
                mYear=mcurrentDate.get(Calendar.YEAR);
                mMonth=mcurrentDate.get(Calendar.MONTH);
                mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(dma.getApplicationContext(), new DatePickerDialog.OnDateSetListener() {
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

        // check whether name and description fields are filled
        if(!TextUtils.isEmpty(txtVName) &&  !TextUtils.isEmpty(txtVDescription)){
            // neues TodoEntry Objekt mit den Daten aus den EditText-Feldern erstellen:
            mytodo = new TodoEntry();
            mytodo.setTitle(txtVName);
            mytodo.setTododesc(txtVDescription);
            mytodo.setEstimatedeffort(txtVEstimatedEffort);
            mytodo.setUsedtime(txtVActualEffort);
            mytodo.setDuedate(txtVDeadline);

            // im Log ausgeben dass der Save Button geklickt wurde, und auch gleich das gerade erstellte Objekt ausgeben.
            Log.e(TAG, "Save Button was clicked");
            Log.e(TAG, "mytodo="+mytodo.toString());

            // Aufruf des AsyncTask (Webservice via HTTP POST oder Lokale DB) um ein neues ToDo zu erstellen
            runAsync();

            // Nachdem ein neuer ToDo-Eintrag angelegt wurde, wird die Create-Ansicht geschlossen und wieder die Übersicht mit allen Einträgen angezeigt.
            // Mit finish() wird die aktuelle Activity geschlossen und wieder die Activity angezeigt, von der man gekommen ist.
            // (Im Hintergrund arbeitet hier ein Stack)
            finish();

        } else{ // if both fields are empty, then log it.
            Log.e(TAG, "Name and Description must be filled!");
        }

    }


    /**
     * Setzt das via DatePicker ausgewählte Datum ins EditText-Feld.
     */
    private void updateFieldWithDate() {
        String myFormat = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);
        txtEDeadline.setText(sdf.format(mcurrentDate.getTime()));

        // wenn ausgewähltes Datum (DueDate) vor dem aktuellen Datum liegt, dann setze roten Hintergrund fürs Feld.
        try {
            if (sdf.parse(txtEDeadline.getText().toString()).before(new Date()) ) {
                txtEDeadline.setBackgroundColor(Color.rgb(255, 77, 77));
            } else {
                txtEDeadline.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (ParseException e) {
            e.printStackTrace();
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
     * method used for calling the async task class which makes the HTTP calls.
     */
    private void runAsync()
    {
        new CreateToDoActivity.AsyncCaller(this).execute();
    }

    /**
     * TODO: evtl. diese Klasse und die Methoden auslagern in separate Utils Klasse, bzw. im HttpHandler fuers POST ergaenzen....
     * Async task class to get json by making HTTP call (POST)
     *
     * see: http://stackoverflow.com/questions/2938502/sending-post-data-in-android
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        //necessary for exchanging data.
        CreateToDoActivity caller;
        AsyncCaller(CreateToDoActivity caller){
            this.caller = caller;
        }

        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(CreateToDoActivity.this);
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
            String response = "";

            // set headers
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h1 = new NameValuePair();
            h1.setName("Content-Type");
            h1.setValue("application/json");
            NameValuePair h2 = new NameValuePair();
            h2.setName("session");
            h2.setValue(caller.getSessionid());
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue("application/json");
            headers.add(h1);
            headers.add(h2);
            headers.add(h3);

            try {
                response = performPostCall(caller.url, headers);
            } catch (Exception e) {
                Log.e(TAG, "Error ...");
            }
            Log.e(TAG, "create, response: " + response);

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


        /**
         * Method to perform a POST call to the webservice.
         * @param requestURL
         * @param postDataParams
         * @return
         */
        public String performPostCall(String requestURL,
                                      ArrayList<NameValuePair> postDataParams) {
            Log.e(TAG, "performPostCall (1)");
            String response = "";
            HttpHandler sh = new HttpHandler();

            // ermitteln ob die App eine Internetverbindung hat:
            boolean isInternetConnected = sh.isNetworkAvailable(dma.getApplicationContext());

            // die SessionID setzen:
            mytodo.setSessionKey(sessionid);

            try {

                // Create a JSON Object out of the TodoEntry Object which was created from input data from the EditText-Fields.
                JSONObject jsonObject = new JSONObject();
                try {
                    if(!TextUtils.isEmpty(caller.mytodo.getTitle()))
                        jsonObject.put("name",caller.mytodo.getTitle());
                    if(!TextUtils.isEmpty(caller.mytodo.getTododesc()))
                        jsonObject.put("description", caller.mytodo.getTododesc());
                    if(caller.mytodo.getEstimatedeffort() != 0.0f)
                        jsonObject.put("estimatedEffort", caller.mytodo.getEstimatedeffort());
                    if(caller.mytodo.getUsedtime() != 0.0f)
                        jsonObject.put("usedTime", caller.mytodo.getUsedtime());
                    if(!TextUtils.isEmpty(caller.mytodo.getDuedateFormatted()))
                        jsonObject.put("dueDate", caller.mytodo.getDuedateFormatted());
                    if(caller.mytodo.getDone() == 0 || caller.mytodo.getDone() == 1)
                        jsonObject.put("done",caller.mytodo.getDone());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //
                String str = jsonObject.toString();
                Log.e(TAG, "jsonObject.toString()="+str);

                // wenn Internetverbindung vorhanden ist:
                if(isInternetConnected) {
                    // make the webservice-call (HTTP POST).
                    response = sh.makeMyServiceCall(requestURL, "POST", postDataParams, null, str);
                    caller.setHttpResponse(response);
                } else { // sonst: wenn keine Internetverbindung vorhanden ist:

                    try {
                        // Headers als String für das Speichern des SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist
                        String headersForLocalDb = "";
                        for (NameValuePair nvp : postDataParams){
                            headersForLocalDb +=  nvp.getName()+":"+(String)nvp.getValue()+";";
                        }

                        // Object eines SyncTodoEntry, welcher für die nachträgliche Synchronisation nötig ist (Id wird von Datenbank automatisch vergeben, dadurch nicht im Konstruktor)
                        // params und jsonString sind beim Delete nicht nötig, dadurch wird ein Leerstring übergeben
                        SyncTodoEntry syncEntry = new SyncTodoEntry(url, "POST", headersForLocalDb, "", str);

                        Log.e(TAG, "syncEntry= " + syncEntry.toString());

                        // Datenbankfunktion für das Insert der SyncTodoEntry
                        localDb.addSyncTodoEntry(syncEntry);
                        // Datenbankfunktion für das Erstellen eines Todos
                        localDb.addTodo(mytodo);
                        // Datenbankfunktion für das Select der Todos nach dem Updaten
                        localDb.getTodos(sessionid);
                    } catch (ParseException e) {
                        // Parse Exception kann bei den Datenbankfunktionen geworfen werden
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e(TAG, "performPostCall (2)");
            return response;

        } // end performPostCall

    } // end private class AsyncCaller



}


