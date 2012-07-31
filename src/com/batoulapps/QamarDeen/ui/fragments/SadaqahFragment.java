package com.batoulapps.QamarDeen.ui.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarFragment;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.ui.widgets.SadaqahWidget;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class SadaqahFragment extends QamarFragment {

   public static int[] SADAQAH_SELECTOR_IMAGES_OFF = new int[]{
      R.drawable.sadaqah_money_t, R.drawable.sadaqah_effort_t,
      R.drawable.sadaqah_food_t, R.drawable.sadaqah_cloth_t,
      R.drawable.sadaqah_smile_t, R.drawable.sadaqah_other_t
   };
   
   private AsyncTask<Object, Void, Boolean> mWritingTask = null;
   
   public static SadaqahFragment newInstance(){
      return new SadaqahFragment();
   }
   
   @Override
   public int getHeaderLayout(){
      return R.layout.sadaqah_hdr;
   }
   
   @Override
   protected QamarListAdapter createAdapter(Context context){
      return new SadaqahListAdapter(context);
   }
   
   @Override
   protected AsyncTask<Long, Void, Cursor> getDataReadingTask(){
      return new ReadSadaqahDataTask();
   }
   
   /**
    * AsyncTask that asynchronously gets prayer data from the database
    * and updates the cursor accordingly.
    */
   private class ReadSadaqahDataTask extends AsyncTask<Long, Void, Cursor> {
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)SadaqahFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.getSadaqahEntries(maxDate / 1000, minDate / 1000);
      }
      
      @Override
      protected void onPostExecute(Cursor cursor){
         if (cursor != null){
            if (cursor.moveToFirst()){
               Map<Long, List<Integer>> data = new HashMap<Long, List<Integer>>();
               do {
                  long timestamp = cursor.getLong(1) * 1000;
                  int sadaqahType = cursor.getInt(2);
                  
                  // time calculations
                  Calendar gmtCal = QamarTime.getGMTCalendar();
                  gmtCal.setTimeInMillis(timestamp);
                  long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);
                  
                  // get or make columns for the data
                  List<Integer> elems = data.get(localTimestamp);
                  if (elems == null){ elems = new ArrayList<Integer>(); }
                  
                  elems.add(sadaqahType);
                  data.put(localTimestamp, elems);
               }
               while (cursor.moveToNext());
               
               if (!data.isEmpty()){
                  // set the data in the adapter
                  ((SadaqahListAdapter)mListAdapter).addDayData(data);
               }
            }
            mListAdapter.notifyDataSetChanged();
            cursor.close();
            mReadData = true;
         }
         else { mReadData = false; }
         mLoadingTask = null;
      }
   }
   
   private void popupSadaqahBox(View anchorView, int currentRow){
      List<Integer> sel =
            ((SadaqahListAdapter)mListAdapter).getDataItem(currentRow);
      mPopupHelper.showMultipleChoicePopup(this, anchorView, currentRow,
            sel, R.array.charity_options, R.array.charity_values,
            SADAQAH_SELECTOR_IMAGES_OFF,
            SadaqahWidget.SADAQAH_SELECTOR_IMAGES);
   }
   
   @Override
   public void onMultipleItemsSelected(int row, List<Integer> selection){
      long ts = -1;
      
      // get the row of the selection
      Object dateObj = mListView.getItemAtPosition(row);
      if (dateObj == null){ return; }
      
      // get the timestamp corresponding to the row
      Date date = (Date)dateObj;
      ts = QamarTime.getGMTTimeFromLocalDate(date);
      
      if (mWritingTask != null){
         mWritingTask.cancel(true);
      }
      
      Collections.sort(selection);
      mWritingTask = new WriteSadaqahDataTask(row, ts);
      mWritingTask.execute(selection.toArray());
   }
   
   private class WriteSadaqahDataTask extends AsyncTask<Object, Void, Boolean> {
      private long mTimestamp = -1;
      private int mSelectedRow = -1;
      private List<Integer> mSadaqat = null;
      
      public WriteSadaqahDataTask(int selectedRow, long timestamp){
         mTimestamp = timestamp;
         mSelectedRow = selectedRow;
      }
      
      @Override
      protected Boolean doInBackground(Object... params) {
         mSadaqat = new ArrayList<Integer>();
         for (int i = 0; i<params.length; i++){
            if (params[i] instanceof Integer){
               mSadaqat.add((Integer)params[i]);
            }
         }
         
         QamarDeenActivity activity =
               (QamarDeenActivity)SadaqahFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.writeSadaqahEntries(mTimestamp / 1000, mSadaqat);
      }
      
      @Override
      protected void onPostExecute(Boolean result) {
         if (result != null && result == true){
            // calculate the local timestamp
            Calendar gmtCal = QamarTime.getGMTCalendar();
            gmtCal.setTimeInMillis(mTimestamp);
            long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

            // update the list adapter with the data
            ((SadaqahListAdapter)mListAdapter)
               .addOneSadaqahData(localTimestamp, mSadaqat);
            
            boolean refreshed = false;
            
            // attempt to refresh just this one list item
            int start = mListView.getFirstVisiblePosition();
            int end = mListView.getLastVisiblePosition();
            if (mSelectedRow >= start && mSelectedRow <= end){
               View view = mListView.getChildAt(mSelectedRow - start);
               if (view != null){
                  mListAdapter.getView(mSelectedRow, view, mListView);
                  refreshed = true;
               }
            }
            
            if (!refreshed){
               // if we can't, refresh everything
               mListAdapter.notifyDataSetChanged();
            }
         }
         mWritingTask = null;
      }
   }
   
   private class SadaqahListAdapter extends QamarListAdapter {
      private Map<Long, List<Integer>> mDataMap =
            new HashMap<Long, List<Integer>>();

      public SadaqahListAdapter(Context context){
         super(context);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestRangeData(maxDate, minDate);
      }
      
      public void addDayData(Map<Long, List<Integer>> data){
         mDataMap.putAll(data);
      }
      
      public void addOneSadaqahData(long when, List<Integer> data){
         mDataMap.put(when, data);
      }
      
      public List<Integer> getDataItem(int position){
         Date date = (Date)getItem(position);
         if (date != null){
            return mDataMap.get(date.getTime());
         }
         return null;
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.sadaqah_layout, null);
            populateDayInfoInHolder(h, convertView, R.id.sadaqah_hdr);
            h.sadaqahWidget = 
                  (SadaqahWidget)convertView.findViewById(R.id.sadaqah_widget);

            holder = h;
            convertView.setTag(holder);
         }
         else { holder = (ViewHolder)convertView.getTag(); }
         
         // initialize generic row stuff (date, header, etc)
         initializeRow(holder, date, position);
         holder.sadaqahWidget.setSadaqat(mDataMap.get(date.getTime()));
         
         final int currentRow = position;
         holder.sadaqahWidget.setOnClickListener(new OnClickListener(){
            
            @Override
            public void onClick(View view){
               scrollListToPosition(mListView, currentRow, mHeaderHeight);
               
               final View v = view;
               view.postDelayed(
                     new Runnable(){
                        public void run(){ 
                           popupSadaqahBox(v, currentRow);
                        } 
                     }, 50);
            }
         });
         
         return convertView;
      }
      
      @Override
      public void configurePinnedHeader(View v, int position, int alpha) {
         super.configurePinnedHeader(v, position, alpha);
         if (alpha == 255){            
            TextView hdr = (TextView)v.findViewById(R.id.sadaqah_header_txt);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);
         }
      }
      
      class ViewHolder extends QamarViewHolder {
         SadaqahWidget sadaqahWidget;
      }
   }
}
