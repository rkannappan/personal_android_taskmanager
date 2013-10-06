package com.axioma.taskmanager;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.axioma.taskmanager.async.AsyncCallback;
import com.axioma.taskmanager.async.GetTaskResultsInBackground;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * @author rkannappan
 */
public class PlotResultsActivity extends Activity implements AsyncCallback {

   private String taskName = null;
   private String taskType = null;
   private String taskTypeDesc = null;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.setContentView(R.layout.activity_plot_results);

      // Get the message from the intent
      Intent intent = getIntent();
      String taskType = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE);
      String taskTypeDesc = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_TYPE_DESC);
      String taskName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_NAME);
      String taskRawName = intent.getStringExtra(ShowTasksActivity.SELECTED_TASK_RAW_NAME);

      this.taskType = taskType;
      this.taskTypeDesc = taskTypeDesc;
      this.taskName = taskName;

      System.out.println("type in intent " + taskType);
      System.out.println("name in intent " + taskName);
      System.out.println("raw name in intent " + taskRawName);
      System.out.println("type desc in intent " + taskTypeDesc);

      if (taskType.equals("RISK_ANALYSIS")) {
         List<String> statistics = Lists.newArrayList("Active Predicted Variance", "Reference Value");
         new GetTaskResultsInBackground(PlotResultsActivity.this, taskName, taskRawName, taskType, taskTypeDesc, statistics, this)
                  .execute();
      } else if (taskType.equals("PERFORMANCE_ATTRIBUTION_FACTOR")) {
         List<String> statistics = Lists.newArrayList("totalActiveReturns", "Reference Value");
         new GetTaskResultsInBackground(PlotResultsActivity.this, taskName, taskRawName, taskType, taskTypeDesc, statistics, this).execute();
      } else {
         this.showUnsupported();
      }
   }

   @Override
   public void postProcessing(String results) {
      String chartTitle = "";

      Map<Long, Double> resultsMap = Maps.newHashMap();
      if (taskType.equals("RISK_ANALYSIS")) {
         resultsMap = this.getActiveRiskMap(results);
         chartTitle = "Active Risk Time Series Plot for " + this.taskName;
      } else if (taskType.equals("PERFORMANCE_ATTRIBUTION_FACTOR")) {
         resultsMap = this.getActiveReturnsMap(results);
         chartTitle = "Active Returns Time Series Plot for " + this.taskName;
      }

      GraphViewData[] data = new GraphViewData[resultsMap.size()];
      int i = 0;
      for (Entry<Long, Double> entry : resultsMap.entrySet()) {
         data[i++] = new GraphViewData(entry.getKey(), entry.getValue());
      }

      GraphViewSeries series = new GraphViewSeries(data);

      final java.text.DateFormat dateFormatter = DateFormat.getDateFormat(this);
      GraphView graphView = new LineGraphView(this, chartTitle) {
         @Override
         protected String formatLabel(double value, boolean isValueX) {
            if (isValueX) {
               // transform number to date
               return dateFormatter.format(new Date((long) value));
            } else {
               return super.formatLabel(value, isValueX);
            }
         }
      };
      graphView.addSeries(series);
      graphView.setBackgroundColor(Color.BLACK);

      LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
      layout.addView(graphView);
   }

   private Map<Long, Double> getActiveRiskMap(final String results) {
      String[] resultArray = results.split("@");
      String activePredictedVarianceJson = resultArray[0];
      String referenceValueJson = resultArray[1];

      Map<LocalDate, Double> apvMap = this.getResultsMap(activePredictedVarianceJson);
      Map<LocalDate, Double> refValueMap = this.getResultsMap(referenceValueJson);

      Map<Long, Double> activeRiskMap = Maps.newTreeMap();
      for (Entry<LocalDate, Double> entry : apvMap.entrySet()) {
         LocalDate date = entry.getKey();   
         Double apv = entry.getValue();
         Double refValue = refValueMap.get(date);
         
         if (refValue != null) {
            Double activeRisk = Math.sqrt(apv) / refValue;
            activeRiskMap.put(date.toDate().getTime(), activeRisk);
         }
      }
      
      return activeRiskMap;
   }

   private Map<Long, Double> getActiveReturnsMap(final String results) {
      String[] resultArray = results.split("@");
      String activeReturnsJson = resultArray[0];
      String referenceValueJson = resultArray[1];

      Map<LocalDate, Double> arMap = this.getResultsMap(activeReturnsJson);
      Map<LocalDate, Double> refValueMap = this.getResultsMap(referenceValueJson);

      Map<Long, Double> activeReturnsMap = Maps.newTreeMap();
      for (Entry<LocalDate, Double> entry : arMap.entrySet()) {
         LocalDate date = entry.getKey();
         Double ar = entry.getValue();
         Double refValue = refValueMap.get(date);

         if (refValue != null) {
            Double activeReturn = ar / refValue;
            activeReturnsMap.put(date.toDate().getTime(), activeReturn);
         }
      }

      return activeReturnsMap;
   }

   private Map<LocalDate, Double> getResultsMap(final String json) {
      Map<LocalDate, Double> resultsMap = Maps.newHashMap();
      
      try {
         JSONObject obj = new JSONObject(json);
         Iterator it = obj.keys();
         while (it.hasNext()) {
            String date = (String) it.next();
            Double value = obj.getDouble(date);

            LocalDate ldate = new LocalDate(date);

            resultsMap.put(ldate, value);
         }
      } catch (JSONException ex) {
         ex.printStackTrace();
      }
      
      return resultsMap;
   }

   private void showUnsupported() {
      LinearLayout layout = (LinearLayout) findViewById(R.id.layout);
      TextView tv = new TextView(PlotResultsActivity.this);
      tv.setText("Sorry, results are not supported for task type " + this.taskTypeDesc);
      layout.addView(tv);
   }
}