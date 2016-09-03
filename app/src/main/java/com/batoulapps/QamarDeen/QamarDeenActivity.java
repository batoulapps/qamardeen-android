package com.batoulapps.QamarDeen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.ui.fragments.FastingFragment;
import com.batoulapps.QamarDeen.ui.fragments.PrayerFragment;
import com.batoulapps.QamarDeen.ui.fragments.QuranFragment;
import com.batoulapps.QamarDeen.ui.fragments.SadaqahFragment;
import com.batoulapps.QamarDeen.ui.helpers.QamarFragment;
import com.crashlytics.android.Crashlytics;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

public class QamarDeenActivity extends AppCompatActivity implements ActionBar.TabListener {

  private ViewPager mQamarPager;
  private PagerAdapter mPagerAdapter;
  private int[] mTabs = new int[]{ R.string.prayers_tab,
      R.string.quran_tab, R.string.sadaqah_tab, R.string.fasting_tab };
  private QamarDbAdapter mDatabaseAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    SharedPreferences prefs =
        PreferenceManager.getDefaultSharedPreferences(this);

    Locale locale;
    if (prefs.getBoolean(QamarConstants.PreferenceKeys.USE_ARABIC, false)) {
      locale = new Locale("ar");
    } else {
      locale = Locale.getDefault();
    }

    Resources resources = getResources();
    Configuration config = resources.getConfiguration();
    config.locale = locale;
    resources.updateConfiguration(config,
        resources.getDisplayMetrics());

    super.onCreate(savedInstanceState);

    if (!BuildConfig.DEBUG) {
      Fabric.with(this, new Crashlytics());
    }

    setContentView(R.layout.main);

    // open will happen during the first query, so this is
    // safe for the ui thread since no actual io is done
    mDatabaseAdapter = new QamarDbAdapter(this);

    mQamarPager = (ViewPager) findViewById(R.id.qamar_pager);
    mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
    mQamarPager.setAdapter(mPagerAdapter);
    mQamarPager.addOnPageChangeListener(mOnPageChangeListener);

    ActionBar actionbar = getSupportActionBar();
    actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      actionbar.setDisplayShowTitleEnabled(false);
    }

    for (int i = 0; i < mTabs.length; i++) {
      ActionBar.Tab tab = actionbar.newTab();
      tab.setText(mTabs[i]);
      tab.setTag(i);
      tab.setTabListener(this);
      actionbar.addTab(tab);
    }
  }

  @Override
  protected void onDestroy() {
    mDatabaseAdapter.close();
    super.onDestroy();
  }

  public QamarDbAdapter getDatabaseAdapter() {
    return mDatabaseAdapter;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.qamar_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.settings) {
      Intent intent = new Intent(this, QamarPreferencesActivity.class);
      startActivity(intent);
      return true;
    } else if (item.getItemId() == R.id.graphs) {
      Intent intent = new Intent(this, QamarGraphActivity.class);
      startActivity(intent);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (mPagerAdapter != null && mQamarPager != null) {
         /* back now first checks if it can ask a fragment
          * to dismiss a popup.  if it can, it dismisses it.
          * if not, it leaves the app like it used to.
          */
      int item = mQamarPager.getCurrentItem();
      String fragmentTag = PagerAdapter.getFragmentTag(
          R.id.qamar_pager, item);
      FragmentManager fm = getSupportFragmentManager();
      Fragment f = fm.findFragmentByTag(fragmentTag);
      if (f != null && f instanceof QamarFragment) {
        boolean dismissed = ((QamarFragment) f).dismissPopup();
        if (dismissed) {
          return;
        }
      }
    }
    super.onBackPressed();
  }

  OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position,
                               float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
      ActionBar actionbar = getSupportActionBar();
      ActionBar.Tab tab = actionbar.getTabAt(position);
      actionbar.selectTab(tab);
    }
  };

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction transaction) {
    Integer tag = (Integer) tab.getTag();
    mQamarPager.setCurrentItem(tag);
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction transaction) {
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction transaction) {
  }

  public static class PagerAdapter extends FragmentPagerAdapter {
    public PagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public int getCount() {
      return 4;
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
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

    /**
     * this is a private method in FragmentPagerAdapter that allows getting the tag that it uses to
     * store the fragment in (for use by getFragmentByTag).  in the future, this could change and
     * cause us issues...
     *
     * @param viewId the view id of the viewpager
     * @param index  the index of the fragment to get
     * @return the tag in which it would be stored under
     */
    public static String getFragmentTag(int viewId, int index) {
      return "android:switcher:" + viewId + ":" + index;
    }
  }
}