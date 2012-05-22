package com.batoulapps.QamarDeen.ui.fragments;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarListHelper;
import com.batoulapps.QamarDeen.ui.helpers.QamarSelectorHelper;
import com.batoulapps.QamarDeen.ui.helpers.QamarSelectorHelper.OnQamarSelectionListener;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesHeaderLayout;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesLayout;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesLayout.SalahClickListener;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class PrayerFragment extends SherlockFragment implements OnQamarSelectionListener {

   private PinnedHeaderListView mListView = null;
   private PrayerListAdapter mListAdapter = null;
   private AsyncTask<Long, Void, Cursor> mLoadingTask = null;
   private AsyncTask<Integer, Void, Boolean> mWritingTask = null;
   private QamarSelectorHelper mPopupHelper = null;
   private int mHeaderHeight = 0;
   
   public static PrayerFragment newInstance(){
      return new PrayerFragment();
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState){
      Activity activity = getActivity();
      View view = inflater.inflate(R.layout.qamar_list, container, false);
      mListView = (PinnedHeaderListView)view.findViewById(R.id.list);
      mListAdapter = new PrayerListAdapter(activity);
      
      // setup the list and adapter
      mHeaderHeight = QamarListHelper.setupQamarList(inflater, activity,
            mListView, mListAdapter, R.layout.prayer_hdr);
      
      mPopupHelper = new QamarSelectorHelper(activity);
      return view;
   }
   
   @Override
   public void onPause() {
      if (mPopupHelper != null){
         mPopupHelper.dismissPopup();
      }
      super.onPause();
   }
   
   /**
    * gets prayer data for a range of times
    * @param maxDate the max date to get prayer data for.  may be null.
    * @param minDate the min date to get prayer data for.  may be null.
    */
   private void requestPrayerData(Long maxDate, Long minDate){
      if (mLoadingTask != null){
         mLoadingTask.cancel(true);
      }
      
      Calendar calendar = QamarTime.getTodayCalendar();
      if (maxDate == null){
         // if no max date, set it to today
         maxDate = calendar.getTimeInMillis();
      }
      else { calendar.setTimeInMillis(maxDate); }
      
      // need ts of 12:00:00 on the max day in gmt
      maxDate = QamarTime.getGMTTimeFromLocal(calendar);
      
      if (minDate == null){
         // if no min date, backup 30 days
         calendar.add(Calendar.DATE, -1 * 30);
         minDate = calendar.getTimeInMillis();
      }
      else { calendar.setTimeInMillis(minDate); }
      
      // need ts 12:00:00 on the min day in gmt
      minDate = QamarTime.getGMTTimeFromLocal(calendar);
      
      // get the data from the database
      mLoadingTask = new ReadPrayerDataTask();
      mLoadingTask.execute(maxDate, minDate);
   }
   
   /**
    * AsyncTask that asynchronously gets prayer data from the database
    * and updates the cursor accordingly.
    */
   private class ReadPrayerDataTask extends AsyncTask<Long, Void, Cursor> {
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)PrayerFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.getPrayerEntries(maxDate / 1000, minDate / 1000);
      }
      
      @Override
      protected void onPostExecute(Cursor cursor){
         if (cursor != null){
            if (cursor.moveToFirst()){
               Map<Long, int[]> data = new HashMap<Long, int[]>();
               do {
                  long timestamp = cursor.getLong(1) * 1000;
                  int prayer = cursor.getInt(2);
                  int status = cursor.getInt(3);
                  
                  // time calculations
                  Calendar gmtCal = QamarTime.getGMTCalendar();
                  gmtCal.setTimeInMillis(timestamp);
                  long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);
                  
                  // get or make columns for the data
                  int[] columns = data.get(localTimestamp);
                  if (columns == null){ columns = new int[7]; }
                  
                  columns[prayer] = status;
                  data.put(localTimestamp, columns);
               }
               while (cursor.moveToNext());
               
               if (!data.isEmpty()){
                  // set the data in the adapter
                  PrayerFragment.this.mListAdapter.addDayData(data);
                  PrayerFragment.this.mListAdapter.notifyDataSetChanged();
               }
            }
            cursor.close();
         }
         mLoadingTask = null;
      }
   }
   
   private void popupSalahBox(View anchorView, int currentRow, int salah){
      mPopupHelper.showPopup(this, anchorView, currentRow, salah,
            R.array.prayer_options, R.array.prayer_values);
   }
   
   @Override
   public void onItemSelected(int row, int salah, int selection){
      long ts = -1;
      
      // get the row of the selection
      Object dateObj = mListView.getItemAtPosition(row);
      if (dateObj == null){ return; }
      
      // get the timestamp corresponding to the row
      Date date = (Date)dateObj;
      Calendar cal = QamarTime.getTodayCalendar();
      cal.setTime(date);
      ts = QamarTime.getGMTTimeFromLocal(cal);
      
      if (mWritingTask != null){
         mWritingTask.cancel(true);
      }
      mWritingTask = new WritePrayerDataTask(ts);
      mWritingTask.execute(row, salah, selection);
   }
   
   private class WritePrayerDataTask extends AsyncTask<Integer, Void, Boolean> {
      private long mTimestamp = -1;
      private int mSelectedRow = -1;
      private int mSalah = -1;
      private int mSelectionValue = -1;

      public WritePrayerDataTask(long timestamp){
         mTimestamp = timestamp;
      }
      
      @Override
      protected Boolean doInBackground(Integer... params) {
         mSelectedRow = params[0];
         mSalah = params[1];
         mSelectionValue = params[2];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)PrayerFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.writePrayerEntry(mTimestamp / 1000,
               mSalah, mSelectionValue);
      }
      
      @Override
      protected void onPostExecute(Boolean result) {
         if (result != null && result == true){
            // calculate the local timestamp
            Calendar gmtCal = QamarTime.getGMTCalendar();
            gmtCal.setTimeInMillis(mTimestamp);
            long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

            // update the list adapter with the data
            mListAdapter.addOneSalahData(localTimestamp,
                  mSalah, mSelectionValue);
            
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
   
   private class PrayerListAdapter extends QamarListAdapter {
      private boolean mIsExtendedMode = false;
      private Map<Long, int[]> dataMap = new HashMap<Long, int[]>();

      public PrayerListAdapter(Context context){
         super(context);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestPrayerData(maxDate, minDate);
      }
      
      public void addDayData(Map<Long, int[]> data){
         dataMap.putAll(data);
      }
      
      public void addOneSalahData(long when, int salah, int val){
         int[] data = dataMap.get(when);
         if (data == null){
            data = new int[7];
         }
         data[salah] = val;
         dataMap.put(when, data);
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.prayer_layout, null);
            populateDayInfoInHolder(h, convertView, R.id.prayer_hdr);
            
            h.boxes = (PrayerBoxesLayout)convertView
                  .findViewById(R.id.prayer_boxes);
            
            h.boxes.setExtendedMode(mIsExtendedMode);
            PrayerBoxesHeaderLayout headerBoxes =
                  (PrayerBoxesHeaderLayout)convertView
                  .findViewById(R.id.prayer_header_boxes);
            headerBoxes.setExtendedMode(mIsExtendedMode);
            
            holder = h;
            convertView.setTag(holder);
         }
         else { holder = (ViewHolder)convertView.getTag(); }
         
         // initialize generic row stuff (date, header, etc)
         initializeRow(holder, date, position);
         
         // set the salah data
         int[] prayerStatus = dataMap.get(date.getTime());
         if (prayerStatus != null){
            holder.boxes.setPrayerSquares(prayerStatus);
         }
         else { holder.boxes.clearPrayerSquares(); }
         
         final int currentRow = position;
         holder.boxes.setSalahClickListener(new SalahClickListener(){
            
            @Override
            public void onSalahClicked(View view, final int salah){
               // use this to determine if we have a header here or not
               int section = getSectionForPosition(currentRow);
               int firstRowForSection = getPositionForSection(section);
               
               // scroll either to 0 (if we are part of a header) or to
               // just under the header
               int scrollHeight =
                     (firstRowForSection == currentRow)? 0 : mHeaderHeight; 
               
               if (android.os.Build.VERSION.SDK_INT >= 11){
                  // honeycomb+, we get smooth scrolling
                  mListView.smoothScrollToPositionFromTop(currentRow,
                        scrollHeight);
               }
               else {
                  // works on older android versions
                  mListView.setSelectionFromTop(currentRow, scrollHeight);
               }
               
               final View v = view;
               view.postDelayed(
                     new Runnable(){
                        public void run(){ 
                           popupSalahBox(v, currentRow, salah);
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
            PrayerBoxesHeaderLayout hdr =
                  (PrayerBoxesHeaderLayout)v
                  .findViewById(R.id.prayer_header_boxes);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);
            hdr.setExtendedMode(mIsExtendedMode);
            hdr.showSalahLabels();
         }
      }
      
      class ViewHolder extends QamarViewHolder {
         PrayerBoxesLayout boxes;
      }
   }
}
