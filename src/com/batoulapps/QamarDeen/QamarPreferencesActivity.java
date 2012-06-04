package com.batoulapps.QamarDeen;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarConstants.PreferenceKeys;

public class QamarPreferencesActivity extends SherlockPreferenceActivity
   implements OnPreferenceChangeListener {

   private static final int MENU_DONE = 1;
   private Preference mGenderPreference = null;
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu){
      menu.add(Menu.NONE, MENU_DONE, Menu.NONE, R.string.cancel)
         .setIcon(R.drawable.ic_action_cancel)
         .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      return super.onCreateOptionsMenu(menu);
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == MENU_DONE){
         finish();
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
   
   @Override
   protected void onCreate(Bundle savedInstanceState){
      setTheme(R.style.Theme_Sherlock_Light);
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences);
      
      mGenderPreference = findPreference(PreferenceKeys.GENDER_PREF);
      if (mGenderPreference != null){
         mGenderPreference.setOnPreferenceChangeListener(this);
         updateGenderPreference();
      }
   }
   
   @Override
   protected void onDestroy() {
      if (mGenderPreference != null){
         mGenderPreference.setOnPreferenceChangeListener(null);
      }
      super.onDestroy();
   }
   
   @Override
   public boolean onPreferenceChange(Preference preference, Object newValue) {
      if (preference.getKey().equals(PreferenceKeys.GENDER_PREF)){
         updateGenderPreference(newValue);
      }
      return true;
   }
   
   private void updateGenderPreference(){
      SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(this);
      String gender = prefs.getString(
            QamarConstants.PreferenceKeys.GENDER_PREF, "");
      updateGenderPreference(gender);
   }
   
   private void updateGenderPreference(Object value){
      if (mGenderPreference != null){
         if ("female".equals(value.toString())){
            mGenderPreference.setSummary(R.string.pref_gender_female);
         }
         else {
            mGenderPreference.setSummary(R.string.pref_gender_male);
         }
      }
   }
}
