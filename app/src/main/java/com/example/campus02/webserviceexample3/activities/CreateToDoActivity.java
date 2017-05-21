package com.example.campus02.webserviceexample3.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    //public static final String EXTRA_MESSAGE4 = "com.example.campus02.webserviceexample3.MESSAGESESSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);
        dma = this;

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

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

        // check whether name and description fields are filled
        if(txtVName != null && !txtVName.isEmpty() && txtVDescription != null && !txtVDescription.isEmpty()){
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

            // call the webservice via HTTP POST to create a new To Do.
            runAsync();


            /*// funktioniert noch nicht, dass es wieder zur AllTodosActivity wechselt...
            // open the AllTodosActivity view to display all list items.
            // send the session_id.
            Intent intentdetail = new Intent(dma, AllTodosActivity.class);
            intentdetail.putExtra(EXTRA_MESSAGE4, sessionid); // we have to send the session_id.
            startActivity(intentdetail);
            */

            // mit finish() wird die aktuelle Activity geschlossen und wieder die Activity angezeigt, von der man gekommen ist.
            // (Im Hintergrund arbeitet hier ein Stack)
            finish();

        } else{ // if both fields are empty, then log it.
            Log.e(TAG, "Name and Description must be filled!");
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

            try {

                // Create a JSON Object out of the TodoEntry Object which was created from input data from the EditText-Fields.
                JSONObject jsonObject = new JSONObject();
                try {
                    if(caller.mytodo.getTitle() != null && !caller.mytodo.getTitle().isEmpty())
                        jsonObject.put("name",caller.mytodo.getTitle());
                    if(caller.mytodo.getTododesc() != null && !caller.mytodo.getTododesc().isEmpty())
                        jsonObject.put("description", caller.mytodo.getTododesc());
                    if(caller.mytodo.getEstimatedeffort() != 0.0f)
                        jsonObject.put("estimatedEffort", caller.mytodo.getEstimatedeffort());
                    if(caller.mytodo.getUsedtime() != 0.0f)
                        jsonObject.put("usedTime", caller.mytodo.getUsedtime());
                    if(caller.mytodo.getDuedateFormatted() != null && !caller.mytodo.getDuedateFormatted().isEmpty())
                        jsonObject.put("dueDate", caller.mytodo.getDuedateFormatted());
                    if(caller.mytodo.getDone() == 0 || caller.mytodo.getDone() == 1)
                        jsonObject.put("done",caller.mytodo.getDone());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String str = jsonObject.toString();
                Log.e(TAG, "jsonObject.toString()="+str);

                // make the webservice-call (HTTP POST).
                response = sh.makeMyServiceCall(requestURL, "POST", postDataParams, null, str);

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e(TAG, "performPostCall (2)");
            return response;

        } // end performPostCall

    } // end private class AsyncCaller



}


