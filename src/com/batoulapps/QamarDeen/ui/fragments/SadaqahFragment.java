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
   
   private void popupSadaqahBox(View anchorView, int currentRow){
      List<Integer> sel =
            ((SadaqahListAdapter)mListAdapter).getDataItem(currentRow);
      mPopupHelper.showMultipleChoicePopup(this, anchorView, currentRow,
            sel, R.array.charity_options, R.array.charity_values);
   }
   
   @Override
   public void onMultipleItemsSelected(int row, List<Integer> selection){
   }
   
   private class SadaqahListAdapter extends QamarListAdapter {
      private Map<Long, List<Integer>> dataMap = new HashMap<Long, List<Integer>>();

      public SadaqahListAdapter(Context context){
         super(context);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
         requestRangeData(maxDate, minDate);
      }
      
      public void addDayData(Map<Long, List<Integer>> data){
         dataMap.putAll(data);
      }
      
      public List<Integer> getDataItem(int position){
         Date date = (Date)getItem(position);
         if (date != null){
            return dataMap.get(date.getTime());
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
         holder.sadaqahWidget.setSadaqat(dataMap.get(date.getTime()));
         
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
