package com.axioma.taskmanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * @author rkannappan
 */
public class PlotResultsActivity extends Activity {

   private final String taskName = null;
   private final String taskRawName = null;
   private final String taskType = null;
   private final String taskTypeDesc = null;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setContentView(R.layout.activity_plot_results);

      // init example series data  
      GraphViewSeries exampleSeries =
               new GraphViewSeries(new GraphViewData[] { new GraphViewData(1, 2.0d), new GraphViewData(2, 1.5d),
                  new GraphViewData(3, 2.5d),
                  new GraphViewData(4, 1.0d) });

      GraphView graphView = new LineGraphView(this // context  
               , "GraphViewDemo" // heading  
      );
      graphView.addSeries(exampleSeries); // data  

      LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
      layout.addView(graphView);

      //      // Get the message from the intent
      //      Intent intent = getIntent();
      //      String taskType = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE);
      //      String taskTypeDesc = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC);
      //      String taskName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_NAME);
      //      String taskRawName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME);
      //
      //      this.taskType = taskType;
      //      this.taskTypeDesc = taskTypeDesc;
      //      this.taskName = taskName;
      //      this.taskRawName = taskRawName;
      //
      //      System.out.println("type in intent " + taskType);
      //      System.out.println("name in intent " + taskName);
      //      System.out.println("raw name in intent " + taskRawName);
      //      System.out.println("type desc in intent " + taskTypeDesc);
      //
      //      new GetTaskDetailsInBackground(ShowTaskDetailsActivity.this, taskName, taskRawName, taskType, taskTypeDesc, this).execute();
   }

   //   @Override
   //   public boolean onCreateOptionsMenu(Menu menu) {
   //      // Inflate the menu; this adds items to the action bar if it is present.
   //      getMenuInflater().inflate(R.menu.show_task_details, menu);
   //      return true;
   //   }

   //   @Override
   //   public boolean onOptionsItemSelected(MenuItem item) {
   //      // Handle presses on the action bar items
   //      switch (item.getItemId()) {
   //         case R.id.action_run:
   //            new RunTaskInBackground(ShowTaskDetailsActivity.this, this.taskName, this.taskRawName, this.taskType,
   //                     this.taskTypeDesc, new RunTaskCallback()).execute();
   //            return true;
   //         default:
   //            return super.onOptionsItemSelected(item);
   //      }
   //   }
   //
   //   @Override
   //   public void postProcessing(String results) {
   //      String[] paramValues = null;
   //
   //      try {
   //         JSONObject attribute = new JSONObject(results);
   //
   //         JSONObject values = attribute.getJSONObject("parameters");
   //
   //         paramValues = new String[(values.length() * 2) + 4];
   //
   //         String taskName = attribute.getString("name");
   //
   //         int i = 0;
   //
   //         paramValues[i++] = "Task Name";
   //         paramValues[i++] = IdentityName.valueOf(taskName).getName();
   //
   //         paramValues[i++] = "Task Type";
   //         paramValues[i++] = taskTypeDesc;
   //
   //         Iterator<Object> it = values.keys();
   //         while (it.hasNext()) {
   //            String paramName = it.next().toString();
   //            System.out.println(paramName);
   //            paramValues[i++] = this.getCleansedParamName(paramName);
   //
   //            String paramValue = values.getString(paramName);
   //            System.out.println(paramValue);
   //
   //            paramValues[i++] = this.getCleansedParamValue(paramValue);
   //         }
   //
   //         setContentView(R.layout.activity_show_task_details);
   //      } catch (JSONException e) {
   //         Log.e("JSON Parser", "Error parsing data " + e.toString());
   //      }
   //
   //      GridView gridView = (GridView) findViewById(R.id.taskDetails);
   //
   //      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, paramValues);
   //
   //      gridView.setAdapter(adapter);
   //   }
   //
   //   private class RunTaskCallback implements AsyncCallback {
   //
   //      @Override
   //      public void postProcessing(String results) {
   //         Toast.makeText(ShowTaskDetailsActivity.this, "Task finished with status " + results, Toast.LENGTH_SHORT).show();
   //
   //         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
   //            this.showNotification(results);
   //         }
   //      }
   //
   //      @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
   //      private void showNotification(final String results) {
   //         // Prepare intent which is triggered if the
   //         // notification is selected
   //
   //         Intent intent = new Intent(ShowTaskDetailsActivity.this, TaskFinderActivity.class);
   //         PendingIntent pIntent = PendingIntent.getActivity(ShowTaskDetailsActivity.this, 0, intent, 0);
   //
   //         // Build notification
   //         Notification noti =
   //                  new Notification.Builder(ShowTaskDetailsActivity.this)
   //                           .setContentTitle(taskTypeDesc + " task " + taskName + " finished with status " + results)
   //                           .setContentText(results).setSmallIcon(R.drawable.axioma_launcher).setContentIntent(pIntent).build();
   //
   //         NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
   //
   //         // Hide the notification after its selected
   //         noti.flags |= Notification.FLAG_AUTO_CANCEL;
   //
   //         notificationManager.notify(0, noti);
   //      }
   //   }
   //
   //   private String getCleansedParamName(final String paramName) {
   //      String cleansedName = this.paramNameToDisplayNameMap.get(paramName);
   //      if (cleansedName == null) {
   //         cleansedName = paramName;
   //      }
   //
   //      return cleansedName;
   //   }
   //
   //   // Returns double values as it is and removes prefixes from others
   //   private String getCleansedParamValue(final String paramValue) {
   //      if (paramValue == null || paramValue.trim().isEmpty()) {
   //         return "";
   //      }
   //
   //      // Not stripping off inidivual values inside the set for now
   //      if (paramValue.startsWith("[")) {
   //         return paramValue;
   //      }
   //
   //      try {
   //         Double.valueOf(paramValue);
   //         return paramValue;
   //      } catch (NumberFormatException ex) {
   //         return IdentityName.valueOf(paramValue).getName();
   //      }
   //   }
}