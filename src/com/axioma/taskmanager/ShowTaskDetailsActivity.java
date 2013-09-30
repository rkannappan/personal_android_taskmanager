package com.axioma.taskmanager;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;

/**
 * @author rkannappan
 */
public class ShowTaskDetailsActivity extends Activity {

   private String taskName = null;
   private String taskType = null;
   private String taskTypeDesc = null;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Get the message from the intent
      Intent intent = getIntent();
      String taskType = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE);
      String taskName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_NAME);
      String taskTypeDesc = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC);

      this.taskType = taskType;
      this.taskName = taskName;
      this.taskTypeDesc = taskTypeDesc;

      System.out.println("type in intent " + taskType);
      System.out.println("name in intent " + taskName);
      System.out.println("type desc in intent " + taskTypeDesc);

      new GetTaskDetailsInBackground().execute();
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.show_task_details, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle presses on the action bar items
      switch (item.getItemId()) {
         case R.id.action_run:
            new RunTaskInBackground().execute();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   private void postProcessing(String results) {
      String[] paramValues = null;

      try {
         JSONObject attribute = new JSONObject(results);

         JSONObject values = attribute.getJSONObject("parameters");

         paramValues = new String[(values.length() * 2) + 4];

         String taskName = attribute.getString("name");

         int i = 0;

         paramValues[i++] = "Task Name";
         paramValues[i++] = taskName;

         paramValues[i++] = "Task Type";
         paramValues[i++] = taskTypeDesc;

         Iterator<String> it = values.keys();
         while (it.hasNext()) {
            String paramName = it.next().toString();
            System.out.println(paramName);
            paramValues[i++] = paramName;


            String paramValue = values.getString(paramName);
            System.out.println(paramValue);

            paramValues[i++] = paramValue;
         }

         setContentView(R.layout.activity_show_task_details);
      } catch (JSONException e) {
         Log.e("JSON Parser", "Error parsing data " + e.toString());
      }

      GridView gridView = (GridView) findViewById(R.id.taskDetails);

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, paramValues);

      gridView.setAdapter(adapter);
   }

   private class GetTaskDetailsInBackground extends AsyncTask<Void, Void, String> {
      private ProgressDialog dialog;

      @Override
      protected void onPreExecute() {
         this.dialog = new ProgressDialog(ShowTaskDetailsActivity.this);
         this.dialog.setMessage("Getting task details for " + taskTypeDesc + " task - " + taskName + "...");
         this.dialog.show();
      }

      @Override
      protected String doInBackground(Void... params) {
         String url = PreferenceUtil.getBaseWSURL(getApplicationContext()) + "tasks/" + taskType + "/" + taskName + "/";
         return getJSONFromUrl(url);
      }

      @Override
      protected void onPostExecute(String results) {
         super.onPostExecute(results);
         postProcessing(results);
         this.dialog.dismiss();
      }
   }

   private class RunTaskInBackground extends AsyncTask<Void, String, String> {
      private ProgressDialog dialog;

      private boolean taskRunning = false;

      @Override
      protected void onPreExecute() {
         this.dialog = new ProgressDialog(ShowTaskDetailsActivity.this);
         this.dialog.setMessage("Running " + taskTypeDesc + " task - " + taskName + "...");
         this.dialog.show();

         this.taskRunning = true;
      }

      @Override
      protected String doInBackground(Void... params) {
         String url = PreferenceUtil.getBaseWSURL(getApplicationContext()) + "tasks/run/" + taskType + "/" + taskName + "/";

         new Timer().schedule(new ConsumeProgressMessage(), 2000);

         String json = getJSONFromUrl(url);
         return json;
      }

      @Override
      protected void onProgressUpdate(String... progress) {
         if (this.dialog.isShowing()) {
            this.dialog.setMessage(progress[0]);
         }
      }

      @Override
      protected void onPostExecute(String results) {
         super.onPostExecute(results);

         this.taskRunning = false;
         //         postProcessing(results);
         this.dialog.dismiss();
      }

      private class ConsumeProgressMessage extends TimerTask {
         @Override
         public void run() {
            if (!taskRunning) {
               this.cancel();
            }
            String url =
                     PreferenceUtil.getBaseWSURL(getApplicationContext()) + "tasks/run/progress/" + taskType + "/" + taskName
                              + "/";
            String json = getJSONFromUrl(url);
            System.out.println(json);
            publishProgress(json);
         }
      }
   }

   private String getJSONFromUrl(final String url) {
      return new RestClientUtil().getJSONFromUrl(url, PreferenceUtil.getAppUserName(getApplicationContext()),
               PreferenceUtil.getAppPassword(getApplicationContext()));
   }
}