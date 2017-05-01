package com.example.michaelhodl.webserviceexample3.model;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.michaelhodl.webserviceexample3.R;

import java.util.ArrayList;

/**
 * Created by michaelhodl on 01.05.17.
 *
 * This class is a custom List Adapter.
 * Will be used to fill the List in AllTodosActivity with Objects of type TodoEntry.
 *
 * see: https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 */

public class TodoListAdapter extends ArrayAdapter<TodoEntry> {


    public TodoListAdapter(Context context, ArrayList<TodoEntry> todos) {
        super(context, 0, todos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        TodoEntry todo = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        // Lookup view for data population
        TextView tvId = (TextView) convertView.findViewById(R.id.todoid);
        TextView tvName = (TextView) convertView.findViewById(R.id.todoname);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tododesc);

        // Populate the data into the template view using the data object
        tvId.setText(todo.getId()+"");
        tvName.setText(todo.getTitle());
        tvHome.setText(todo.getTododesc());

        // Return the completed view to render on screen
        return convertView;
    }

}
