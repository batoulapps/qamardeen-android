package com.batoulapps.QamarDeen.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.batoulapps.QamarDeen.QamarDeenActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.SuraSelectorActivity;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.data.QuranData;
import com.batoulapps.QamarDeen.ui.helpers.QamarFragment;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QuranSelectorPopupHelper;
import com.batoulapps.QamarDeen.ui.helpers.QuranSelectorPopupHelper.OnQuranSelectionListener;
import com.batoulapps.QamarDeen.utils.QamarTime;

import java.util.*;

public class QuranFragment extends QamarFragment
   implements OnQuranSelectionListener {

   public static final String EXTRA_DATE = "date";
   public static final String EXTRA_READ = "read";
   
   private Button mDailyButton = null;
   private Button mExtraButton = null;
   private boolean mIsStandardReadingMode = true;
   private int mLeftPadding = 0;
   private QuranSelectorPopupHelper mQuranSelectorPopupHelper = null;
   private AsyncTask<Object, Void, Boolean> mWritingTask = null;

   public static QuranFragment newInstance(){
      return new QuranFragment();
   }
   
   @Override
   protected int getLayout(){
      return R.layout.quran_view;
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
            if (!mIsStandardReadingMode){               
               // launch the selector activity
               Intent intent = new Intent(getActivity(),
                     SuraSelectorActivity.class);
               intent.putExtra(EXTRA_DATE,
                     ((Date)mListAdapter.getItem(position)).getTime());
               intent.putExtra(EXTRA_READ, ((QuranListAdapter)mListAdapter)
                     .getExtraReadSuras(position));
               
               // force refresh in onResume
               mReadData = false;
               // clear data to avoid conflicts when we return
               ((QuranListAdapter)mListAdapter).clearData();
               
               startActivity(intent);
               return;
            }
            
            view.postDelayed(
                  new Runnable(){
                     public void run(){ 
                        popupQuranBox(view, position);
                     } 
                  }, 50);
         }
      });
      
      mDailyButton = (Button)view.findViewById(R.id.daily_button);
      mExtraButton = (Button)view.findViewById(R.id.extra_button);
      mDailyButton.setOnClickListener(mOnButtonClickListener);
      mExtraButton.setOnClickListener(mOnButtonClickListener);
      mDailyButton.setEnabled(false);
      mExtraButton.setEnabled(true);
      
      mLeftPadding = getResources()
            .getDimensionPixelSize(R.dimen.extra_reading_padding);
      return view;
   }
   
   OnClickListener mOnButtonClickListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         if (v.getId() == R.id.daily_button){
            if (!mIsStandardReadingMode){
               mDailyButton.setEnabled(false);
               mExtraButton.setEnabled(true);
               
               View hdr = mListView.getPinnedHeaderView();
               TextView hdrText =
                     (TextView)hdr.findViewById(R.id.quran_hdr_daily_readings);
               hdrText.setVisibility(View.VISIBLE);
               hdrText = (TextView)hdr.findViewById(R.id.quran_hdr_ayah_count);
               hdrText.setVisibility(View.VISIBLE);
               hdrText = (TextView)hdr.findViewById(R.id.quran_hdr_extra_readings);
               hdrText.setVisibility(View.GONE);
               hdr.invalidate();
               
               mIsStandardReadingMode = true;
               mListAdapter.notifyDataSetChanged();
            }
         }
         else if (v.getId() == R.id.extra_button){            
            if (mIsStandardReadingMode){
               mDailyButton.setEnabled(true);
               mExtraButton.setEnabled(false);
               
               View hdr = mListView.getPinnedHeaderView();
               TextView hdrText =
                     (TextView)hdr.findViewById(R.id.quran_hdr_daily_readings);
               hdrText.setVisibility(View.GONE);
               hdrText = (TextView)hdr.findViewById(R.id.quran_hdr_ayah_count);
               hdrText.setVisibility(View.GONE);
               hdrText = (TextView)hdr.findViewById(R.id.quran_hdr_extra_readings);
               hdrText.setVisibility(View.VISIBLE);
               hdr.invalidate();

               mIsStandardReadingMode = false;
               mListAdapter.notifyDataSetChanged();
            }
         }
      }
   };
   
   @Override
   public void onPause() {
      if (mQuranSelectorPopupHelper != null){
         mQuranSelectorPopupHelper.dismissPopup();
      }
      super.onPause();
   }
   
   @Override
   protected void initializePopup(Context context){
      mQuranSelectorPopupHelper = new QuranSelectorPopupHelper(context);
   }
   
   private void popupQuranBox(View anchorView, int currentRow){      
      int selectedSura = 1;
      int selectedAyah = 0;
      
      QuranData startData = null;
      Date date = (Date)mListAdapter.getItem(currentRow);
      startData = ((QuranListAdapter)mListAdapter).mDayData
            .get(date.getTime());
      
      if (startData == null){
         startData = ((QuranListAdapter)mListAdapter)
               .getEarlierEntry(currentRow);
      }
      
      selectedSura = startData.getEndSura();
      selectedAyah = startData.getEndAyah();
      
      mQuranSelectorPopupHelper.showPopup(this, anchorView, currentRow,
            selectedSura, selectedAyah);
   }
   
   @Override
   public void onSuraAyahSelected(int row, int sura, int ayah) {
      if (mWritingTask != null){
         mWritingTask.cancel(true);
      }
            
      // get or initialize the current quran entry
      Date currentDate = (Date)mListAdapter.getItem(row);
      QuranData currentEntry = ((QuranListAdapter)mListAdapter)
            .mDayData.get(currentDate.getTime());
      if (currentEntry == null){
         // this is a new entry, so use an earlier entry as a template
         QuranData earlierEntry =
               ((QuranListAdapter)mListAdapter).getEarlierEntry(row);
         currentEntry = new QuranData();
         
         // our starting point is the previous entry's ending point
         currentEntry.setStartAyah(earlierEntry.getEndAyah());
         currentEntry.setStartSura(earlierEntry.getEndSura());
      }
      
      // set the end sura and ayah based on the user's selection
      currentEntry.setEndAyah(ayah);
      currentEntry.setEndSura(sura);
      
      long currentTimestamp = QamarTime.getGMTTimeFromLocalDate(currentDate);
      
      /* see if we have affected a row that came after us, and if so, get the
       * row and update its data based on the new values the user selected */
      
      Long affectedTimestamp = null;
      QuranData affectedRowData = null;
      Integer affectedRowNumber =
            ((QuranListAdapter)mListAdapter).getLaterEntryRow(row);
      if (affectedRowNumber != null){
         // get details about the affected row
         Date affectedDate = (Date)mListAdapter.getItem(affectedRowNumber);
         affectedTimestamp = QamarTime.getGMTTimeFromLocalDate(affectedDate);
         affectedRowData = ((QuranListAdapter)mListAdapter)
               .mDayData.get(affectedDate.getTime());
         affectedRowData.setStartAyah(ayah);
         affectedRowData.setStartSura(sura);
      }
      
      mWritingTask = new WriteQuranDataTask(
            row, currentTimestamp, currentEntry,
            affectedRowNumber, affectedTimestamp, affectedRowData);
      mWritingTask.execute();
   }
   
   @Override
   public void onNoneSelected(int row) {
      if (mWritingTask != null){
         mWritingTask.cancel(true);
      }
      
      Date currentDate = (Date)mListAdapter.getItem(row);
      long currentTimestamp = QamarTime.getGMTTimeFromLocalDate(currentDate);
      
      /* see if we have affected a row that came after us, and if so, get the
       * row and update its data based on the data point prior to this one. */
      
      Long affectedTimestamp = null;
      QuranData affectedRowData = null;
      Integer affectedRowNumber =
            ((QuranListAdapter)mListAdapter).getLaterEntryRow(row);
      if (affectedRowNumber != null){
         // get details about the affected row
         Date affectedDate = (Date)mListAdapter.getItem(affectedRowNumber);
         affectedTimestamp = QamarTime.getGMTTimeFromLocalDate(affectedDate);
         affectedRowData = ((QuranListAdapter)mListAdapter)
               .mDayData.get(affectedDate.getTime());
         
         // update the start sura/ayah to point to the earlier datapoint
         QuranData earlierEntry =
               ((QuranListAdapter)mListAdapter).getEarlierEntry(row);
         affectedRowData.setStartAyah(earlierEntry.getEndAyah());
         affectedRowData.setStartSura(earlierEntry.getEndSura());
      }
      
      mWritingTask = new WriteQuranDataTask(row, currentTimestamp, null,
            affectedRowNumber, affectedTimestamp, affectedRowData);
      mWritingTask.execute();
   }
   
   private class WriteQuranDataTask extends AsyncTask<Object, Void, Boolean> {
      private int mSelectedRow = -1;
      private long mChangedTimestamp = -1;
      private QuranData mChangedData = null;
      private Integer mAffectedRowNumber = null;
      private Long mAffectedTimestamp = null;
      private QuranData mAffectedData = null;
      
      public WriteQuranDataTask(int changedRow, long changedTimestamp,
            QuranData changedData, Integer affectedRowNumber,
            Long affectedTimestamp,
            QuranData affectedData){
         mSelectedRow = changedRow;
         mChangedTimestamp = changedTimestamp;
         mChangedData = changedData;
         mAffectedTimestamp = affectedTimestamp;
         mAffectedData = affectedData;
         mAffectedRowNumber = affectedRowNumber;
      }
      
      @Override
      protected Boolean doInBackground(Object... params) {
         QamarDeenActivity activity =
               (QamarDeenActivity)QuranFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         
         Long affectedTime = null;
         if (mAffectedTimestamp != null){
            affectedTime = mAffectedTimestamp / 1000;
         }
         
         return adapter.writeQuranEntry(mChangedTimestamp / 1000,
               mChangedData, affectedTime, mAffectedData);
      }
      
      @Override
      protected void onPostExecute(Boolean result) {
         if (result != null && result == true){
            // calculate the local timestamp
            Calendar gmtCal = QamarTime.getGMTCalendar();
            gmtCal.setTimeInMillis(mChangedTimestamp);
            long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

            // update the list adapter with the data
            ((QuranListAdapter)mListAdapter)
               .updateQuranDataRow(localTimestamp, mChangedData);
            if (mAffectedTimestamp != null){
               gmtCal = QamarTime.getGMTCalendar();
               gmtCal.setTimeInMillis(mAffectedTimestamp);
               long affectedTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);
               
               ((QuranListAdapter)mListAdapter)
                  .updateQuranDataRow(affectedTimestamp, mAffectedData);
            }
            
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
            
            // attempt to refresh the affected element if it exists
            if (mAffectedRowNumber != null && mAffectedRowNumber >= start &&
                  mAffectedRowNumber <= end){
               View view = mListView.getChildAt(mAffectedRowNumber - start);
               if (view != null){
                  mListAdapter.getView(mAffectedRowNumber, view, mListView);
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
      private QuranData mEarlierData = null;
      
      @Override
      protected Cursor doInBackground(Long... params){
         long maxDate = params[0];
         long minDate = params[1];
         
         QamarDeenActivity activity =
               (QamarDeenActivity)QuranFragment.this.getActivity();
         QamarDbAdapter adapter = activity.getDatabaseAdapter();
         Cursor earlierCursor = adapter.getEarlierQuranEntry(minDate / 1000);
         if (earlierCursor != null){
            if (earlierCursor.moveToFirst()){
               QuranData d = new QuranData();
               d.setEndAyah(earlierCursor.getInt(2));
               d.setEndSura(earlierCursor.getInt(3));
               d.setStartAyah(earlierCursor.getInt(4));
               d.setStartSura(earlierCursor.getInt(5));
               mEarlierData = d;
            }
            earlierCursor.close();
         }
         
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
                     QuranData qd = new QuranData(
                           startAyah, startSura, endAyah, endSura);
                     dayData.put(localTimestamp, qd);
                  }
               }
               while (cursor.moveToNext());
               
               if (!dayData.isEmpty()){
                  // set the data in the adapter
                  ((QuranListAdapter)mListAdapter).addDayData(dayData);
               }
               
               if (!extraData.isEmpty()){
                  ((QuranListAdapter)mListAdapter).addExtraData(extraData);
               }
            }

            Log.d("are", "here and setting notifyDataSetChanged");
            mListAdapter.notifyDataSetChanged();
            ((QuranListAdapter)mListAdapter).setEarlierEntryData(mEarlierData);
            cursor.close();
            mReadData = true;
         }
         else { mReadData = false; }         
         mLoadingTask = null;
      }
   }
   
   private class QuranListAdapter extends QamarListAdapter {
      private Map<Long, QuranData> mDayData = new HashMap<Long, QuranData>();
      private Map<Long, List<Integer>> mExtraData =
            new HashMap<Long, List<Integer>>();
      private String[] mSuras = null;
      
      // this is used to store the first entry not on the screen
      private QuranData mEarlierEntryData = null;
      
      public QuranListAdapter(Context context){
         super(context);
         mSuras = context.getResources().getStringArray(R.array.sura_names);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestRangeData(maxDate, minDate);
      }
      
      public void updateQuranDataRow(long timestamp, QuranData data){
         mDayData.put(timestamp, data);
      }
      
      public void addDayData(Map<Long, QuranData> data){
         mDayData.putAll(data);
      }
      
      public void addExtraData(Map<Long, List<Integer>> data){
         mExtraData.putAll(data);
      }
      
      public void setEarlierEntryData(QuranData earlierData){
         mEarlierEntryData = earlierData;
      }
      
      public void clearData(){
         mExtraData.clear();
         mDayData.clear();
         mEarlierEntryData = null;
      }
      
      public String getExtraReadSuras(int position){
         StringBuilder result = new StringBuilder();
         long date = ((Date)getItem(position)).getTime();
         List<Integer> data = mExtraData.get(date);
         if (data != null){
            boolean firstItem = true;
            for (Integer item : data){
               if (firstItem){ firstItem = false; }
               else { result.append(","); }
               result.append(item);
            }
         }
         return result.toString();
      }
      
      /**
       * gets an earlier QuranData object based on this row
       * @param row the current row
       * @return QuranData representing the first row before
       * this row with data, or a default QuranData structure
       */
      public QuranData getEarlierEntry(int row){
         QuranData result = null;
         Integer earlierRow = getEarlierEntryRow(row);
         if (earlierRow != null){
            Date d = (Date)getItem(earlierRow);
            result = mDayData.get(d.getTime());
         }
         
         if (result == null){ result = mEarlierEntryData; }
         if (result == null){ result = new QuranData(); }
         return result;
      }
      
      /**
       * gets the row number of the first entry who is older
       * than the current row and has data
       * @param row the current row
       * @return the row number or null if none
       */
      public Integer getEarlierEntryRow(int row){
         Integer result = null;
         for (int i=row + 1; i < getCount(); i++){
            Date d = (Date)getItem(i);
            if (mDayData.containsKey(d.getTime())){
               result = i;
               break;
            }
         }
         return result;
      }
      
      /**
       * gets the row number of the first entry who is newer
       * than the current row and has data
       * @param row the current row
       * @return the row number or null if none
       */
      public Integer getLaterEntryRow(int row){
         Integer result = null;
         for (int i=row - 1; i >= 0; i--){
            Date d = (Date)getItem(i);
            if (mDayData.containsKey(d.getTime())){
               result = i;
               break;
            }
         }
         return result;
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
            h.ayahArea = convertView.findViewById(R.id.ayah_area);
            h.ayahNumber = (TextView)h.ayahArea.findViewById(R.id.ayah_number);
            h.ayahImage = (ImageView)h.ayahArea.findViewById(R.id.ayah_image);
            
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
            holder.ayahArea.setVisibility(View.VISIBLE);
            
            QuranData data = mDayData.get(date.getTime());
            if (data != null){
               // set the sura name and ayah count
               holder.dailyReadings.setText(mSuras[data.getEndSura()-1]);
               int readAyahs = data.getAyahCount();
               holder.ayahCount.setText("" + readAyahs);
               holder.ayahImage.setImageResource(R.drawable.quran_ayah);
               holder.ayahNumber.setText("" + data.getEndAyah());
            }
            else {
               // nothing is in this row, so just hide stuff
               holder.dailyReadings.setText("");
               holder.ayahCount.setText("");
               holder.jumpImage.setVisibility(View.INVISIBLE);
               
               holder.ayahImage.setImageResource(R.drawable.prayer_notset);
               holder.ayahNumber.setText("");
            }
         }
         else {
            holder.extraReadings.setVisibility(View.VISIBLE);
            holder.ayahArea.setVisibility(View.GONE);
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
               holder.extraReadings.setPadding(mLeftPadding, 0, 0, 0);
            }
            else {
               holder.extraReadings.setText("");
               holder.extraReadings.setPadding(0, 0, 0, 0);
               holder.extraReadings.setCompoundDrawablesWithIntrinsicBounds(
                     R.drawable.prayer_notset, 0, 0, 0);
            }
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
         View ayahArea;
         TextView ayahCount;
         TextView ayahNumber;
         TextView dailyReadings;
         TextView extraReadings;
         ImageView jumpImage;
         ImageView ayahImage;
      }
   }
}
