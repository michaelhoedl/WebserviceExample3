package com.example.michaelhodl.webserviceexample3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;
    ArrayList<HashMap<String, String>> contactList;

    public static final String EXTRA_MESSAGE = "com.example.michaelhodl.webserviceexample2.MESSAGE";

    // URL to get contacts JSON
    //private static String url = "http://api.androidhive.info/contacts/";
    private static String url = "http://campus02win14mobapp.azurewebsites.net/User/login";

    private String httpResponse = null;
    private String mymail = null;
    private String mypwd = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList = new ArrayList<>();

        //lv = (ListView) findViewById(R.id.list);




    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {

        EditText emymail = (EditText) findViewById(R.id.editTextMail);
        EditText emypwd = (EditText) findViewById(R.id.editTextPwd);
        MainActivity.this.setMymail(emymail.getText().toString());
        MainActivity.this.setMypwd(emypwd.getText().toString());
        Log.e(TAG, "mymail: " + mymail);
        Log.e(TAG, "mypwd: " + mypwd);

        AsyncTask bla = new AsyncCaller(this).execute();
        Log.e(TAG, "status (im sendMessage): " + bla.getStatus());


        //String ret = MainActivity.this.getHttpResponse(); //=doHttpHandling();

        // Solange httpResponse nicht befuellt ist, warten.
        // Auch wenn httpResponse nie befuellt werden sollte, erstmal 4 Sekunden abwarten.
        int x = 0;
        while(httpResponse == null && x < 4000) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x += 1;
        }
        Log.e(TAG, "x= " + x);


        Log.e(TAG, "httpResponse: " + httpResponse);


        if (httpResponse == null){
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("No Session found");
            alertDialog.setMessage("No Session found. Your Username or Password is incorrect.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
        else {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra(EXTRA_MESSAGE, httpResponse);
            startActivity(intent);
        }
    }


/*    private String doHttpHandling(){

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
        h3.setValue(new String("text/plain"));
        headers.add(h1);
        headers.add(h2);
        headers.add(h3);


        // Making a request to url and getting response
        String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null);//sh.makeServiceCall(url);

        httpResponse = jsonStr;

        Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
        Log.e(TAG, "Response from url (httpResponse): " + httpResponse);

        return jsonStr;
    }*/

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



    /**
     * Async task class to get json by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        MainActivity caller;

        AsyncCaller(MainActivity caller){
            this.caller = caller;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
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
            h3.setValue(new String("text/plain"));
            headers.add(h1);
            headers.add(h2);
            headers.add(h3);


            // Making a request to url and getting response
            String jsonStr = sh.makeMyServiceCall(url,"GET",headers, null);//sh.makeServiceCall(url);

            //httpResponse = jsonStr;

            caller.setHttpResponse(jsonStr);

            Log.e(TAG, "Response from url: " + jsonStr);

            Log.e(TAG, "Response from url (jsonStr): " + jsonStr);
            Log.e(TAG, "Response from url (httpResponse): " + httpResponse);

/*            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray contacts = jsonObj.getJSONArray("contacts");

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString("id");
                        String name = c.getString("name");
                        String email = c.getString("email");
                        String address = c.getString("address");
                        String gender = c.getString("gender");

                        // Phone node is JSON Object
                        JSONObject phone = c.getJSONObject("phone");
                        String mobile = phone.getString("mobile");
                        String home = phone.getString("home");
                        String office = phone.getString("office");

                        // tmp hash map for single contact
                        HashMap<String, String> contact = new HashMap<>();

                        // adding each child node to HashMap key => value
                        contact.put("id", id);
                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("mobile", mobile);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }*/

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
    /*            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, contactList,
                    R.layout.list_item, new String[]{"name", "email",
                    "mobile"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile});

            lv.setAdapter(adapter);
    */
            Log.e(TAG, "status: (im onPostExecute): " + this.getStatus());
        }

    } // end private class AsyncCaller


}
