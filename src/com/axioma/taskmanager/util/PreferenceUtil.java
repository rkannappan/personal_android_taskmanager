package com.axioma.taskmanager.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author rkannappan
 */
public class PreferenceUtil {

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

   public static String getBaseWSURL(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

      String host = sharedPrefs.getString(PreferenceUtil.HOST_NAME, PreferenceUtil.DEFAULT_HOST_NAME).trim();
      String portNumber = sharedPrefs.getString(PreferenceUtil.PORT_NUMBER, PreferenceUtil.DEFAULT_PORT_NUMBER).trim();
      String ws_context = sharedPrefs.getString(PreferenceUtil.WS_CONTEXT, PreferenceUtil.DEFAULT_WS_CONTEXT).trim();

      return host + ":" + portNumber + "/" + ws_context + "/";
   }

   public static String getAppUserName(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPrefs.getString(PreferenceUtil.APP_USER_NAME, PreferenceUtil.DEFAULT_APP_USER_NAME).trim();
   }

   public static String getAppPassword(Context context) {
      SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
      return sharedPrefs.getString(PreferenceUtil.APP_USER_PASSWORD, PreferenceUtil.DEFAULT_APP_USER_PASSWORD).trim();
   }
}