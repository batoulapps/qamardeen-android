package com.batoulapps.QamarDeen.ui.fragments;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarFragment;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.utils.HijriUtils;
import com.batoulapps.QamarDeen.utils.HijriUtils.HijriDate;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class FastingFragment extends QamarFragment {

   public static int[] FASTING_HIGHLIGHT_IMAGES = new int[]{
      R.drawable.fasting_row_fareedah, R.drawable.fasting_row_tatawou,
      R.drawable.fasting_row_qadaa, R.drawable.fasting_row_kaffarah,
      R.drawable.fasting_row_nazr
   };
   
   public static int[] FASTING_POPUP_IMAGES = new int[]{
      R.drawable.fasting_hud_fareedah, R.drawable.fasting_hud_sunnah,
      R.drawable.fasting_hud_qadaa, R.drawable.fasting_hud_kaffarah,
      R.drawable.fasting_hud_nazr, R.drawable.fasting_hud_not
   };
   
   private AsyncTask<Integer, Void, Boolean> mWritingTask = null;

   public static FastingFragment newInstance(){
      return new FastingFragment();
   }
   
   @Override
   protected QamarListAdapter createAdapter(Context context) {
      return new FastingListAdapter(context);
   }

   @Override
   protected int getHeaderLayout() {
      return R.layout.fasting_hdr;
   }

   @Override
   protected AsyncTask<Long, Void, Cursor> getDataReadingTask() {
      return new ReadFastingDataTask();
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState){
      View view = super.onCreateView(inflater, container, savedInstanceState);
      mListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, final View view,
               final int position, long id) {
            mListAdapter.scrollListToPosition(
                  mListView, position, mHeaderHeight);
           view.postDelayed(
                  new Runnable(){
                     public void run(){ 
                        popupFastingBox(view, position);
                     } 
                  }, 50);
         }
      });
      
      return view;
   }
   
   private class ReadFastingDataTask extends AsyncTask<Long, Void, Cursor> {
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)FastingFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.getFastingEntries(maxDate / 1000, minDate / 1000);
      }
      
      @Override
      protected void onPostExecute(Cursor cursor){
         if (cursor != null){
            if (cursor.moveToFirst()){
               Map<Long, Integer> data = new HashMap<Long, Integer>();
               do {
                  long timestamp = cursor.getLong(1) * 1000;
                  int sadaqahType = cursor.getInt(2);
                  
                  // time calculations
                  Calendar gmtCal = QamarTime.getGMTCalendar();
                  gmtCal.setTimeInMillis(timestamp);
                  long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);
                  
                  data.put(localTimestamp, sadaqahType);
               }
               while (cursor.moveToNext());
               
               if (!data.isEmpty()){
                  // set the data in the adapter
                  ((FastingListAdapter)mListAdapter).addDayData(data);
                  mListAdapter.notifyDataSetChanged();
               }
            }
            cursor.close();
            mReadData = true;
         }
         else { mReadData = false; }
         mLoadingTask = null;
      }
   }
   
   @Override
   public void onItemSelected(int row, int itemId, int selection){
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
      mWritingTask = new WriteFastingDataTask(ts);
      mWritingTask.execute(row, selection);
   }
   
   private class WriteFastingDataTask extends AsyncTask<Integer, Void, Boolean>{
      private long mTimestamp = -1;
      private int mSelectedRow = -1;
      private int mSelectionValue = -1;

      public WriteFastingDataTask(long timestamp){
         mTimestamp = timestamp;
      }
      
      @Override
      protected Boolean doInBackground(Integer... params) {
         mSelectedRow = params[0];
         mSelectionValue = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)FastingFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.writeFastingEntry(mTimestamp / 1000, mSelectionValue);
      }
      
      @Override
      protected void onPostExecute(Boolean result) {
         if (result != null && result == true){
            // calculate the local timestamp
            Calendar gmtCal = QamarTime.getGMTCalendar();
            gmtCal.setTimeInMillis(mTimestamp);
            long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

            // update the list adapter with the data
            ((FastingListAdapter)mListAdapter)
               .addOneFastData(localTimestamp, mSelectionValue);
            
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
   
   private void popupFastingBox(View anchorView, int currentRow){
      Integer sel = ((FastingListAdapter)mListAdapter).getDayData(currentRow);
      mPopupHelper.showPopup(this, anchorView, currentRow,
            currentRow, sel, R.array.fasting_options, R.array.fasting_values,
            FASTING_POPUP_IMAGES);
   }
   
   private class FastingListAdapter extends QamarListAdapter {
      private String[] mFastingTypes = null;
      private String[] mIslamicMonths = null;
      private Map<Long, Integer> mDataMap = new HashMap<Long, Integer>();
      
      public FastingListAdapter(Context context){
         super(context);
         mFastingTypes = getResources()
               .getStringArray(R.array.fasting_options);
         mIslamicMonths = getResources()
               .getStringArray(R.array.islamic_months);
      }
      
      public void addDayData(Map<Long, Integer> data){
         mDataMap.putAll(data);
      }
      
      public void addOneFastData(long localTimestamp, int value){
         mDataMap.put(localTimestamp, value);
      }
      
      public Integer getDayData(int position){
         Date d = (Date)getItem(position);
         return mDataMap.get(d.getTime());
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestRangeData(maxDate, minDate);
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.fasting_layout, null);
            populateDayInfoInHolder(h, convertView, R.id.fasting_hdr);
            h.hijriDay = 
                  (TextView)convertView.findViewById(R.id.fasting_hijri_day);
            h.hijriMonth =
                  (TextView)convertView.findViewById(R.id.fasting_hijri_month);
            h.fastingType =
                  (TextView)convertView.findViewById(R.id.fasting_hijri_type);
            h.fastingArea = convertView.findViewById(R.id.fasting_area);
            
            holder = h;
            convertView.setTag(holder);
         }
         else { holder = (ViewHolder)convertView.getTag(); }
         
         initializeRow(holder, date, position);
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         HijriDate hijriDate = HijriUtils.getHijriDate(cal);
         
         String dayString = ((hijriDate.day < 10)? "0" : "") + hijriDate.day;         
         holder.hijriDay.setText(dayString);
         holder.hijriMonth.setText(mIslamicMonths[hijriDate.month - 1]);
         
         Integer val = mDataMap.get(date.getTime());
         if (val != null && val > 0){
            int drawableId = FASTING_HIGHLIGHT_IMAGES[val - 1];
            holder.fastingArea.setBackgroundResource(drawableId);
            holder.fastingArea.setPadding(0, 0, 0, 0);
            holder.fastingType.setText(mFastingTypes[val - 1]);
         }
         else {
            holder.fastingArea.setBackgroundDrawable(null);
            holder.fastingType.setText("");
         }
         
         return convertView;
      }
      
      @Override
      public void configurePinnedHeader(View v, int position, int alpha) {
         super.configurePinnedHeader(v, position, alpha);
         if (alpha == 255){
            v.setBackgroundResource(R.color.pinned_hdr_background);
         }
      }
      
      class ViewHolder extends QamarViewHolder {
         TextView hijriDay;
         TextView hijriMonth;
         TextView fastingType;
         View fastingArea;
      }
   }

}
