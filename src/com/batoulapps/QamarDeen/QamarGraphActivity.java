package com.batoulapps.QamarDeen;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar.Tab;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.data.ScoresHelper;
import com.batoulapps.QamarDeen.data.ScoresHelper.ScoreResult;
import com.batoulapps.QamarDeen.ui.widgets.GraphWidget;
import com.batoulapps.QamarDeen.ui.widgets.StatisticsWidget;
import com.batoulapps.QamarDeen.ui.widgets.TimeSelectorWidget;
import com.batoulapps.QamarDeen.utils.QamarTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class QamarGraphActivity extends SherlockActivity
        implements ActionBar.TabListener,
                   TimeSelectorWidget.TimeSelectedListener {

   public static final int GRAPH_PRAYER_TAB = 0;
   public static final int GRAPH_QURAN_TAB = 1;
   public static final int GRAPH_SADAQAH_TAB = 2;
   public static final int GRAPH_FASTING_TAB = 3;

   private int mCurrentTab = 0;
   private int mDateOption = 1;
   private int[] mDateOffsets = new int[]{ 7, 14, 30, 60, 90, 180, 365, -1 };
   private int[] mTabs = new int[]{R.string.prayers_tab,
           R.string.quran_tab, R.string.sadaqah_tab, R.string.fasting_tab};

   private QamarDbAdapter mDatabaseAdapter;

   private ViewPager mGraphPager;
   private TextView mMinDate;
   private TextView mMaxDate;
   private GraphWidget mGraphWidget;
   private StatisticsWidget mStatisticsWidget;

   private GraphPagerAdapter mGraphAdapter;
   private static DateFormat mDateFormat = DateFormat.getDateInstance();

   @Override
   public void onCreate(Bundle savedInstanceState) {
      setTheme(R.style.Theme_Sherlock_Light);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.graph_layout);

      mDatabaseAdapter = new QamarDbAdapter(this);

      mGraphPager = (ViewPager)findViewById(R.id.graph_pager);
      mGraphAdapter = new GraphPagerAdapter();
      mGraphPager.setAdapter(mGraphAdapter);
      mMinDate = (TextView)findViewById(R.id.min_date);
      mMaxDate = (TextView)findViewById(R.id.max_date);
      TimeSelectorWidget timeSelectorWidget =
              (TimeSelectorWidget)findViewById(R.id.time_selector);
      timeSelectorWidget.setTimeSelectedListener(this);

      ActionBar actionbar = getSupportActionBar();
      actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

      for (int i = 0; i < mTabs.length; i++) {
         ActionBar.Tab tab = actionbar.newTab();
         tab.setText(mTabs[i]);
         tab.setTag(i);
         tab.setTabListener(this);
         actionbar.addTab(tab);
      }

      Calendar today = QamarTime.getTodayCalendar();
      mMaxDate.setText(mDateFormat.format(today.getTime()));
      drawGraph();
   }

   @Override
   protected void onDestroy(){
      mDatabaseAdapter.close();
      super.onDestroy();
   }

   @Override
   public void timeSelected(int position){
      mDateOption = position;
      refreshData();
   }

   public void refreshData(){
      mGraphWidget.showProgressView();
      mStatisticsWidget.showProgressView();
      drawGraph();
   }

   private void drawGraph(){
      Calendar today = QamarTime.getTodayCalendar();
      long maxTime = QamarTime.getGMTTimeFromLocal(today);
      long minTime = getGmtTimestamp(today);

      new DataFetcher().execute(maxTime, minTime);
   }

   public long getGmtTimestamp(Calendar localCal){
      int delta = mDateOffsets[mDateOption];
      if (delta == -1){ return 0; }
      localCal.add(Calendar.DATE, -1 * delta);
      mMinDate.setText(mDateFormat.format(localCal.getTime()));
      return QamarTime.getGMTTimeFromLocal(localCal);
   }

   private class DataFetcher extends AsyncTask<Long, Void, ScoreResult> {

      @Override
      protected ScoreResult doInBackground(Long... params){
         long maxDate = params[0] / 1000;
         long minDate = params[1] / 1000;

         if (mCurrentTab == GRAPH_PRAYER_TAB){
            return ScoresHelper.getPrayerScores(mDatabaseAdapter,
                    maxDate, minDate);
         }
         else if (mCurrentTab == GRAPH_QURAN_TAB){
            return ScoresHelper.getQuranScores(mDatabaseAdapter,
                    maxDate, minDate);
         }
         else if (mCurrentTab == GRAPH_SADAQAH_TAB){
            return ScoresHelper.getSadaqahScores(mDatabaseAdapter,
                    maxDate, minDate);
         }
         else if (mCurrentTab == GRAPH_FASTING_TAB){
            return ScoresHelper.getFastingScores(mDatabaseAdapter,
                    maxDate, minDate);
         }
         return null;
      }

      @Override
      protected void onPostExecute(ScoreResult result){
         if (result != null && mGraphWidget != null){
            mGraphWidget.renderGraph(result.scores);
            mStatisticsWidget.showStats(mCurrentTab, result.statistics,
                    result.scores.size());
         }
      }
   }

   @Override
   public void onTabSelected(Tab tab, FragmentTransaction transaction) {
      Integer tag = (Integer)tab.getTag();
      if (tag != null && tag != mCurrentTab){
         mCurrentTab = tag;
         refreshData();
      }
   }

   @Override
   public void onTabReselected(Tab tab, FragmentTransaction transaction) {
   }

   @Override
   public void onTabUnselected(Tab tab, FragmentTransaction transaction) {
   }

   public class GraphPagerAdapter extends PagerAdapter {
      @Override
      public int getCount(){
         return 2;
      }

      @Override
      public boolean isViewFromObject(View view, Object object) {
         return view == (View)object;
      }

      @Override
      public Object instantiateItem(ViewGroup collection, int position) {
         if (position == 0){
            mGraphWidget = new GraphWidget(QamarGraphActivity.this);
            collection.addView(mGraphWidget);
            return mGraphWidget;
         }
         else {
            mStatisticsWidget = new StatisticsWidget(QamarGraphActivity.this);
            collection.addView(mStatisticsWidget);
            return mStatisticsWidget;
         }
      }
   }
}
