package com.axioma.taskmanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.axioma.taskmanager.TaskFinderActivity;

/**
 * @author rkannappan
 */
public class PreferenceUtil {

   public static String getBaseWSURL(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

      String host = sharedPrefs.getString(TaskFinderActivity.HOST_NAME, TaskFinderActivity.DEFAULT_HOST_NAME).trim();
      String portNumber = sharedPrefs.getString(TaskFinderActivity.PORT_NUMBER, TaskFinderActivity.DEFAULT_PORT_NUMBER).trim();
      String ws_context = sharedPrefs.getString(TaskFinderActivity.WS_CONTEXT, TaskFinderActivity.DEFAULT_WS_CONTEXT).trim();

      return host + ":" + portNumber + "/" + ws_context + "/";
   }

   public static String getAppUserName(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPrefs.getString(TaskFinderActivity.APP_USER_NAME, TaskFinderActivity.DEFAULT_APP_USER_NAME).trim();
   }

   public static String getAppPassword(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPrefs.getString(TaskFinderActivity.APP_USER_PASSWORD, TaskFinderActivity.DEFAULT_APP_USER_PASSWORD).trim();
   }
}
