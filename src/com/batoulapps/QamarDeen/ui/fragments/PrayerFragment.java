package com.batoulapps.QamarDeen.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView.PinnedHeaderAdapter;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesHeaderLayout;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesLayout;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesLayout.SalahClickListener;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class PrayerFragment extends SherlockFragment {

   private PinnedHeaderListView mListView = null;
   private PrayerListAdapter mListAdapter = null;
   private AsyncTask<Long, Void, Cursor> loadingTask = null;
   private int mHeaderHeight = 0;
   
   public static PrayerFragment newInstance(){
      return new PrayerFragment();
   }
   
   @Override
   public void onCreate(Bundle savedInstanceState){
      super.onCreate(savedInstanceState);
      mHeaderHeight = getActivity().getResources()
            .getDimensionPixelSize(R.dimen.header_height);
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState){
      View view = inflater.inflate(R.layout.qamar_list, container, false);
      mListView = (PinnedHeaderListView)view.findViewById(R.id.list);
      mListAdapter = new PrayerListAdapter(getActivity());
      mListView.setAdapter(mListAdapter);
      
      mListView.setPinnedHeaderView(
            LayoutInflater.from(getActivity())
            .inflate(R.layout.prayer_hdr, mListView, false));
      mListView.setOnScrollListener(mListAdapter);
      mListView.setDividerHeight(0);
      return view;
   }
   
   /**
    * gets prayer data for a range of times
    * @param maxDate the max date to get prayer data for.  may be null.
    * @param minDate the min date to get prayer data for.  may be null.
    */
   private void requestPrayerData(Long maxDate, Long minDate){
      if (loadingTask != null){
         loadingTask.cancel(true);
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
      loadingTask = new DataTask();
      loadingTask.execute(maxDate, minDate);
   }
   
   /**
    * AsyncTask that asynchronously gets prayer data from the database
    * and updates the cursor accordingly.
    */
   private class DataTask extends AsyncTask<Long, Void, Cursor> {
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)PrayerFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.getPrayerEntries(maxDate / 1000, minDate / 1000);
         
         /*
         // testing code
         MatrixCursor mc = new MatrixCursor(new String[]{ "_id", "ts", "salah", "status" });
         mc.newRow().add(1).add(1336694400).add(4).add(1);
         mc.newRow().add(2).add(1336435200).add(3).add(2);
         return mc;
         */
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
         loadingTask = null;
      }
   }
   
   private class PrayerListAdapter extends BaseAdapter implements 
         OnScrollListener, PinnedHeaderAdapter {
      private List<Date> mDays;
      private LayoutInflater mInflater;
      private boolean mIsExtendedMode = false;
      private Map<Long, int[]> dataMap = new HashMap<Long, int[]>();

      public PrayerListAdapter(Context context){
         mInflater = LayoutInflater.from(context);
         mDays = new ArrayList<Date>();
         addDays(30);
      }
      
      /**
       * extend the list to add n more days.  puts in a request
       * for those days to the database.
       * @param daysToAdd number of days to add
       */
      public void addDays(int daysToAdd){
         // end calendar is the new minimum date based on what
         // data we already have and how many days we want to add
         Calendar endCalendar = QamarTime.getTodayCalendar();
         endCalendar.add(Calendar.DATE, -1 * (mDays.size() + daysToAdd));
         long minDate = endCalendar.getTimeInMillis();
         
         // calculations is today minus the number of days we already
         // have (the current minimum date that is populated).
         Calendar calculations = QamarTime.getTodayCalendar();
         calculations.add(Calendar.DATE, -1 * mDays.size());
         long maxDate = calculations.getTimeInMillis();

         while (calculations.compareTo(endCalendar) >= 0){
            mDays.add(calculations.getTime());
            calculations.add(Calendar.DATE, -1);
         }
         
         // request the data from the database
         requestPrayerData(maxDate, minDate);
      }
      
      public void addDayData(Map<Long, int[]> data){
         dataMap.putAll(data);
      }
      
      public int getCount(){
         return mDays.size();
      }
      
      public Object getItem(int position){
         return mDays.get(position);
      }
      
      public long getItemId(int position){
         return ((Date)mDays.get(position)).getTime();
      }
      
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.prayer_layout, null);
            h.headerView = (View)convertView.findViewById(R.id.prayer_hdr);
            h.dateAreaView = (View)convertView
                  .findViewById(R.id.section_date_index);
            h.dividerView = (View)convertView.findViewById(R.id.list_divider);
            h.dayOfWeek = (TextView)convertView.findViewById(R.id.day_of_week);
            h.dayNumber = (TextView)convertView.findViewById(R.id.day_number);
            h.headerMonth = (TextView)convertView
                  .findViewById(R.id.section_month_index);
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
         
         // handling for the color of the day area based on whether
         // the day represents today or not
         Resources res = getActivity().getResources();
         if (DateUtils.isToday(date.getTime())){
            holder.dateAreaView.setBackgroundResource(R.color.today_bg_color);
            holder.dayOfWeek.setTextColor(
                  res.getColor(R.color.today_weekday_color));
            holder.dayNumber.setTextColor(
                  res.getColor(R.color.today_day_color));
         }
         else {
            holder.dateAreaView.setBackgroundResource(
                  R.color.normal_day_bg_color);
            holder.dayOfWeek.setTextColor(
                  res.getColor(R.color.normal_weekday_color));
            holder.dayNumber.setTextColor(
                  res.getColor(R.color.normal_day_color));
         }
         
         holder.dayOfWeek.setText(new SimpleDateFormat("EEE").format(date));
         holder.dayNumber.setText(new SimpleDateFormat("dd").format(date));
         
         final int section = getSectionForPosition(position);
         if (getPositionForSection(section) == position) {
            // show header
            holder.headerMonth.setText(
                  new SimpleDateFormat("LLL").format(date));
            holder.headerView.setVisibility(View.VISIBLE);
            holder.dividerView.setVisibility(View.GONE);
         }
         else {
            // hide header, show divider
            holder.headerView.setVisibility(View.GONE);
            holder.dividerView.setVisibility(View.VISIBLE);
         }
         
         // move the divider for the last item in a section
         if (getPositionForSection(section + 1) - 1 == position) {
            holder.dividerView.setVisibility(View.GONE);
         }
         else {
            holder.dividerView.setVisibility(View.VISIBLE);
         }
         
         // set the salah data
         int[] prayerStatus = dataMap.get(date.getTime());
         if (prayerStatus != null){
            holder.boxes.setPrayerSquares(prayerStatus);
         }
         else { holder.boxes.clearPrayerSquares(); }
         
         final int currentRow = position;
         holder.boxes.setSalahClickListener(new SalahClickListener(){
            
            @Override
            public void onSalahClicked(int salah){
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
            }
         });
   
         return convertView;
      }
      
      /**
       * gets the section for a particular position in the list
       * @param position the position of the list item
       * @return the section number (which is just the month number)
       */
      public int getSectionForPosition(int position){
         Date info = (Date)getItem(position);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(info);
         return calendar.get(Calendar.MONTH) + 1;
      }
      
      /**
       * gets the position of the first item for a particular section
       * @param section the section number (currently a month)
       * @return the first position where an item that has that section
       * occurs.  this is used for figuring out whether to show the header
       */
      public int getPositionForSection(int section){
         int max = mDays.size();
         Calendar calendar = Calendar.getInstance();

         for (int i=0; i<max; i++){
            Date di = mDays.get(i);
            calendar.setTime(di);
            if (calendar.get(Calendar.MONTH) + 1 == section){ return i; }
         }
         return -1;
      }
      
      @Override
      public int getPinnedHeaderState(int position) {
         if (getCount() == 0) {
            return PINNED_HEADER_GONE;
         }

         if (position < 0) {
            return PINNED_HEADER_GONE;
         }

         // The header should get pushed up if the top item shown
         // is the last item in a section for a particular letter.
         int section = getSectionForPosition(position);
         int nextSectionPosition = getPositionForSection(section + 1);

         if (nextSectionPosition != -1 &&
               position == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
         }

         return PINNED_HEADER_VISIBLE;
      }
      
      @Override
      public void configurePinnedHeader(View v, int position, int alpha) {
         if (alpha == 255){
            TextView monthArea =
                  (TextView)v.findViewById(R.id.section_month_index);
            Date date = (Date)getItem(position);
            monthArea.setText(new SimpleDateFormat("LLL").format(date));
            monthArea.setBackgroundResource(R.color.pinned_hdr_month_bg_color);
            
            PrayerBoxesHeaderLayout hdr =
                  (PrayerBoxesHeaderLayout)v
                  .findViewById(R.id.prayer_header_boxes);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);
            hdr.setExtendedMode(mIsExtendedMode);
            hdr.showSalahLabels();
         }
      }
      
      @Override
      public void onScroll(AbsListView view, int firstVisibleItem,
                           int visibleItemCount, int totalItemCount) {
         if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView)view).configureHeaderView(firstVisibleItem);
         }           
      }

      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
      }
      
      class ViewHolder {
         View headerView;
         View dividerView;
         View dateAreaView;
         TextView dayOfWeek;
         TextView dayNumber;
         TextView headerMonth;
         PrayerBoxesLayout boxes;
      }
   }
   
}
