package com.batoulapps.QamarDeen;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.data.QuranData;
import com.batoulapps.QamarDeen.ui.fragments.QuranFragment;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class SuraSelectorActivity extends SherlockActivity {

   private ListView mListView = null;
   private SuraAdapter mListAdapter = null;
   private long mCurrentTime = 0;
   private ActionMode mMode = null;
   
   public static final int MENU_CANCEL = 1;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      setTheme(R.style.Theme_Sherlock_Light);
      super.onCreate(savedInstanceState);
      
      // initialize the view
      setContentView(R.layout.sura_list);
      mListView = (ListView)findViewById(R.id.list);
      mListAdapter = new SuraAdapter(this);
      
      Intent currentIntent = getIntent();
      if (currentIntent == null){ finish(); }
      mCurrentTime = currentIntent.getLongExtra(QuranFragment.EXTRA_DATE, 0);
      if (mCurrentTime == 0){ finish(); }
      
      String mSelectedSuras = currentIntent
            .getStringExtra(QuranFragment.EXTRA_READ);
      if (!TextUtils.isEmpty(mSelectedSuras)){
         String[] suras = mSelectedSuras.split(",");
         for (String suraStr : suras){
            try {
               int sura = Integer.parseInt(suraStr);
               mListAdapter.selectSura(sura);
            }
            catch (Exception e){}
         }
      }
    
      // set the adapter
      mListView.setAdapter(mListAdapter);
      mListView.setOnItemClickListener(mOnItemClickListener);
      
      // set the title
      mMode = startActionMode(new QuranSelectorActionMode());
      updateCount(mListAdapter.getSelectedCount());
   }
   
   private final class QuranSelectorActionMode implements ActionMode.Callback {
      @Override
      public boolean onCreateActionMode(ActionMode mode, Menu menu){
         menu.add(Menu.NONE, MENU_CANCEL, Menu.NONE, R.string.cancel)
            .setIcon(R.drawable.ic_action_cancel)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
         return true;
      }
      
      @Override
      public boolean onPrepareActionMode(ActionMode mode, Menu menu){
         return false;
      }
      
      @Override
      public boolean onActionItemClicked(ActionMode mode, MenuItem item){
         if (item.getItemId() == MENU_CANCEL){
            cancelSuraSelection();
         }
         return true;
      }
      
      @Override
      public void onDestroyActionMode(ActionMode mode){
         saveSuraData();
      }
   }
   
   OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
         int drawable = R.drawable.quran_picker_edit;
         if (mListAdapter.isSuraSelected(position + 1)){
            mListAdapter.unselectSura(position + 1);
         }
         else {
            mListAdapter.selectSura(position + 1);
            drawable = R.drawable.quran_picker_edit_on;
         }
         ((TextView)view).setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
      }
      
   };
   
   private class WriteExtraDataTask extends AsyncTask<Object, Void, Boolean> {
      
      @Override
      protected Boolean doInBackground(Object... params) {
         Date localDate = new Date(mCurrentTime);
         long entryTime = QamarTime.getGMTTimeFromLocalDate(localDate) / 1000;
         
         List<QuranData> suras = new ArrayList<QuranData>();
         for (Object item : params){
            try {
               Integer sura = Integer.parseInt(item.toString());
               if (sura != null){
                  int endAyah = QamarConstants.SURA_NUM_AYAHS[sura-1];
                  QuranData qd = new QuranData(1, sura, endAyah, sura);
                  suras.add(qd);
               }
            }
            catch (Exception e){}
         }
         
         QamarDbAdapter databaseAdapter =
               new QamarDbAdapter(SuraSelectorActivity.this);
         boolean result =
               databaseAdapter.writeExtraQuranEntries(entryTime, suras);
         databaseAdapter.close();
         return result;
      }
      
      @Override
      protected void onPostExecute(Boolean result) {
      }
   }
   
   public void saveSuraData(){
      Object[] suras = mListAdapter.getSelectedSuras();
      AsyncTask<Object, Void, Boolean> writingTask = 
            new WriteExtraDataTask();
      writingTask.execute(suras);
      finish();
   }
   
   public void cancelSuraSelection(){
      finish();
   }
   
   public void updateCount(int count){
      String countString = getResources()
            .getQuantityString(R.plurals.surasSelected, count, count);
      mMode.setTitle(countString);
   }
   
   private class SuraAdapter extends BaseAdapter {
      protected LayoutInflater mInflater = null;
      private String[] mSuras = null;
      private Map<Integer, Boolean> mSelectedSuras =
            new HashMap<Integer, Boolean>();

      public SuraAdapter(Context context){
         mInflater = LayoutInflater.from(context);
         mSuras = context.getResources().getStringArray(R.array.sura_names);
      }

      @Override
      public int getCount() { return mSuras.length; }

      @Override
      public Object getItem(int position) { return mSuras[position]; }
      
      public boolean isSuraSelected(int sura){
         return mSelectedSuras.containsKey(sura);
      }
      
      public void selectSura(int sura){
         mSelectedSuras.put(sura, true);
         updateCount(mSelectedSuras.size());
      }
      
      public void unselectSura(int sura){
         if (mSelectedSuras.containsKey(sura)){
            mSelectedSuras.remove(sura);
         }
         updateCount(mSelectedSuras.size());
      }
      
      public Object[] getSelectedSuras(){
         return mSelectedSuras.keySet().toArray();
      }
      
      public int getSelectedCount(){
         return mSelectedSuras.size();
      }
      
      @Override
      public View getView(final int position,
            View convertView, ViewGroup parent) {
         if (convertView == null){
            convertView = mInflater.inflate(R.layout.sura_row, null);
         }
         
         int img = R.drawable.quran_picker_edit;
         if (isSuraSelected(position + 1)){
            img = R.drawable.quran_picker_edit_on;
         }
         
         TextView tv = (TextView)convertView;
         tv.setText(getItem(position).toString());
         tv.setCompoundDrawablesWithIntrinsicBounds(img, 0, 0, 0);
         return convertView;
      }
   
      @Override
      public long getItemId(int position) { return position; }

   }
}
