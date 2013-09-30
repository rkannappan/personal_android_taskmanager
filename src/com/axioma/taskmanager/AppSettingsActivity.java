package com.axioma.taskmanager;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author rkannappan
 */
public class AppSettingsActivity extends Activity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // Display the fragment as the main content.
      getFragmentManager().beginTransaction().replace(android.R.id.content, new AppSettingsFragment()).commit();
   }

   public static class AppSettingsFragment extends PreferenceFragment {

      @Override
      public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

         // Load the preferences from an XML resource
         addPreferencesFromResource(R.xml.preferences);
      }
   }
}