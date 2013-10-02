package com.axioma.taskmanager.async;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

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
   private final AsyncCallback callback;

   public RunTaskInBackground(final Context context, final String taskName, final String taskType, final String taskTypeDesc,
            final AsyncCallback callback) {
      this.context = context;
      this.taskName = taskName;
      this.taskType = taskType;
      this.taskTypeDesc = taskTypeDesc;
      this.callback = callback;
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
      this.dialog.dismiss();

      this.callback.postProcessing(results);
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