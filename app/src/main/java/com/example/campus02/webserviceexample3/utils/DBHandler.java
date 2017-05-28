package com.example.campus02.webserviceexample3.utils;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.example.campus02.webserviceexample3.model.TodoEntry;
import com.example.campus02.webserviceexample3.model.UserEntry;
import com.example.campus02.webserviceexample3.model.SyncTodoEntry;

/**
 * Created by andreas on 12.05.2017.
 */

public class DBHandler extends SQLiteOpenHelper {
    // Datenbank Version - bei Datenbankänderung (Spalten oder Tabellen) muss die Version geändert werden, damit diese Änderungen erzeugt werden
    private static final int DATABASE_VERSION               = 6;
    // Datenbank
    private static final String DATABASE_NAME               = "dbLocal";
    // Tabellen
    private static final String TABLE_TODOS                 = "todos";
    private static final String TABLE_USER                  = "user";
    private static final String TABLE_SYNCTODO              = "synctodos";
    // Spalten für die Todos-Tabelle
    private static final String KEY_TODO_ID                 = "id";
    private static final String KEY_TODO_TITLE              = "title";
    private static final String KEY_TODO_DESC               = "tododesc";
    private static final String KEY_TODO_ESTIMATED          = "estimatedeffort";
    private static final String KEY_TODO_USED               = "usedtime";
    private static final String KEY_TODO_DONE               = "done";
    private static final String KEY_TODO_CREATE             = "createdate";
    private static final String KEY_TODO_DUE                = "duedate";
    private static final String KEY_TODO_SESSIONKEY         = "sessionkey";
    // Spalten für die User-Tabelle
    private static final String KEY_USER_MAIL               = "mail";
    private static final String KEY_USER_PDW                = "pwd";
    private static final String KEY_USER_SESSIONKEY         = "sessionKey";
    // Spalten für die SyncTodos-Tabelle
    private static final String KEY_SYNCTODO_ID             = "id";
    private static final String KEY_SYNCTODO_URL            = "url";
    private static final String KEY_SYNCTODO_CMD            = "cmd";
    private static final String KEY_SYNCTODO_HEADERS        = "headers";
    private static final String KEY_SYNCTODO_PARAMS         = "params";
    private static final String KEY_SYNCTODO_JSONPOSTSTR    = "jsonpoststr";
    private static final String KEY_SYNCTODO_SESSIONKEY     = "sessionkey";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    /**
     * Erzeugen der Datenbanktabellen, wenn sich die Datenbankversion geändert hat
     */
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TODO_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_TODOS + " ( "
                        + KEY_TODO_ID + " INTEGER PRIMARY KEY, "
                        + KEY_TODO_TITLE + " TEXT, "
                        + KEY_TODO_DESC + " TEXT, "
                        + KEY_TODO_ESTIMATED + " REAL, "
                        + KEY_TODO_USED + " REAL, "
                        + KEY_TODO_DONE + " INTEGER, "
                        + KEY_TODO_CREATE + " TEXT, "
                        + KEY_TODO_DUE + " TEXT, "
                        + KEY_TODO_SESSIONKEY + " TEXT"
                        + " )";
        db.execSQL(CREATE_TODO_TABLE);

        String CREATE_USER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_USER + " ( "
                        + KEY_USER_MAIL + " TEXT PRIMARY KEY ,"
                        + KEY_USER_PDW + " TEXT, "
                        + KEY_USER_SESSIONKEY + " TEXT "
                        + " )";
        db.execSQL(CREATE_USER_TABLE);

