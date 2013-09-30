package com.axioma.taskmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.axioma.taskmanager.util.PreferenceUtil;
import com.axioma.taskmanager.util.RestClientUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * @author rkannappan
 */
public class ShowTasksActivity extends FragmentActivity {

   public final static String SELECTED_TASK_TYPE = "com.axioma.showtasksactivity.selected_task_type";
   public final static String SELECTED_TASK_NAME = "com.axioma.showtasksactivity.selected_task_name";
   public final static String SELECTED_TASK_TYPE_DESC = "com.axioma.showtasksactivity.selected_task_type_desc";

   /**
    * The {@link android.support.v4.view.PagerAdapter} that will provide
    * fragments for each of the sections. We use a
    * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
    * will keep every loaded fragment in memory. If this becomes too memory
    * intensive, it may be best to switch to a
    * {@link android.support.v4.app.FragmentStatePagerAdapter}.
    */
   SectionsPagerAdapter mSectionsPagerAdapter;

   BiMap<String, String> taskTypeDescToTaskTypeMap = HashBiMap.create();

   /**
    * The {@link ViewPager} that will host the section contents.
    */
   ViewPager mViewPager;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_show_tasks);

      taskTypeDescToTaskTypeMap.put("Import Axioma Data", "IMPORT_AXIOMA_DATA");
      taskTypeDescToTaskTypeMap.put("Import Attribute Data", "IMPORT_ATTRIBUTE_DATA");
      taskTypeDescToTaskTypeMap.put("Import Portfolio Data", "IMPORT_PORTFOLIO_DATA");
      taskTypeDescToTaskTypeMap.put("Import Composite Data", "IMPORT_COMPOSITE_DATA");
      taskTypeDescToTaskTypeMap.put("Import Classification Data", "IMPORT_CLASSIFICATION_DATA");
      taskTypeDescToTaskTypeMap.put("Factor Performance Attribution", "PERFORMANCE_ATTRIBUTION_FACTOR");
      taskTypeDescToTaskTypeMap.put("Returns Performance Attribution", "PERFORMANCE_ATTRIBUTION_RETURNS");
      taskTypeDescToTaskTypeMap.put("Risk Analysis", "RISK_ANALYSIS");
      taskTypeDescToTaskTypeMap.put("Risk Model Machine", "RISK_MODEL_MACHINE");
      taskTypeDescToTaskTypeMap.put("Report", "REPORT");

      // Get the message from the intent
      Intent intent = getIntent();
      String taskType = intent.getStringExtra(TaskFinderActivity.SELECTED_TASK_TYPE);
      System.out.println(taskType);
      String owner = intent.getStringExtra(TaskFinderActivity.SELECTED_OWNER);
      System.out.println(owner);
      String portfolio = intent.getStringExtra(TaskFinderActivity.SELECTED_PORTFOLIO);
      System.out.println(portfolio);
      String benchmark = intent.getStringExtra(TaskFinderActivity.SELECTED_BENCHMARK);
      System.out.println(benchmark);
      String riskmodel = intent.getStringExtra(TaskFinderActivity.SELECTED_RISKMODEL);
      System.out.println(riskmodel);
      String taskName = intent.getStringExtra(TaskFinderActivity.SELECTED_TASK_NAME);
      System.out.println(taskName);

      String queryParams = getQueryParams(taskType, owner, portfolio, benchmark, riskmodel, taskName);

      String fullTasksUrl = PreferenceUtil.getBaseWSURL(this) + "tasks" + queryParams;
      System.out.println(fullTasksUrl);

      new GetMatchingTasksInBackground(fullTasksUrl).execute();
   }

   private String getQueryParams(final String taskType, final String owner, final String portfolio, final String benchmark,
            final String riskmodel, final String taskName) {
      List<Parameter> params = new ArrayList<Parameter>();

      if (!this.getString(R.string.feedback_tasktype).equals(taskType)) {
         params.add(new Parameter("taskType", this.taskTypeDescToTaskTypeMap.get(taskType)));
      }
      if (!this.getString(R.string.feedback_owner).equals(owner)) {
         params.add(new Parameter("owner", owner));
      }
      if (!this.getString(R.string.feedback_portfolio).equals(portfolio)) {
         params.add(new Parameter("portfolio", portfolio));
      }
      if (!this.getString(R.string.feedback_benchmark).equals(benchmark)) {
         params.add(new Parameter("benchmark", benchmark));
      }
      if (!this.getString(R.string.feedback_riskmodel).equals(riskmodel)) {
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

   private class GetMatchingTasksInBackground extends AsyncTask<Void, Void, String> {
      private final String url;

      private GetMatchingTasksInBackground(final String url) {
         this.url = url;
      }

      @Override
      protected String doInBackground(Void... params) {
         return new RestClientUtil().getJSONFromUrl(url, PreferenceUtil.getAppUserName(getApplicationContext()),
                  PreferenceUtil.getAppPassword(getApplicationContext()));
      }

      @Override
      protected void onPostExecute(String results) {
         super.onPostExecute(results);
         System.out.println(results);
         postProcessing(results);
      }
   }

   private void postProcessing(String results) {
      // Create the adapter that will return a fragment for each of the three
      // primary sections of the app.
      mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), results);

      // Set up the ViewPager with the sections adapter.
      mViewPager = (ViewPager) findViewById(R.id.pager);
      mViewPager.setAdapter(mSectionsPagerAdapter);
   }

   /**
    * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
    * one of the sections/tabs/pages.
    */
   public class SectionsPagerAdapter extends FragmentPagerAdapter {
      private final Multimap<String, String> taskTypeToNameMap = LinkedListMultimap.create();

      public SectionsPagerAdapter(FragmentManager fm, String results) {
         super(fm);

         try {
            JSONArray entries = new JSONArray(results);

            for (int i = 0; i < entries.length(); i++) {
               JSONArray entry = (JSONArray) entries.get(i);
               taskTypeToNameMap.put(entry.getString(0), entry.getString(1));
            }
         } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
         }
      }

      @Override
      public Fragment getItem(int position) {
         Pair<String, Collection<String>> fragmentTasks = getTasksForFragment(position);
         String taskType = fragmentTasks.first;
         Collection<String> tasks = fragmentTasks.second;

         Fragment fragment = new TasksSectionFragment();
         Bundle args = new Bundle();
         args.putString(TasksSectionFragment.TASK_TYPE, taskType);
         args.putString(TasksSectionFragment.TASK_TYPE_DESC, taskTypeDescToTaskTypeMap.inverse().get(taskType));
         System.out.println("tt desc " + taskTypeDescToTaskTypeMap.inverse().get(taskType));
         args.putStringArrayList(TasksSectionFragment.TASK_NAMES, new ArrayList<String>(tasks));
         fragment.setArguments(args);
         return fragment;
      }

      @Override
      public int getCount() {
         // Show 3 total pages.
         return 10;
      }

      @Override
      public CharSequence getPageTitle(int position) {
         Locale l = Locale.getDefault();
         switch (position) {
            case 0:
               return getString(R.string.ra_tasks).toUpperCase(l);
            case 1:
               return getString(R.string.fbpa_tasks).toUpperCase(l);
            case 2:
               return getString(R.string.rbpa_tasks).toUpperCase(l);
            case 3:
               return getString(R.string.report_tasks).toUpperCase(l);
            case 4:
               return getString(R.string.rmm_tasks).toUpperCase(l);
            case 5:
               return getString(R.string.ax_import_tasks).toUpperCase(l);
            case 6:
               return getString(R.string.port_import_tasks).toUpperCase(l);
            case 7:
               return getString(R.string.att_import_tasks).toUpperCase(l);
            case 8:
               return getString(R.string.comp_import_tasks).toUpperCase(l);
            case 9:
               return getString(R.string.classif_import_tasks).toUpperCase(l);
         }
         return null;
      }

      private Pair<String, Collection<String>> getTasksForFragment(final int position) {
         switch (position) {
            case 0:
               return new Pair<String, Collection<String>>("RISK_ANALYSIS", this.taskTypeToNameMap.get("RISK_ANALYSIS"));
            case 1:
               return new Pair<String, Collection<String>>("PERFORMANCE_ATTRIBUTION_FACTOR",
                        this.taskTypeToNameMap.get("PERFORMANCE_ATTRIBUTION_FACTOR"));
            case 2:
               return new Pair<String, Collection<String>>("PERFORMANCE_ATTRIBUTION_RETURNS",
                        this.taskTypeToNameMap.get("PERFORMANCE_ATTRIBUTION_RETURNS"));
            case 3:
               return new Pair<String, Collection<String>>("REPORT", this.taskTypeToNameMap.get("REPORT"));
            case 4:
               return new Pair<String, Collection<String>>("RISK_MODEL_MACHINE", this.taskTypeToNameMap.get("RISK_MODEL_MACHINE"));
            case 5:
               return new Pair<String, Collection<String>>("IMPORT_AXIOMA_DATA", this.taskTypeToNameMap.get("IMPORT_AXIOMA_DATA"));
            case 6:
               return new Pair<String, Collection<String>>("IMPORT_PORTFOLIO_DATA",
                        this.taskTypeToNameMap.get("IMPORT_PORTFOLIO_DATA"));
            case 7:
               return new Pair<String, Collection<String>>("IMPORT_ATTRIBUTE_DATA",
                        this.taskTypeToNameMap.get("IMPORT_ATTRIBUTE_DATA"));
            case 8:
               return new Pair<String, Collection<String>>("IMPORT_COMPOSITE_DATA",
                        this.taskTypeToNameMap.get("IMPORT_COMPOSITE_DATA"));
            case 9:
               return new Pair<String, Collection<String>>("IMPORT_CLASSIFICATION_DATA",
                        this.taskTypeToNameMap.get("IMPORT_CLASSIFIATION_DATA"));
         }

         return null;
      }
   }

   public static class TasksSectionFragment extends ListFragment {
      public static final String TASK_NAMES = "task_names";
      public static final String TASK_TYPE = "task_type";
      public static final String TASK_TYPE_DESC = "task_type_desc";

      private List<String> taskNames = Lists.newArrayList();
      private String taskType = null;
      private String taskTypeDesc = null;

      public TasksSectionFragment() {
      }

      @Override
      public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_task_names, container, false);

         this.taskType = getArguments().getString(TasksSectionFragment.TASK_TYPE);
         this.taskNames = getArguments().getStringArrayList(TasksSectionFragment.TASK_NAMES);
         this.taskTypeDesc = getArguments().getString(TasksSectionFragment.TASK_TYPE_DESC);
         //         List<Map<String, String>> taskNamesListForAdapter = Lists.newArrayList();
         //         for (String taskName : taskNames) {
         //            Map<String, String> entry = Maps.newHashMap();
         //            entry.put(TASK_NAME, taskName);
         //            taskNamesListForAdapter.add(entry);
         //         }

         //         ListView lv = (ListView) findViewById(android.R.id.list);

         ArrayAdapter<String> adapter =
                  new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,
                           taskNames.toArray(new String[taskNames.size()]));

         setListAdapter(adapter);

         return rootView;
      }

      @Override
      public void onListItemClick(ListView l, View v, int position, long id) {
         String taskName = this.taskNames.get(position);

         Intent intent = new Intent(getActivity(), ShowTaskDetailsActivity.class);
         intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE, taskType);
         intent.putExtra(ShowTasksActivity.SELECTED_TASK_NAME, taskName);
         intent.putExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC, taskTypeDesc);
         startActivity(intent);
      }
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