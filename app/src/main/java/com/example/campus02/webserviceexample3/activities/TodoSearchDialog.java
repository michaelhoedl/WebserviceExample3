package com.example.campus02.webserviceexample3.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.campus02.webserviceexample3.R;

/**
 * Created by Philipp on 27.05.2017.
 */

class TodoSearchDialog extends Dialog {

    private AllTodosActivity activity;
    private Button search, cancel;
    private EditText text;
    private TodoSearchDialog thisDialog;

    public TodoSearchDialog(AllTodosActivity context) {
        super(context);
        this.activity = context;
        this.thisDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_search);
        getWindow().setLayout(android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        initalize();
    }

    private void initalize() {
        text = (EditText) findViewById(R.id.text);
        search = (Button) findViewById(R.id.search);
        cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                thisDialog.cancel();
            }
        });
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String searchStr = text.getText().toString();
                activity.searchTodos(searchStr);
            }
        });
    }

}
