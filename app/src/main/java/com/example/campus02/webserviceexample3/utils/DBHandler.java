package com.example.campus02.webserviceexample3.utils;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.util.ArrayList;

import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.model.UserEntry;

/**
 * Created by andreas on 12.05.2017.
 */

public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "dbLocal6";
    // Contacts table name
    private static final String TABLE_TODOS = "todos";
    private static final String TABLE_USER = "user";
    // Todo Table Columns names
    private static final String KEY_TODO_ID = "id";
    private static final String KEY_TODO_TITLE = "title";
    private static final String KEY_TODO_DESC = "tododesc";
    private static final String KEY_TODO_ESTIMATED = "estimatedeffort";
    private static final String KEY_TODO_USED = "usedtime";
    private static final String KEY_TODO_DONE = "done";
    private static final String KEY_TODO_CREATE = "createdate";
    private static final String KEY_TODO_DUE = "duedate";
    private static final String KEY_TODO_SESSIONKEY = "sessionkey";
    // User Table Columns names
    private static final String KEY_USER_MAIL = "mail";
    private static final String KEY_USER_PDW = "pwd";
    private static final String KEY_USER_SESSIONKEY = "sessionKey";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODO_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_TODOS + "("
                        + KEY_TODO_ID + " INTEGER PRIMARY KEY, "
                        + KEY_TODO_TITLE + " TEXT, "
                        + KEY_TODO_DESC + " TEXT, "
                        + KEY_TODO_ESTIMATED + " REAL, "
                        + KEY_TODO_USED + " REAL, "
                        + KEY_TODO_DONE + " INTEGER, "
                        + KEY_TODO_CREATE + " TEXT, "
                        + KEY_TODO_DUE + " TEXT, "
                        + KEY_TODO_SESSIONKEY + " TEXT"
                        + ")";
        db.execSQL(CREATE_TODO_TABLE);

        String CREATE_USER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "("
                        + KEY_USER_MAIL + " TEXT PRIMARY KEY ,"
                        + KEY_USER_PDW + " TEXT, "
                        + KEY_USER_SESSIONKEY + " TEXT "
                        + ")";
        db.execSQL(CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Creating tables again
        onCreate(db);
    }

    // Adding new shop
    public void addTodo(TodoEntry todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TODO_ID, todo.getId());
        values.put(KEY_TODO_TITLE, todo.getTitle());
        values.put(KEY_TODO_DESC, todo.getTododesc());
        values.put(KEY_TODO_ESTIMATED, todo.getEstimatedeffort());
        values.put(KEY_TODO_USED, todo.getUsedtime());
        values.put(KEY_TODO_DONE, todo.getDone());
        values.put(KEY_TODO_CREATE, String.valueOf(todo.getCreatedate()));
        values.put(KEY_TODO_DUE, String.valueOf(todo.getDuedate()));
        values.put(KEY_TODO_SESSIONKEY, todo.getSessionKey());
        // Inserting Row
        db.insertWithOnConflict(TABLE_TODOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public ArrayList<TodoEntry> getTodos (String session) throws ParseException {
        String selectQuery = "SELECT * FROM " + TABLE_TODOS
                + " WHERE " + KEY_TODO_SESSIONKEY + " = '" + session + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<TodoEntry> todos = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                TodoEntry todo = new TodoEntry();
                todo.setId(cursor.getInt(0));
                todo.setTitle(cursor.getString(1));
                todo.setTododesc(cursor.getString(2));
                todo.setEstimatedeffort(cursor.getFloat(3));
                todo.setUsedtime(cursor.getFloat(4));
                todo.setDone(cursor.getInt(5));
                todo.setCreatedate(todo.string2date(cursor.getString(6), "yyyy-MM-dd'T'HH:mm:ss"));
                todo.setCreatedate(todo.string2date(cursor.getString(7), "yyyy-MM-dd'T'HH:mm:ss"));
                todo.setSessionKey(cursor.getString(8));

                todos.add(todo);
            } while (cursor.moveToNext());

        }

        return todos;
    }


    public void addUser(UserEntry user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_MAIL, user.getMail());
        values.put(KEY_USER_PDW, user.getPwd());
        values.put(KEY_USER_SESSIONKEY, user.getSessionKey());
        // Inserting Row
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public UserEntry getUser(String mail, String pwd) {
        String selectQuery = "SELECT * FROM " + TABLE_USER
                + " WHERE " + KEY_USER_MAIL + " = '" + mail
                + "' AND " + KEY_USER_PDW + " = '" + pwd+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        UserEntry user = new UserEntry();
        if(cursor.moveToFirst()) {
            user.setMail(cursor.getString(0));
            user.setPwd(cursor.getString(1));
            user.setSessionKey(cursor.getString(2));
        } else {
            user = null;
        }

        return user;
    }

    public void deleteTodo(String id, String session) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = KEY_TODO_ID+" = '"+id+"' AND "+KEY_TODO_SESSIONKEY+" = '"+session+"'";
        db.delete(TABLE_TODOS, where, null);
    }

    public void completeTodo (String id, String session){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("done",1);
        String where = KEY_TODO_ID+" = '"+id+"' AND "+KEY_TODO_SESSIONKEY+" = '"+session+"'";
        db.update(TABLE_TODOS, values , where, null);

    }




}
