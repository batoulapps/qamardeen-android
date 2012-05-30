package com.batoulapps.QamarDeen.ui.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarFragment;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.utils.QamarTime;

public class QuranFragment extends QamarFragment {

   private boolean mIsStandardReadingMode = true;
   
   public static QuranFragment newInstance(){
      return new QuranFragment();
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState){
      View view = super.onCreateView(inflater, container, savedInstanceState);
      mListView.setOnItemClickListener(new OnItemClickListener() {

         @Override
         public void onItemClick(AdapterView<?> parent, View view,
               int position, long id) {
            mListAdapter.scrollListToPosition(
                  mListView, position, mHeaderHeight);
            view.postDelayed(
                  new Runnable(){
                     public void run(){ 
                        //popupQuranBox(v, currentRow);
                     } 
                  }, 50);
         }
      });
      return view;
   }
   
   @Override
   public int getHeaderLayout(){
      return R.layout.quran_hdr;
   }
   
   @Override
   protected QamarListAdapter createAdapter(Context context){
      return new QuranListAdapter(context);
   }
   
   @Override
   protected AsyncTask<Long, Void, Cursor> getDataReadingTask(){
      return new ReadQuranDataTask();
   }
   
   private class ReadQuranDataTask extends AsyncTask<Long, Void, Cursor> {
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)QuranFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         return adapter.getQuranEntries(maxDate / 1000, minDate / 1000);
      }
      
      @Override
      protected void onPostExecute(Cursor cursor){
         if (cursor != null){
            if (cursor.moveToFirst()){
               Map<Long, QuranData> dayData = new HashMap<Long, QuranData>();
               Map<Long, List<Integer>> extraData =
                     new HashMap<Long, List<Integer>>();
               do {
                  long timestamp = cursor.getLong(1) * 1000;
                  int endAyah = cursor.getInt(2);
                  int endSura = cursor.getInt(3);
                  int startAyah = cursor.getInt(4);
                  int startSura = cursor.getInt(5);
                  int isExtraReading = cursor.getInt(6);
                  
                  // time calculations
                  Calendar gmtCal = QamarTime.getGMTCalendar();
                  gmtCal.setTimeInMillis(timestamp);
                  long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);
                  
                  if (isExtraReading == 1){
                     List<Integer> suras = extraData.get(localTimestamp);
                     if (suras == null){
                        suras = new ArrayList<Integer>();
                     }
                     suras.add(endSura);
                     extraData.put(localTimestamp, suras);
                  }
                  else {
                     QuranData qd = new QuranData();
                     qd.endAyah = endAyah;
                     qd.endSura = endSura;
                     qd.startAyah = startAyah;
                     qd.startSura = startSura;
                     dayData.put(localTimestamp, qd);
                  }
               }
               while (cursor.moveToNext());
               
               boolean changed = false;
               if (!dayData.isEmpty()){
                  // set the data in the adapter
                  changed = true;
                  ((QuranListAdapter)mListAdapter).addDayData(dayData);
               }
               
               if (!extraData.isEmpty()){
                  changed = true;
                  ((QuranListAdapter)mListAdapter).addExtraData(extraData);
               }
               
               if (changed){
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
   
   public static class QuranData {
      public int startAyah;
      public int startSura;
      public int endAyah;
      public int endSura;
   }
   
   /**
    * get the number of ayahs read for a QuranData object
    * @param data a QuranData object with read info
    * @return the number of ayahs read
    */
   public int getAyahCount(QuranData data){
      if (data.startSura > data.endSura){ return 0; }
      else if ((data.startSura == data.endSura) &&
               (data.startAyah > data.endAyah)){ return 0; }
      int ayahs = QamarConstants.SURA_NUM_AYAHS[data.startSura-1];
      ayahs = ayahs - data.startAyah;
      for (int i = data.startSura + 1; i < data.endSura; i++){
         ayahs += QamarConstants.SURA_NUM_AYAHS[i-1];
      }
      ayahs += data.endAyah;
      return ayahs;
   }
   
   private class QuranListAdapter extends QamarListAdapter {
      Map<Long, QuranData> mDayData = new HashMap<Long, QuranData>();
      Map<Long, List<Integer>> mExtraData =
            new HashMap<Long, List<Integer>>();
      String[] mSuras = null;
      
      public QuranListAdapter(Context context){
         super(context);
         mSuras =context.getResources().getStringArray(R.array.sura_names);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestRangeData(maxDate, minDate);
      }
      
      public void addDayData(Map<Long, QuranData> data){
         mDayData.putAll(data);
      }
      
      public void addExtraData(Map<Long, List<Integer>> data){
         mExtraData.putAll(data);
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.quran_layout, null);
            populateDayInfoInHolder(h, convertView, R.id.quran_hdr);
            h.dailyReadings = 
                  (TextView)convertView.findViewById(R.id.daily_readings);
            h.ayahCount =
                  (TextView)convertView.findViewById(R.id.ayah_count);
            h.jumpImage =
                  (ImageView)convertView.findViewById(R.id.jump_image);
            h.extraReadings =
                  (TextView)convertView.findViewById(R.id.extra_readings);

            holder = h;
            convertView.setTag(holder);
         }
         else { holder = (ViewHolder)convertView.getTag(); }
         
         // initialize generic row stuff (date, header, etc)
         initializeRow(holder, date, position);
         
         if (mIsStandardReadingMode){
            // hide extra readings area
            holder.extraReadings.setVisibility(View.GONE);
            
            // show daily readings, ayah count, and jump images
            holder.dailyReadings.setVisibility(View.VISIBLE);
            holder.ayahCount.setVisibility(View.VISIBLE);
            holder.jumpImage.setVisibility(View.VISIBLE);
            
            QuranData data = mDayData.get(date.getTime());
            if (data != null){
               // set the sura name and ayah count
               holder.dailyReadings.setText(mSuras[data.endSura-1]);
               int readAyahs = getAyahCount(data);
               holder.ayahCount.setText("" + readAyahs);
               
               // TODO set ayah number as left drawable
            }
            else {
               // nothing is in this row, so just hide stuff
               holder.dailyReadings.setText("");
               holder.ayahCount.setText("");
               holder.jumpImage.setVisibility(View.INVISIBLE);
               
               // TODO set placeholder as left drawable
            }
         }
         else {
            holder.extraReadings.setVisibility(View.VISIBLE);
            holder.dailyReadings.setVisibility(View.GONE);
            holder.ayahCount.setVisibility(View.GONE);
            holder.jumpImage.setVisibility(View.GONE);
            
            List<Integer> suras = mExtraData.get(date.getTime());
            if (suras != null){
               boolean first = true;
               StringBuilder suraStringBuilder = new StringBuilder();
               for (Integer sura : suras){
                  if (!first){ suraStringBuilder.append(", "); }
                  suraStringBuilder.append(mSuras[sura-1]);
                  first = false;
               }
               String suraString = suraStringBuilder.toString();
               holder.extraReadings.setText(suraString);
               holder.extraReadings.setCompoundDrawablesWithIntrinsicBounds(
                     0, 0, 0, 0);
            }
            else {
               holder.extraReadings.setText("");
               
               // TODO set placeholder as left drawable
            }
         }
         
         return convertView;
      }
      
      @Override
      public void configurePinnedHeader(View v, int position, int alpha) {
         super.configurePinnedHeader(v, position, alpha);
         if (alpha == 255){
            TextView hdr =
                  (TextView)v.findViewById(R.id.quran_hdr_daily_readings);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);
            hdr = (TextView)v.findViewById(R.id.quran_hdr_ayah_count);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);
            hdr = (TextView)v.findViewById(R.id.quran_hdr_extra_readings);
            hdr.setBackgroundResource(R.color.pinned_hdr_background);

            View jumpArea = (View)v.findViewById(R.id.quran_jump_view);
            jumpArea.setBackgroundResource(R.color.pinned_hdr_background);
         }
      }
      
      class ViewHolder extends QamarViewHolder {
         TextView ayahCount;
         TextView dailyReadings;
         TextView extraReadings;
         ImageView jumpImage;
      }
   }
}
