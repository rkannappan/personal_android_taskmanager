package com.axioma.taskmanager.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;

/**
 * @author rkannappan
 */
public class GetTaskResultsInBackground extends AsyncTask<Void, Void, String> {
   private ProgressDialog dialog;

   private final Context context;
   private final String taskName;
   private final String taskRawName;
   private final String taskType;
   private final String taskTypeDesc;
   private final AsyncCallback callback;

   public GetTaskResultsInBackground(final Context context, final String taskName, final String taskRawName,
            final String taskType, final String taskTypeDesc,
            final AsyncCallback callback) {
      this.context = context;
      this.taskName = taskName;
      this.taskRawName = taskRawName;
      this.taskType = taskType;
      this.taskTypeDesc = taskTypeDesc;
      this.callback = callback;
   }

   @Override
   protected void onPreExecute() {
      this.dialog = new ProgressDialog(this.context);
      this.dialog.setMessage("Getting task results for " + taskTypeDesc + " task - " + taskName + "...");
      this.dialog.show();
   }

   @Override
   protected String doInBackground(Void... params) {
      String url =
               PreferenceUtil.getBaseWSURL(this.context) + "tasks/results/" + taskType + "/" + taskRawName
                        + "/Active Predicted Variance/";
      String activePredictedVarianceJson = RestClientUtil.getJSONFromUrl(url, this.context);

      url = PreferenceUtil.getBaseWSURL(this.context) + "tasks/results/" + taskType + "/" + taskRawName + "/Reference Value/";
      String referenceValueJson = RestClientUtil.getJSONFromUrl(url, this.context);

      return activePredictedVarianceJson + "@" + referenceValueJson;
   }

   @Override
   protected void onPostExecute(String results) {
      super.onPostExecute(results);
      this.callback.postProcessing(results);
      this.dialog.dismiss();
   }
}