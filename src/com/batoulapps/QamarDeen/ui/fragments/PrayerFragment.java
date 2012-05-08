package com.batoulapps.QamarDeen.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
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
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView.PinnedHeaderAdapter;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesHeaderLayout;
import com.batoulapps.QamarDeen.ui.widgets.PrayerBoxesLayout;

public class PrayerFragment extends SherlockFragment {

   private PinnedHeaderListView mListView = null;
   private PrayerListAdapter mListAdapter = null;
   
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
   
   private class PrayerListAdapter extends BaseAdapter implements 
         OnScrollListener, PinnedHeaderAdapter {
      private List<Date> mDays;
      private LayoutInflater mInflater;
      private boolean mIsExtendedMode = false;

      public PrayerListAdapter(Context context){
         mInflater = LayoutInflater.from(context);
         mDays = new ArrayList<Date>();
         addDays(30);
      }
      
      public void addDays(int daysToAdd){
         Calendar endCalendar = Calendar.getInstance();
         endCalendar.add(Calendar.DATE, -1 * (mDays.size() + daysToAdd));
         
         Calendar calculations = Calendar.getInstance();
         calculations.add(Calendar.DATE, -1 * mDays.size());

         while (calculations.compareTo(endCalendar) >= 0){
            mDays.add(calculations.getTime());
            calculations.add(Calendar.DATE, -1);
         }
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
   
         return convertView;
      }
      
      public int getSectionForPosition(int position){
         Date info = (Date)getItem(position);
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(info);
         return calendar.get(Calendar.MONTH) + 1;
      }
      
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
