package com.example.michaelhodl.webserviceexample3.utils;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.example.michaelhodl.webserviceexample3.activities.AllTodosActivity;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by andreas on 21.04.2017.
 */

public class DeleteTodoAction {

    private String TAG = DeleteTodoAction.class.getSimpleName();
    private ProgressDialog pDialog;
    private AllTodosActivity mainDialog;
    private String todoId;
    private String sessionId;
    private String httpResponse = null;
    private String url;

    // Konstruktor mit den Parametern, welche gebraucht werden, um die delete action im AllTodosActivity auszuführen
    public DeleteTodoAction(AllTodosActivity mainDialog, String todoId, String sessionId) {
        this.mainDialog = mainDialog;
        this.todoId = todoId;
        this.sessionId = sessionId;
    }

    public void runDeleteAction() {
        runDelete();
    }

    private void runDelete() {
        // execute the asyn task to get the json from the server by making a HTTP call.
        runAsync();

        // TODO: Das Delay muss noch ins Utils übernommen werden und in allen Stellen von dort augerufen werden
        // bissl primitiver ansatz, um die problematik zu loesen
        //   dass der server ein bisschen zeit braucht um zu responden nachdem der HTTP call abgesetzt wurde...
        // Solange httpResponse nicht befuellt ist (mit dem json string, den der server liefert), warten.
        // Auch wenn httpResponse nie befuellt werden sollte, erstmal ca. 4 Sekunden (bzw. bis 4000 zaehlen) abwarten.
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
    }

    /**
     * Async task class delete todo by making HTTP call
     */
    private class AsyncCaller extends AsyncTask<Void, Void, Void> {

        DeleteTodoAction caller;
        boolean isInternetConnected;
        HttpHandler sh;

        AsyncCaller(DeleteTodoAction caller){
            this.caller = caller;
            sh = new HttpHandler();
            isInternetConnected = sh.isNetworkAvailable(mainDialog.getApplicationContext());
        }

        @Override
        //this method will be running on UI thread
        protected void onPreExecute() {
            super.onPreExecute();



            // Showing progress dialog
            pDialog = new ProgressDialog(mainDialog);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        //this method will be running on background thread so dont update UI from here
        //do your long running http tasks here, you dont want to pass argument and u can access the parent class variable url over here
        protected Void doInBackground(Void... arg0) {

            if(isInternetConnected) {
                Log.e(TAG, "--- internet connection! ---");
                // headers - at delete action only the session key is necessary, the todo id will be added to the path of the url
                ArrayList<NameValuePair> headers = new ArrayList<>();
                NameValuePair h1 = new NameValuePair();
                h1.setName("session");
                h1.setValue(sessionId);
                NameValuePair h2 = new NameValuePair();
                h2.setName("Accept");
                h2.setValue("text/plain");
                headers.add(h1);
                headers.add(h2);

                // add the todo id to the path from the url
                url = "http://campus02win14mobapp.azurewebsites.net/Todo/" + todoId;

                // Making a delete request to url and getting response
                String jsonStr = sh.makeMyServiceCall(url, "DELETE", headers, null, null);//sh.makeServiceCall(url);
                // fill the httpResponse with the json string. If the response is null there was a problem at the server, if it is empty the request was successful
                caller.setHttpResponse(jsonStr);

                Log.e(TAG, "Response from url (jsonStr) delete action: " + jsonStr);
                Log.e(TAG, "Response from url (httpResponse) delete action: " + httpResponse);
                return null;
            } // else: if no internet connection is available
            else {
                DBHandler localDb = new DBHandler(mainDialog);
                try {
                    localDb.deleteTodo(todoId, sessionId);
                    localDb.getTodos(sessionId);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        @Override
        //this method will be running on UI thread
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            Log.e(TAG, "status: (im onPostExecute) delete action: " + this.getStatus());
        }

    } // end private class AsyncCaller

    private void setHttpResponse(String httpResponse) {
        this.httpResponse = httpResponse;
    }

    private void runAsync()
    {
        new AsyncCaller(this).execute();
    }
}
