package com.example.campus02.webserviceexample3.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.campus02.webserviceexample3.model.UserEntry;
import com.example.campus02.webserviceexample3.utils.DBHandler;
import com.example.campus02.webserviceexample3.utils.HttpHandler;
import com.example.campus02.webserviceexample3.utils.NameValuePair;
import com.example.campus02.webserviceexample3.R;

import java.util.ArrayList;
import java.security.*;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    public static final String EXTRA_MESSAGE = "com.example.campus02.webserviceexample3.MESSAGE";
    private static String url = "http://campus02win14mobapp.azurewebsites.net/User/login";
    private String httpResponse = null;
    private String mymail = null;
    private String mypwd = null;
    private DBHandler localDb = new DBHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Called when the user taps the Login button, a Message is sent to the Webservice
     * @param view
     */
    public void sendMessage(View view) {

        String emymail = ((EditText) findViewById(R.id.editTextMail)).getText().toString();
        String emypwd = ((EditText) findViewById(R.id.editTextPwd)).getText().toString();
        MainActivity.this.setMymail(emymail);
        MainActivity.this.setMypwd(emypwd);
        Log.e(TAG, "mymail: " + mymail);
        Log.e(TAG, "mypwd: " + mypwd);

        // some simple input validation:
        // if nothing was input into the text fields, then show message, otherwise continue with login process
        if(TextUtils.isEmpty(emymail) || TextUtils.isEmpty(emypwd)) {
            showMyAlert("No Data entered", "Your Username and/or Password is empty. Both fields must be filled!");
        } else {

            // execute the async task to get the json from the server by making a HTTP call.
            AsyncTask async = new AsyncCaller(this).execute();
            Log.e(TAG, "status (im sendMessage): " + async.getStatus());

            // bissl primitiver ansatz, um die problematik zu loesen
            //   dass der server ein bisschen zeit braucht um zu responden nachdem der HTTP call abgesetzt wurde...
            // Solange httpResponse nicht befuellt ist (mit dem json string, den der server liefert), warten.
            // Auch wenn httpResponse nie befuellt werden sollte, erstmal ca. 4 Sekunden (bzw. bis 4000 zaehlen) abwarten.
            int x = 0;
            while (TextUtils.isEmpty(httpResponse) && x <= 4000) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                x += 1;
            }

            Log.e(TAG, "x= " + x);
            Log.e(TAG, "httpResponse: " + httpResponse);


            UserEntry myuser = null;

            // if server did not return any response, then show a message dialog saying that no session was found.
            if (TextUtils.isEmpty(httpResponse)) {
                try {
                    //verschlüsselt das Passwort bevor es in der lokalen Datenbank geprüft wird
                    emypwd = encrypt(emypwd);
                } catch (NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                myuser = localDb.getUser(emymail, emypwd);
                if (myuser != null) {
                    if (!(myuser.getMail().equals(emymail) && myuser.getPwd().equals(emypwd))) {
                        showMyAlert("No Session found", "No Session found. Your Username or Password is incorrect.");
                    } else {
                        httpResponse = myuser.getSessionKey();
                        Intent intent = new Intent(this, AllTodosActivity.class);
                        intent.putExtra(EXTRA_MESSAGE, httpResponse); // we have to send the session_id.
                        startActivity(intent);
                    }
                } else {
                    httpResponse = null;
                    showMyAlert("No Session found","No Session found. Your Username or Password is incorrect.");
                }

            } // else (if server returns session), then switch to the new screen where a list of all todos is shown.
            else {
                //TODO: insert the user only once?
                try {
                    //verschlüsselt das Passwort bevor es in die lokale Datenbank geschrieben wird
                    emypwd = encrypt(emypwd);
                } catch (NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                myuser = new UserEntry(emymail, emypwd, httpResponse);
                localDb.addUser(myuser);

                Intent intent = new Intent(this, AllTodosActivity.class);
                intent.putExtra(EXTRA_MESSAGE, httpResponse); // we have to send the session_id.
                startActivity(intent);
            }
        } // end if username and/or password is empty
    }


    /**
     * liefert ein MD5 verschlüsseltes Passwort zurück für einen String pwd.
     * @param pwd
     * @return
     * @throws NoSuchAlgorithmException
     */
    private String encrypt (String pwd) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.reset();
        m.update(pwd.getBytes());
        byte[] digest = m.digest();

        String hexString = "";
        for (int i=0; i<digest.length; i++) {
            if(digest[i] <= 15 && digest[i] >= 0){
                hexString += "0";
            }
            hexString += Integer.toHexString(0xFF & digest[i]);
        }
        return hexString;
    }


    /**
     * Einfache Methode, um das Erstellen einer Alert Message Box mit Titel s und Message s1 auszulagern.
     * @param s
     * @param s1
     */
    private void showMyAlert(String s, String s1) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(s);
        alertDialog.setMessage(s1);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    // ---------------------------------------------------------------------------------------------

    // Getters und Setters

    public String getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }

    public String getMymail() {
        return mymail;
    }

    public void setMymail(String mymail) {
        this.mymail = mymail;
    }

    public String getMypwd() {
        return mypwd;
    }

    public void setMypwd(String mypwd) {
        this.mypwd = mypwd;
    }

    // ---------------------------------------------------------------------------------------------


    /**
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        MainActivity caller;
        AsyncCaller(MainActivity caller){
            this.caller = caller;
        }

        @Override
        /**
         * this method will be running on UI thread
         */
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
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
            HttpHandler sh = new HttpHandler();

            // set headers
            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            NameValuePair h1 = new NameValuePair();
            h1.setName("mail");
            h1.setValue(MainActivity.this.getMymail());
            NameValuePair h2 = new NameValuePair();
            h2.setName("pwd");
            h2.setValue(MainActivity.this.getMypwd());
            NameValuePair h3 = new NameValuePair();
            h3.setName("Accept");
            h3.setValue("text/plain");
            headers.add(h1);
            headers.add(h2);
            headers.add(h3);


            // Making a request to url and getting response
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null, null); //sh.makeServiceCall(url);
            // fill the httpResponse with the json string. The httpResponse basically just is the session_id returned by the server.
            caller.setHttpResponse(jsonStr);

            Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
            Log.e(TAG, "Response from url (httpResponse): " + httpResponse);

            return null;
        }

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


}
