package com.axioma.taskmanager;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.axioma.taskmanager.async.AsyncCallback;
import com.axioma.taskmanager.async.GetTaskDetailsInBackground;
import com.axioma.taskmanager.async.RunTaskInBackground;

/**
 * @author rkannappan
 */
public class ShowTaskDetailsActivity extends Activity implements AsyncCallback {

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

      new GetTaskDetailsInBackground(ShowTaskDetailsActivity.this, taskName, taskType, taskTypeDesc, this).execute();
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
            new RunTaskInBackground(ShowTaskDetailsActivity.this, this.taskName, this.taskType, this.taskTypeDesc,
                     new RunTaskCallback()).execute();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   public void postProcessing(String results) {
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

   private class RunTaskCallback implements AsyncCallback {

      @Override
      public void postProcessing(String results) {
         Toast.makeText(ShowTaskDetailsActivity.this, "Task finished with status " + results, Toast.LENGTH_SHORT).show();

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.showNotification(results);
         }
      }

      @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
      private void showNotification(final String results) {
         // Prepare intent which is triggered if the
         // notification is selected

         //         Intent intent = new Intent(ShowTaskDetailsActivity.this, ShowTaskDetailsActivity.class);
         //         PendingIntent pIntent = PendingIntent.getActivity(ShowTaskDetailsActivity.this, 0, intent, 0);

         // Build notification
         Notification noti =
                  new Notification.Builder(ShowTaskDetailsActivity.this)
                           .setContentTitle(taskTypeDesc + " task " + taskName + " finished with status " + results)
                           .setContentText("Task Status").setSmallIcon(R.drawable.axioma_launcher).build();

         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

         // Hide the notification after its selected
         noti.flags |= Notification.FLAG_AUTO_CANCEL;

         notificationManager.notify(0, noti);
      }
   }
}