package com.axioma.taskmanager.async;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;

/**
 * @author rkannappan
 */
public class RunTaskInBackground extends AsyncTask<Void, String, String> {
   private ProgressDialog dialog;

   private boolean taskRunning = false;

   private final Context context;
   private final String taskName;
   private final String taskType;
   private final String taskTypeDesc;

   public RunTaskInBackground(final Context context, final String taskName, final String taskType, final String taskTypeDesc) {
      this.context = context;
      this.taskName = taskName;
      this.taskType = taskType;
      this.taskTypeDesc = taskTypeDesc;
   }

   @Override
   protected void onPreExecute() {
      this.dialog = new ProgressDialog(context);

      this.dialog.setMessage("Running " + taskTypeDesc + " task - " + taskName + "...");
      this.dialog.show();

      this.taskRunning = true;
   }

   @Override
   protected String doInBackground(Void... params) {
      this.flushProgressMessages();

      this.consumeProgressMessages();

      String url = PreferenceUtil.getBaseWSURL(this.context) + "tasks/run/" + taskType + "/" + taskName + "/";
      String status = RestClientUtil.getJSONFromUrl(url, this.context);

      this.flushProgressMessages();
      
      Toast.makeText(this.context, "Task finished with status " + status, Toast.LENGTH_SHORT).show();

      return status;
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

      // Nothing to postProcess here - Maybe, send a mobile notification
      //         postProcessing(results);
      this.dialog.dismiss();
   }

   private void flushProgressMessages() {
      String url = PreferenceUtil.getBaseWSURL(this.context) + "tasks/run/flush/" + taskType + "/" + taskName + "/";
      RestClientUtil.getJSONFromUrl(url, this.context);
   }

   private void consumeProgressMessages() {
      new Timer().scheduleAtFixedRate(new ConsumeProgressMessage(), 1000, 1000);
   }

   private class ConsumeProgressMessage extends TimerTask {
      @Override
      public void run() {
         if (!taskRunning) {
            this.cancel();
         }
         String url = PreferenceUtil.getBaseWSURL(context) + "tasks/run/progress/" + taskType + "/" + taskName + "/";
         String json = RestClientUtil.getJSONFromUrl(url, context);
         if (json != null && !json.trim().equals("")) {
            publishProgress(json);
         }
      }
   }
}