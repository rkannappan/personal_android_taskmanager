package com.axioma.taskmanager;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;

/**
 * @author rkannappan
 */
public class TaskFinderActivity extends Activity implements OnSharedPreferenceChangeListener {

   public final static String SELECTED_TASK_TYPE = "com.axioma.taskfinder.selected_task_type";
   public final static String SELECTED_OWNER = "com.axioma.taskfinder.selected_owner";
   public final static String SELECTED_PORTFOLIO = "com.axioma.taskfinder.selected_portfolio";
   public final static String SELECTED_BENCHMARK = "com.axioma.taskfinder.selected_benchmark";
   public final static String SELECTED_RISKMODEL = "com.axioma.taskfinder.selected_riskmodel";
   public final static String SELECTED_TASK_NAME = "com.axioma.taskfinder.selected_task_name";

   public final static String HOST_NAME = "host_name";
   public final static String PORT_NUMBER = "port_number";
   public final static String WS_CONTEXT = "ws_context";
   public final static String APP_USER_NAME = "app_user_name";
   public final static String APP_USER_PASSWORD = "app_user_password";

   public final static String DEFAULT_HOST_NAME = "http://localhost";
   public final static String DEFAULT_PORT_NUMBER = "8080";
   public final static String DEFAULT_WS_CONTEXT = "axioma-websrv";
   public final static String DEFAULT_APP_USER_NAME = "axioma";
   public final static String DEFAULT_APP_USER_PASSWORD = "axioma";

   private boolean preferencesChanged = false;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_task_finder);

      populateSpinner((Spinner) findViewById(R.id.ownerSpinner), PreferenceUtil.getBaseWSURL(this) + "userNames",
               this.getString(R.string.feedback_owner));
      populateSpinner((Spinner) findViewById(R.id.portfolioSpinner), PreferenceUtil.getBaseWSURL(this) + "portfolioNames",
               this.getString(R.string.feedback_portfolio));
      populateSpinner((Spinner) findViewById(R.id.benchmarkSpinner), PreferenceUtil.getBaseWSURL(this)
               + "attributeNames?tagName=Benchmark",
               this.getString(R.string.feedback_benchmark));
      populateSpinner((Spinner) findViewById(R.id.riskmodelSpinner), PreferenceUtil.getBaseWSURL(this) + "factorRiskModelNames",
               this.getString(R.string.feedback_riskmodel));

      PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.task_finder, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle presses on the action bar items
      switch (item.getItemId()) {
         case R.id.action_settings:
            Intent i = new Intent(getApplicationContext(), AppSettingsActivity.class);
            startActivity(i);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   @Override
   protected void onResume() {
      super.onResume();
      if (this.preferencesChanged) {
         this.onCreate(null);
         this.preferencesChanged = false;
      }
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      this.preferencesChanged = true;

      //      finish();
      //      startActivity(getIntent());
   }

   private void populateSpinner(Spinner spinner, final String url, final String defaultSelection) {
      new GetNamesInBackground(spinner, url, defaultSelection).execute();
   }

   private class GetNamesInBackground extends AsyncTask<Void, Void, String> {
      private final Spinner spinner;
      private final String url;
      private final String defaultSelection;

      private GetNamesInBackground(final Spinner spinner, final String url, final String defaultSelection) {
         this.spinner = spinner;
         this.url = url;
         this.defaultSelection = defaultSelection;
      }

      @Override
      protected String doInBackground(Void... params) {
         return new RestClientUtil().getJSONFromUrl(url, PreferenceUtil.getAppUserName(getApplicationContext()),
                  PreferenceUtil.getAppPassword(getApplicationContext()));
      }

      @Override
      protected void onPostExecute(String results) {
         super.onPostExecute(results);
         postProcessing(results, this.spinner, this.defaultSelection);
      }
   }

   private void postProcessing(final String results, final Spinner spinner, final String defaultSelection) {
      List<String> names = new ArrayList<String>();

      names.add(defaultSelection);

      // try parse the string to a JSON object
      try {
         JSONArray entries = new JSONArray(results);

         for (int i = 0; i < entries.length(); i++) {
            String entry = (String) entries.get(i);
            names.add(entry);
         }
      } catch (JSONException e) {
         Log.e("JSON Parser", "Error parsing data " + e.toString());
      }

      ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
      dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(dataAdapter);
   }

   public void findTasks(final View view) {
      final String taskType = ((Spinner) findViewById(R.id.taskTypeSpinner)).getSelectedItem().toString();
      System.out.println(taskType);

      final String owner = ((Spinner) findViewById(R.id.ownerSpinner)).getSelectedItem().toString();
      System.out.println(owner);

      final String portfolio = ((Spinner) findViewById(R.id.portfolioSpinner)).getSelectedItem().toString();
      System.out.println(portfolio);

      final String benchmark = ((Spinner) findViewById(R.id.benchmarkSpinner)).getSelectedItem().toString();
      System.out.println(benchmark);

      final String riskmodel = ((Spinner) findViewById(R.id.riskmodelSpinner)).getSelectedItem().toString();
      System.out.println(riskmodel);

      final String taskName = ((EditText) findViewById(R.id.taskNameText)).getText().toString();
      System.out.println("task name is " + taskName);

      Intent intent = new Intent(getApplicationContext(), ShowTasksActivity.class);
      intent.putExtra(TaskFinderActivity.SELECTED_TASK_TYPE, taskType);
      intent.putExtra(TaskFinderActivity.SELECTED_OWNER, owner);
      intent.putExtra(TaskFinderActivity.SELECTED_PORTFOLIO, portfolio);
      intent.putExtra(TaskFinderActivity.SELECTED_BENCHMARK, benchmark);
      intent.putExtra(TaskFinderActivity.SELECTED_RISKMODEL, riskmodel);
      intent.putExtra(TaskFinderActivity.SELECTED_TASK_NAME, taskName);
      startActivity(intent);
   }
}