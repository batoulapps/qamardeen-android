package com.batoulapps.QamarDeen.ui.helpers;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.helpers.QamarSelectorHelper.OnQamarSelectionListener;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.utils.QamarTime;

public abstract class QamarFragment extends SherlockFragment
   implements OnQamarSelectionListener {

   protected PinnedHeaderListView mListView = null;
   protected QamarListAdapter mListAdapter = null;
   protected QamarSelectorHelper mPopupHelper = null;
   protected int mHeaderHeight = 0;
   protected boolean mReadData = false;
   protected AsyncTask<Long, Void, Cursor> mLoadingTask = null;

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
      mListView.setDividerHeight(0);
      mListAdapter = createAdapter(activity);
      
      // setup the list and adapter
      Resources res = activity.getResources();
      mHeaderHeight = res.getDimensionPixelSize(R.dimen.header_height);
      int itemHeight = res.getDimensionPixelSize(R.dimen.list_item_height);
      int abHeight = 
            res.getDimensionPixelSize(R.dimen.abs__action_bar_default_height);
      
      // set the footer
      DisplayMetrics metrics = new DisplayMetrics();
      activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      
      int footerHeight = metrics.heightPixels -
            ((2 * abHeight) + itemHeight + mHeaderHeight);
      View footer = inflater.inflate(R.layout.list_footer, null, false);
      AbsListView.LayoutParams ll = new AbsListView.LayoutParams(
            LayoutParams.MATCH_PARENT, footerHeight);
      footer.setLayoutParams(ll);
      mListView.addFooterView(footer);
      
      // set the adapter
      mListView.setAdapter(mListAdapter);
      
      // set pinned header
      mListView.setPinnedHeaderView(
            inflater.inflate(getHeaderLayout(), mListView, false));
      mListView.setOnScrollListener(mListAdapter);
      mListView.setDividerHeight(0);
      
      initializePopup(activity);
      return view;
   }
   
   @Override
   public void onPause() {
      if (mPopupHelper != null){
         mPopupHelper.dismissPopup();
      }
      super.onPause();
   }
   
   protected void initializePopup(Context context){
      mPopupHelper = new QamarSelectorHelper(context);
   }
   
   public void refreshData(){
      if (mReadData == false && mListAdapter != null){
         mListAdapter.requeryData();
      }
   }
   
   /**
    * gets data for a range of times
    * @param maxDate the max date to get data for.  may be null.
    * @param minDate the min date to get data for.  may be null.
    */
   protected void requestRangeData(Long maxDate, Long minDate){
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
      mLoadingTask = getDataReadingTask();
      mLoadingTask.execute(maxDate, minDate);
   }
   
   @Override
   public void onItemSelected(int row, int item, int selection){
   }
   
   @Override
   public void onMultipleItemsSelected(int row, List<Integer> selection){
   }
   
   protected abstract QamarListAdapter createAdapter(Context context);
   protected abstract int getHeaderLayout();
   protected abstract AsyncTask<Long, Void, Cursor> getDataReadingTask();
}
