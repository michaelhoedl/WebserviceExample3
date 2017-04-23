package com.example.michaelhodl.webserviceexample3.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.example.michaelhodl.webserviceexample3.R;
import com.example.michaelhodl.webserviceexample3.model.TodoEntry;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);

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

    }

}