        String CREATE_SYNCTODO_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_SYNCTODO + " ( "
                        + KEY_SYNCTODO_ID + " INTEGER PRIMARY KEY autoincrement,"
                        + KEY_SYNCTODO_URL + " TEXT, "
                        + KEY_SYNCTODO_CMD + " TEXT, "
                        + KEY_SYNCTODO_HEADERS + " TEXT, "
                        + KEY_SYNCTODO_PARAMS + " TEXT, "
                        + KEY_SYNCTODO_JSONPOSTSTR + " TEXT, "
                        + KEY_SYNCTODO_SESSIONKEY + " TEXT "
                        + " )";
        db.execSQL(CREATE_SYNCTODO_TABLE);
        //db.close();
    }

    @Override
    /**
     * Löschen der alten Tabellen, wenn sich die Datenbankversion ändert
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNCTODO);
        // Erzeugen der Tabellen
        onCreate(db);
    }

    /**
     * Loeschen der Inhalte der jeweiligen Tabelle.
     * Danach werden die Inhalte in die jeweilige Tabelle eingefuegt.
     * Das passiert jedoch nur wenn die App eine Internetverbindung hat.
     * @param tablename
     */
    public void deleteTableBeforeInserting(String tablename){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(tablename, null, null);
        db.close();
    }

    /**
     * Todos einfügen oder updaten (bspw. beim Hinzufügen von Todos, wenn keine Internetverbindung
     * besteht oder sobald man wieder online ist, die Todos abgleichen in die Lokale Datenbank)
     * @param todo
     */
    public void addTodo(TodoEntry todo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if(!TextUtils.isEmpty(todo.getId()+""))  // die ID nur setzen wenn sie vorhanden ist
            values.put(KEY_TODO_ID, todo.getId());
        values.put(KEY_TODO_TITLE, todo.getTitle());
        values.put(KEY_TODO_DESC, todo.getTododesc());
        values.put(KEY_TODO_ESTIMATED, todo.getEstimatedeffort());
        values.put(KEY_TODO_USED, todo.getUsedtime());
        values.put(KEY_TODO_DONE, todo.getDone());
        values.put(KEY_TODO_CREATE, todo.getCreatedateFormatted());
        values.put(KEY_TODO_DUE, todo.getDuedateFormatted());
        values.put(KEY_TODO_SESSIONKEY, todo.getSessionKey());
        // Insert oder Update
        db.insertWithOnConflict(TABLE_TODOS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }


    /**
     * Auslesen aller Todos von einem User / Session Key
     * @param session
     * @return
     * @throws ParseException
     */
    public ArrayList<TodoEntry> getTodos (String session) throws ParseException {
        String selectQuery = "SELECT * FROM " + TABLE_TODOS
                + " WHERE " + KEY_TODO_SESSIONKEY + " = '" + session + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<TodoEntry> todos = new ArrayList<>();

        // Erstellen der TodoEntrys anhand der gelesen Daten
        // diese Funktion wird solang durchgeführt bis es keine Daten mehr gibt (moveToNext = false)
        if (cursor.moveToFirst()) {
            do {
                TodoEntry todo = new TodoEntry();
                todo.setId(cursor.getInt(0));
                todo.setTitle(cursor.getString(1));
                todo.setTododesc(cursor.getString(2));
                todo.setEstimatedeffort(cursor.getFloat(3));
                todo.setUsedtime(cursor.getFloat(4));
                todo.setDone(cursor.getInt(5));

                // Datumswerte:
                SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                Date d1 = null;
                Date d2 = null;
                String s1 = cursor.getString(6);
                String s2 = cursor.getString(7);
                if(!TextUtils.isEmpty(s1)) {
                    try {
                        d1 = dt1.parse(s1);
                    } catch (ParseException p) {
                        p.printStackTrace();
                        Log.e("DBHandler", "ParseException: " + p.getMessage());
                    }
                }
                if(!TextUtils.isEmpty(s2)) {
                    try {
                        d2 = dt1.parse(s2);
                    } catch (ParseException p) {
                        p.printStackTrace();
                        Log.e("DBHandler", "ParseException: " + p.getMessage());
                    }
                }

                if (d1 != null)
                    todo.setCreatedate(d1);
                if(d2 != null)
                    todo.setDuedate(d2);

                todo.setSessionKey(cursor.getString(8));

                // zu Liste von Todos hinzufügen, welche zurückgegeben wird
                todos.add(todo);
            } while (cursor.moveToNext());
        }
        cursor.close(); // schliessen des Cursors
        db.close();
        return todos;
    }

    /**
     * Suche von Todos anhand eines übergebenen Suchstrings
     * Auslesen der gefundenen Todos welche den Suchstring zumindest in 1 Spalte enthalten
     * @param session, searchStr
     * @return ArrayList<TodoEntry>
     * @throws ParseException
     */
    public ArrayList<TodoEntry> getSearchedTodos (String session, String searchStr) throws ParseException {
        String selectQuery = "SELECT * FROM " + TABLE_TODOS
                + " WHERE " + KEY_TODO_SESSIONKEY + " = '" + session + "' AND ("
                + "instr("+KEY_TODO_TITLE+",'" + searchStr + "') > 0 OR "
                + "instr("+KEY_TODO_DESC+",'" + searchStr + "') > 0 "
                        //+"OR "
                        //+ "CHARINDEX('" + searchStr + "'," + "CONVERT(VARCHAR(10)," + KEY_TODO_ESTIMATED + ")) > 0 OR "
                        //+ "CHARINDEX('" + searchStr + "'," + "CONVERT(VARCHAR(10)," + KEY_TODO_USED + ")) > 0
                 +" )";

                    /* noch ergänzen
                    + "CHARINDEX('" + searchStr + "'," + "CONVERT(VARCHAR(10)," + KEY_TODO_CREATE + ")) > 0 OR "
                    + "CHARINDEX('" + searchStr + "'," + "CONVERT(VARCHAR(10)," + KEY_TODO_DUE + ")) > 0 OR "
                    */

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        ArrayList<TodoEntry> foundTodos = new ArrayList<>();

        // Erstellen der TodoEntrys anhand der gelesen Daten
        // diese Funktion wird solang durchgeführt bis es keine Daten mehr gibt (moveToNext = false)
        if (cursor.moveToFirst()) {
            do {
                TodoEntry todo = new TodoEntry();
                todo.setId(cursor.getInt(0));
                todo.setTitle(cursor.getString(1));
                todo.setTododesc(cursor.getString(2));
                todo.setEstimatedeffort(cursor.getFloat(3));
                todo.setUsedtime(cursor.getFloat(4));
                todo.setDone(cursor.getInt(5));
                todo.setCreatedate( todo.string2date(cursor.getString(6), "dd.MM.yyyy" /*"yyyy-MM-dd'T'HH:mm:ss"*/) );
                todo.setDuedate( todo.string2date(cursor.getString(7), "dd.MM.yyyy" /*"yyyy-MM-dd'T'HH:mm:ss"*/) );
                todo.setSessionKey(cursor.getString(8));

                // zu Liste von Todos hinzufügen, welche zurückgegeben wird
                foundTodos.add(todo);
            } while (cursor.moveToNext());
        }
        cursor.close(); // schliessen des Cursors
        db.close();
        return foundTodos;
    }

    /**
     * Auslesen eines bestimmten ToDos aus der Lokalen DB.
     * @param session
     * @param todoid
     * @return
     * @throws ParseException
     */
    public TodoEntry getTodoById (String session, String todoid) throws ParseException {
        String selectQuery = "SELECT * FROM " + TABLE_TODOS
                + " WHERE " + KEY_TODO_SESSIONKEY + " = '" + session + "' AND "+KEY_TODO_ID+" = '"+todoid+"' ";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        TodoEntry todo = new TodoEntry();

        // Erstellen des TodoEntrys anhand der gelesen Daten
        // diese Funktion wird solang durchgeführt bis es keine Daten mehr gibt (moveToNext = false)
        if (cursor.moveToFirst()) {
                todo.setId(cursor.getInt(0));
                todo.setTitle(cursor.getString(1));
                todo.setTododesc(cursor.getString(2));
                todo.setEstimatedeffort(cursor.getFloat(3));
                todo.setUsedtime(cursor.getFloat(4));
                todo.setDone(cursor.getInt(5));

            // Datumswerte:
                SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                Date d1 = null;
                Date d2 = null;
                String s1 = cursor.getString(6);
                String s2 = cursor.getString(7);
                if(!TextUtils.isEmpty(s1)) {
                    try {
                        d1 = dt1.parse(s1);
                    } catch (ParseException p) {
                        p.printStackTrace();
                        Log.e("DBHandler", "ParseException: " + p.getMessage());
                    }
                }
                if(!TextUtils.isEmpty(s2)) {
                    try {
                        d2 = dt1.parse(s2);
                    } catch (ParseException p) {
                        p.printStackTrace();
                        Log.e("DBHandler", "ParseException: " + p.getMessage());
                    }
                }

                if (d1 != null)
                    todo.setCreatedate(d1);
                if(d2 != null)
                    todo.setDuedate(d2);

                todo.setSessionKey(cursor.getString(8));
        }
        cursor.close(); // schliessen des Cursors
        db.close();
        return todo;
    }

    /**
     * Einfügen oder Update des Benutzers (Mail, PWD und Session Key)
     * @param user
     */
    public void addUser(UserEntry user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_MAIL, user.getMail());
        values.put(KEY_USER_PDW, user.getPwd());
        values.put(KEY_USER_SESSIONKEY, user.getSessionKey());
        // Insert oder Update
        db.insertWithOnConflict(TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * Auslesen des Benutzers zum Prüfen
     * @param mail
     * @param pwd
     * @return
     */
    public UserEntry getUser(String mail, String pwd) {
        String selectQuery = "SELECT * FROM " + TABLE_USER
                + " WHERE " + KEY_USER_MAIL + " = '" + mail
                + "' AND " + KEY_USER_PDW + " = '" + pwd+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Erstellen der User Entry anhand der gelesen Daten
        // diese Funktion wird solang durchgeführt bis der Benutzer null ist
        UserEntry user = new UserEntry();
        if(cursor.moveToFirst()) {
            user.setMail(cursor.getString(0));
            user.setPwd(cursor.getString(1));
            user.setSessionKey(cursor.getString(2));
        } else {
            user = null;
        }
        cursor.close(); // schliessen des Cursors.
        db.close();
        return user;
    }

    /**
     * Löschen des Todos mit der übergebenen Id, sofern der Session Key korrekt ist.
     * @param id
     * @param session
     */
    public void deleteTodo(String id, String session) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = KEY_TODO_ID+" = '"+id+"' AND "+KEY_TODO_SESSIONKEY+" = '"+session+"'";
        db.delete(TABLE_TODOS, where, null);
        db.close();
    }

    /**
     * Erledigen eines Todos
     * @param id
     * @param session
     */
    public void completeTodo (String id, String session){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TODO_DONE,1);
        String where = KEY_TODO_ID+" = '"+id+"' AND "+KEY_TODO_SESSIONKEY+" = '"+session+"'";
        db.update(TABLE_TODOS, values , where, null);
        db.close();
    }

    /**
     * Updaten eines Todos mit den Daten aus dem übergebenen TodoEntry Objektes.
     * @param id
     * @param session
     * @param mytodo
     */
    public void updateTodo (String id, String session, TodoEntry mytodo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TODO_TITLE,mytodo.getTitle());
        values.put(KEY_TODO_DESC, mytodo.getTododesc());
        values.put(KEY_TODO_ESTIMATED, mytodo.getEstimatedeffort());
        values.put(KEY_TODO_USED,mytodo.getUsedtime());
        values.put(KEY_TODO_DUE,mytodo.getDuedateFormatted());
        values.put(KEY_TODO_DONE,mytodo.getDone());

        String where = KEY_TODO_ID+" = '"+id+"' AND "+KEY_TODO_SESSIONKEY+" = '"+session+"'";
        db.update(TABLE_TODOS, values , where, null);
        db.close();
    }


    /**
     * Hinzufügen eines SyncTodoEntries für die nachträgliche Synchronisation mit der API.
     * @param syncEntry
     */
    public void addSyncTodoEntry(SyncTodoEntry syncEntry) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SYNCTODO_URL, syncEntry.getUrl());
        values.put(KEY_SYNCTODO_CMD, syncEntry.getCmd());
        values.put(KEY_SYNCTODO_SESSIONKEY, syncEntry.getSession());

        if (!TextUtils.isEmpty(syncEntry.getHeaders()))
            values.put(KEY_SYNCTODO_HEADERS, syncEntry.getHeaders());
        if (!TextUtils.isEmpty(syncEntry.getParams()))
            values.put(KEY_SYNCTODO_PARAMS, syncEntry.getParams());
        if (!TextUtils.isEmpty(syncEntry.getJsonPostStr()))
            values.put(KEY_SYNCTODO_JSONPOSTSTR, syncEntry.getJsonPostStr());

        // Insert oder Update
        db.insertWithOnConflict(TABLE_SYNCTODO, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    /**
     * Löschen des SyncTodoEntry mit bestimmter ID und bestimmter Session.
     * @param id
     * @param session
     */
    public void deleteSyncTodoEntry(int id, String session) {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = KEY_SYNCTODO_ID+" = '"+id+"' AND "+KEY_SYNCTODO_SESSIONKEY+" = '"+session+"'";
        db.delete(TABLE_SYNCTODO, where, null);
        db.close();
    }




}
