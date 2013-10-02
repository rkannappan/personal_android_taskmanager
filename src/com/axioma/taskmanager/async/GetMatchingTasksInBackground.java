package com.axioma.taskmanager.async;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.axioma.taskmanager.R;
import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;
import com.google.common.collect.BiMap;

/**
 * @author rkannappan
 */
public class GetMatchingTasksInBackground extends AsyncTask<Void, Void, String> {
   private final Context context;
   private final AsyncCallback callback;
   private final String url;

   public GetMatchingTasksInBackground(final Context context, final String taskType, final String owner, final String portfolio,
            final String benchmark, final String riskmodel, final String taskName,
            final BiMap<String, String> taskTypeDescToTaskTypeMap, final AsyncCallback callback) {
      this.context = context;
      this.callback = callback;

      String queryParams = getQueryParams(taskType, owner, portfolio, benchmark, riskmodel, taskName, taskTypeDescToTaskTypeMap);

      this.url = PreferenceUtil.getBaseWSURL(this.context) + "tasks" + queryParams;
      System.out.println(url);
   }

   @Override
   protected String doInBackground(Void... params) {
      return RestClientUtil.getJSONFromUrl(url, this.context);
   }

   @Override
   protected void onPostExecute(String results) {
      super.onPostExecute(results);
      System.out.println(results);
      callback.postProcessing(results);
   }

   private String getQueryParams(final String taskType, final String owner, final String portfolio, final String benchmark,
            final String riskmodel, final String taskName, final BiMap<String, String> taskTypeDescToTaskTypeMap) {
      List<Parameter> params = new ArrayList<Parameter>();

      if (!this.context.getString(R.string.feedback_tasktype).equals(taskType)) {
         params.add(new Parameter("taskType", taskTypeDescToTaskTypeMap.get(taskType)));
      }
      if (!this.context.getString(R.string.feedback_owner).equals(owner)) {
         params.add(new Parameter("owner", owner));
      }
      if (!this.context.getString(R.string.feedback_portfolio).equals(portfolio)) {
         params.add(new Parameter("portfolio", portfolio));
      }
      if (!this.context.getString(R.string.feedback_benchmark).equals(benchmark)) {
         params.add(new Parameter("benchmark", benchmark));
      }
      if (!this.context.getString(R.string.feedback_riskmodel).equals(riskmodel)) {
         params.add(new Parameter("riskmodel", riskmodel));
      }
      if (!(taskName == null || taskName.equals(""))) {
         params.add(new Parameter("taskName", taskName));
      }

      StringBuilder queryParams = new StringBuilder();
      if (params.size() > 0) {
         queryParams.append("?");
      }

      // Note that this is easy to do with Google guava joiner, but we are not using that dependency here. 
      for (Parameter param : params) {
         queryParams.append(param.name).append('=').append(param.value).append('&');
      }

      // Remove final &
      if (queryParams.length() > 0) {
         queryParams.setLength(queryParams.length() - 1);
      }

      return queryParams.toString();
   }

   private class Parameter {
      String name;
      String value;

      Parameter(final String paramName, final String paramValue) {
         this.name = paramName;
         this.value = paramValue;
      }

      @Override
      public String toString() {
         return this.name + " = " + this.value;
      }
   }
}