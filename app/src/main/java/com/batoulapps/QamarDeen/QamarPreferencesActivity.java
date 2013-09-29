package com.batoulapps.QamarDeen;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.*;
import android.os.Process;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarConstants.PreferenceKeys;
import com.batoulapps.QamarDeen.data.QamarDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

public class QamarPreferencesActivity extends SherlockPreferenceActivity
   implements OnPreferenceChangeListener {
   public static final String BACKUP_NAME = "qamardeen.db";

   private static final int MENU_DONE = 1;
   private Preference mGenderPreference = null;
   private Preference mRestorePreference = null;
   private CheckBoxPreference mArabicPreference = null;
   private boolean mUsingArabic = false;
   private boolean mArabicChanged = false;
   private AlertDialog mDialog;
   
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
         leavePreferences();
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

      PreferenceScreen about = (PreferenceScreen)findPreference("about");
      if (about != null){
         String title = getString(R.string.qamar_about_title);
         try {
            PackageInfo info = getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            title = String.format(title, info.versionName);
            about.setTitle(title);
         }
         catch (Exception e){
         }
      }

      // backup and restore
      Preference backup = findPreference("backup");
      if (backup != null){
        backup.setOnPreferenceClickListener(mOnPreferenceClickListener);
      }

      boolean enabled = false;
      mRestorePreference = findPreference("restore");
      if (mRestorePreference != null){
        try {
          File restoreFile = getBackupFile();
          enabled = restoreFile.exists();
        }
        catch (Exception e){
        }
        mRestorePreference.setEnabled(enabled);
        mRestorePreference.setOnPreferenceClickListener(mOnPreferenceClickListener);
      }

      // arabic preferences
      mArabicPreference = (CheckBoxPreference)findPreference(
              PreferenceKeys.USE_ARABIC);
      if (mArabicPreference != null){
         if ("ar".equals(Locale.getDefault().getLanguage())){
            mArabicPreference.setEnabled(false);
         }
         else { mArabicPreference.setOnPreferenceChangeListener(this); }
         mUsingArabic = mArabicPreference.isChecked();
      }
   }

   @Override
   protected void onDestroy() {
      if (mGenderPreference != null){
         mGenderPreference.setOnPreferenceChangeListener(null);
      }

      if (mArabicPreference != null){
         mArabicPreference.setOnPreferenceChangeListener(null);
      }

      if (mDialog != null){
        mDialog.dismiss();
        mDialog = null;
      }
      super.onDestroy();
   }

   @Override
   public void onBackPressed() {
      if (mArabicChanged){
         leavePreferences();
         return;
      }
      super.onBackPressed();
   }

   private void leavePreferences(){
      if (mArabicChanged){
         Intent i = new Intent(this, QamarDeenActivity.class);
         i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         startActivity(i);
      }
      finish();
   }
   
   @Override
   public boolean onPreferenceChange(Preference preference, Object newValue) {
      if (preference.getKey().equals(PreferenceKeys.GENDER_PREF)){
         updateGenderPreference(newValue);
      }
      else if (preference.getKey().equals(PreferenceKeys.USE_ARABIC)){
         if (newValue != null && newValue instanceof Boolean){
            Boolean value = (Boolean)newValue;
            if (value != mUsingArabic){
               mArabicChanged = true;
            }
            else { mArabicChanged = false; }
         }
      }
      return true;
   }

   private Preference.OnPreferenceClickListener mOnPreferenceClickListener =
       new Preference.OnPreferenceClickListener() {
         @Override
         public boolean onPreferenceClick(Preference preference) {
           if ("backup".equals(preference.getKey())){
              File backupFile = getBackupFile();
              if (!backupFile.exists()){
                backupDatabase();
              }
              else {
                mDialog = makeBackupDialog();
                mDialog.show();
              }
           }
           else if ("restore".equals(preference.getKey())){
              mDialog = makeRestoreDialog();
              mDialog.show();
           }
           return false;
         }
       };

   private AlertDialog makeBackupDialog(){
     AlertDialog.Builder builder = getBuilder(R.string.overwrite_current_db,
         R.string.overwrite_current_backup_msg);
     builder.setPositiveButton(R.string.yes,
         new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
             dialog.dismiss();
             mDialog = null;
             backupDatabase();
           }
         });
     return builder.create();
   }

   private AlertDialog makeRestoreDialog(){
     AlertDialog.Builder builder = getBuilder(R.string.overwrite_current_db,
         R.string.overwrite_current_db_msg);
     builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
       @Override
       public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
          mDialog = null;
          restoreDatabase();
       }
     });
     return builder.create();
   }

  private AlertDialog.Builder getBuilder(int title, int message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title)
        .setMessage(message)
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            mDialog = null;
          }
        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mDialog = null;
          }
        });
    return builder;
  }

  private void backupDatabase(){
     try {
       File destinationFile = getBackupFile();
       File sourceFile = getDatabasePath(QamarDbHelper.DATABASE_NAME);
       if (!sourceFile.exists()){
         // error
         Toast.makeText(this, R.string.error_backing_up_database,
             Toast.LENGTH_LONG).show();
         return;
       }

       if (destinationFile.exists()){
         destinationFile.delete();
       }

       new BackupAsyncTask().execute(sourceFile, destinationFile);
     } catch (Exception e){
       Toast.makeText(this, R.string.error_backing_up_database,
           Toast.LENGTH_LONG).show();
     }
   }

   public void restoreDatabase(){
     try {
       File backupFile = getBackupFile();
       File destinationFile = getDatabasePath(QamarDbHelper.DATABASE_NAME);
       if (!backupFile.exists()){
         // error
         Toast.makeText(this, R.string.error_restoring_database,
             Toast.LENGTH_LONG).show();
         return;
       }

       if (destinationFile.exists()){
         destinationFile.delete();
       }

       new RestoreAsyncTask().execute(backupFile, destinationFile);
     }
     catch (Exception e){
       Toast.makeText(this, R.string.error_restoring_database,
           Toast.LENGTH_LONG).show();
     }
   }

   private class CopyFileTask extends AsyncTask<File, Void, Boolean> {

     @Override
     protected void onPreExecute() {
       ProgressDialog dialog = new ProgressDialog(
           QamarPreferencesActivity.this);
       dialog.setMessage(getString(R.string.please_wait));
       dialog.setCancelable(false);
       dialog.show();
       mDialog = dialog;
     }

     @Override
     protected Boolean doInBackground(File... params) {
       try {
         copyFile(params[0], params[1]);
         return true;
       }
       catch (Exception e){
         return false;
       }
     }

     @Override
     protected void onPostExecute(Boolean aBoolean) {
       if (mDialog != null){
        mDialog.dismiss();
        mDialog = null;
       }
     }
   }

   private class BackupAsyncTask extends CopyFileTask {
     @Override
     protected void onPostExecute(Boolean aBoolean) {
       super.onPostExecute(aBoolean);
       Toast.makeText(QamarPreferencesActivity.this,
           R.string.database_backed_up, Toast.LENGTH_LONG).show();
         mRestorePreference.setEnabled(true);
     }
   }

  private class RestoreAsyncTask extends CopyFileTask {
    @Override
    protected void onPostExecute(Boolean aBoolean) {
      super.onPostExecute(aBoolean);
      Toast.makeText(QamarPreferencesActivity.this,
          R.string.restore_successful,
          Toast.LENGTH_LONG).show();
      android.os.Process.killProcess(Process.myPid());
    }
  }

   public static void copyFile(File src, File dst) throws IOException {
     FileChannel inChannel = new FileInputStream(src).getChannel();
     FileChannel outChannel = new FileOutputStream(dst).getChannel();
     try {
       inChannel.transferTo(0, inChannel.size(), outChannel);
     }
     finally {
       if (inChannel != null)
         inChannel.close();
       if (outChannel != null)
         outChannel.close();
     }
   }

   private File getBackupFile(){
     File externalStorage = Environment.getExternalStorageDirectory();
     return new File(externalStorage, BACKUP_NAME);
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
