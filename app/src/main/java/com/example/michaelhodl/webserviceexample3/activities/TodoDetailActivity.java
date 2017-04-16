package com.example.michaelhodl.webserviceexample3.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.michaelhodl.webserviceexample3.R;

public class TodoDetailActivity extends AppCompatActivity {

    String todoid;
    String sessionid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // Get the Intent that started this activity and extract the string (which is the session id)
        Intent intent = getIntent();
        todoid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE2);
        sessionid = intent.getStringExtra(AllTodosActivity.EXTRA_MESSAGE3);

        // just for testing... output the id.
        TextView t3 = (TextView) findViewById(R.id.textView3);
        t3.setText("todo id = "+todoid+", session id = "+sessionid);

    }
}
