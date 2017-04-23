package com.example.michaelhodl.webserviceexample3.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;


import com.example.michaelhodl.webserviceexample3.R;
import com.example.michaelhodl.webserviceexample3.model.TodoEntry;

import org.w3c.dom.Text;

import java.util.HashMap

public class CreateToDoActivity extends AppCompatActivity {

    private String TAG = CreateToDoActivity.class.getSimpleName();
    private String todoid;
    private String sessionid = null;
    private String httpResponse = null;
    private ProgressDialog pDialog;
    private TodoEntry mytodo;
    private EditText txtEName;
    private EditText txtEDescription;
    private EditText txtEDeadline;
    private EditText txtEEstimatedEffort;
    private EditText txtEActualEffort;
    private TextView txtVName;
    private TextView txtVDescription;
    private TextView txtVDeadline;
    private TextView txtVEstimatedEffort;
    private TextView txtVActualEffort;


    private String url = "http://campus02win14mobapp.azurewebsites.net/Todo/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_to_do);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        todoid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE2);
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

// Objektzuweisung, Anzeigen und Auslesen Text
        txtVName = (TextView)
                this.findViewById(R.id.txtName)
        txtEName = (EditText)
                this.findViewById(R.id.txtName)
                txtEName.getText().toString()

        txtVDescription = (TextView)
                this.findViewById(R.id.txtName)
        txtEDescription = (EditText)
                this.findViewById(R.id.txtDescription)
                txtEDescription.getText().toString()

        txtVDeadline = (TextView)
                this.findViewById(R.id.txtDeadline)
        txtEDeadline = (EditText)
                this.findViewById(R.id.txtDeadline)
                txtEDeadline.getText().toString()

        txtVEstimatedEffort = (TextView)
                this.findViewById(R.id.txtEstimatedEffort)
        txtEEstimatedEffort = (EditText)
                this.findViewById(R.id.txtEstimatedEffort)
                txtEEstimatedEffort.getText().toString()

        txtVActualEffort = (TextView)
                this.findViewById(R.id.txtActualEffort)
        txtEActualEffort = (EditText)
                this.findViewById(R.id.txtActualEffort)
                txtEActualEffort.getText().toString();

// Geänderter Text anzeigen
        EditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                TextView.setText(EditText.getText().toString());
            }




    }
}


