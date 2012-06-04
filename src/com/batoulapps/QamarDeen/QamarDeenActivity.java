package com.batoulapps.QamarDeen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.fragments.FastingFragment;
import com.batoulapps.QamarDeen.ui.fragments.PrayerFragment;
import com.batoulapps.QamarDeen.ui.fragments.QuranFragment;
import com.batoulapps.QamarDeen.ui.fragments.SadaqahFragment;

public class QamarDeenActivity extends SherlockFragmentActivity 
   implements ActionBar.TabListener {

   private ViewPager mQamarPager;
   private PagerAdapter mPagerAdapter;
   private int[] mTabs = new int[]{ R.string.prayers_tab,
         R.string.quran_tab, R.string.sadaqah_tab, R.string.fasting_tab };
   private QamarDbAdapter mDatabaseAdapter;
   
   @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Sherlock_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // open will happen during the first query, so this is
        // safe for the ui thread since no actual io is done
        mDatabaseAdapter = new QamarDbAdapter(this);
        
        mQamarPager = (ViewPager)findViewById(R.id.qamar_pager);
        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mQamarPager.setAdapter(mPagerAdapter);
        mQamarPager.setOnPageChangeListener(mOnPageChangeListener);
        
        ActionBar actionbar = getSupportActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        for (int i=0; i<mTabs.length; i++){
           ActionBar.Tab tab = actionbar.newTab();
           tab.setText(mTabs[i]);
           tab.setTag(i);
           tab.setTabListener(this);
           actionbar.addTab(tab);
        }
    }
   
   @Override
   protected void onDestroy(){
      mDatabaseAdapter.close();
      super.onDestroy();
   }
   
   public QamarDbAdapter getDatabaseAdapter(){
      return mDatabaseAdapter;
   }
   
   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      MenuInflater inflater = getSupportMenuInflater();
      inflater.inflate(R.menu.qamar_menu, menu);
      return true;
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item){
      if (item.getItemId() == R.id.settings){
         Intent intent = new Intent(this, QamarPreferencesActivity.class);
         startActivity(intent);
         return true;
      }
      
      return super.onOptionsItemSelected(item);
   }
   
   OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener(){

      @Override
      public void onPageScrollStateChanged(int state) {
      }

      @Override
      public void onPageScrolled(int position,
            float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
         ActionBar actionbar = getSherlock().getActionBar();
         Tab tab = actionbar.getTabAt(position);
         actionbar.selectTab(tab);
      }
      
   };
   
   @Override
   public void onTabSelected(Tab tab, FragmentTransaction transaction){
      Integer tag = (Integer)tab.getTag();
      mQamarPager.setCurrentItem(tag);
   }
   
   @Override
   public void onTabReselected(Tab tab, FragmentTransaction transaction){
   }

   @Override
   public void onTabUnselected(Tab tab, FragmentTransaction transaction){
   }
   
   public static class PagerAdapter extends FragmentPagerAdapter {
      public PagerAdapter(FragmentManager fm){
         super(fm);
      }
      
      @Override
      public int getCount(){
         return 4;
      }
      
      @Override
      public Fragment getItem(int position){
         switch (position){
         case 0:
            return PrayerFragment.newInstance();
         case 1:
            return QuranFragment.newInstance();
         case 2:
            return SadaqahFragment.newInstance();
         case 3:
         default:
            return FastingFragment.newInstance();
         }
      }
   }
}