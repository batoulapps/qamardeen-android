package com.batoulapps.QamarDeen.ui.helpers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView.PinnedHeaderAdapter;
import com.batoulapps.QamarDeen.utils.QamarTime;

public abstract class QamarListAdapter extends BaseAdapter implements 
      OnScrollListener, PinnedHeaderAdapter {
   
   protected List<Date> mDays;
   protected Context mContext;
   protected LayoutInflater mInflater;

   public QamarListAdapter(Context context){
      mInflater = LayoutInflater.from(context);
      mDays = new ArrayList<Date>();
      mContext = context;
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
      requestData(maxDate, minDate);
   }
   
   public void requeryData(){
      if (mDays == null || mDays.size() < 2){ return; }
      Calendar endCalendar = QamarTime.getTodayCalendar();
      Date actualDate = endCalendar.getTime();

      Date maxDate = mDays.get(0);
      Date minDate = mDays.get(mDays.size() - 1);
      if (!maxDate.equals(actualDate)){
         maxDate = actualDate;
         mDays.add(0, maxDate);
      }

      requestData(maxDate.getTime(), minDate.getTime());
   }
   
   public abstract void requestData(Long maxDate, Long minDate);

   @Override
   public int getCount(){
      return mDays.size();
   }

   @Override
   public Object getItem(int position){
      return mDays.get(position);
   }

   @Override
   public long getItemId(int position){
      return ((Date)mDays.get(position)).getTime();
   }

   @Override
   public abstract View getView(
         int position, View convertView, ViewGroup parent);
   
   /**
    * convenience method to populate the ViewHolder with the header info
    * @param holder the view holder (subclass of QamarViewHolder)
    * @param convertView the inflated convert view
    * @param headerId the id of the header
    */
   public void populateDayInfoInHolder(QamarViewHolder holder,
                              View convertView, int headerId){
      holder.headerView = (View)convertView.findViewById(headerId);
      holder.dateAreaView = (View)convertView
            .findViewById(R.id.section_date_index);
      holder.dividerView = (View)convertView.findViewById(R.id.list_divider);
      holder.dayOfWeek = (TextView)convertView.findViewById(R.id.day_of_week);
      holder.dayNumber = (TextView)convertView.findViewById(R.id.day_number);
      holder.headerMonth = (TextView)convertView
            .findViewById(R.id.section_month_index);
   }
   
   /**
    * convenience method to initialize a row - handles setting the date info,
    * color of the date field, and whether the header and divider are visible
    * or not.
    * @param holder a subclass of QamarViewHolder, the ViewHolder
    * @param date the date for the row
    * @param position the position of the row
    */
   public void initializeRow(QamarViewHolder holder, Date date, int position){
      // handling for the color of the day area based on whether
      // the day represents today or not
      Resources res = mContext.getResources();
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
               new SimpleDateFormat("MMM").format(date));
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

      if (position < 0 || position >= getCount()) {
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
   public void configurePinnedHeader(View v, int position, int alpha){
      if (alpha == 255){
         TextView monthArea =
               (TextView)v.findViewById(R.id.section_month_index);
         Date date = (Date)getItem(position);
         monthArea.setText(new SimpleDateFormat("MMM").format(date));
         monthArea.setBackgroundResource(R.color.pinned_hdr_month_bg_color);
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
   
   public void scrollListToPosition(ListView listView,
         int currentRow, int headerHeight){
      // use this to determine if we have a header here or not
      int section = getSectionForPosition(currentRow);
      int firstRowForSection = getPositionForSection(section);
      
      // scroll either to 0 (if we are part of a header) or to
      // just under the header
      int scrollHeight =
            (firstRowForSection == currentRow)? 0 : headerHeight; 
      
      if (android.os.Build.VERSION.SDK_INT >= 11){
         // honeycomb+, we get smooth scrolling
         listView.smoothScrollToPositionFromTop(currentRow,
               scrollHeight);
      }
      else {
         // works on older android versions
         listView.setSelectionFromTop(currentRow, scrollHeight);
      }
   }
   
   protected abstract class QamarViewHolder {
      public View headerView;
      public View dividerView;
      public View dateAreaView;
      public TextView dayOfWeek;
      public TextView dayNumber;
      public TextView headerMonth;
   }
}
