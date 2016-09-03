package com.batoulapps.QamarDeen.ui.helpers;

import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.helpers.QamarSelectorHelper.OnQamarSelectionListener;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;
import com.batoulapps.QamarDeen.utils.QamarTime;

public abstract class QamarFragment extends SherlockFragment
    implements OnQamarSelectionListener {

  public static final int REFRESH_MSG = 1;

  protected PinnedHeaderListView mListView = null;
  protected QamarListAdapter mListAdapter = null;
  protected QamarSelectorHelper mPopupHelper = null;
  protected int mHeaderHeight = 0;
  protected boolean mReadData = false;
  protected Button mLoadMoreButton = null;
  protected AsyncTask<Long, Void, Cursor> mLoadingTask = null;
  protected boolean mJustInitialized = false;


  protected Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == REFRESH_MSG) {
        refreshData();
      }
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Activity activity = getActivity();
    View view = inflater.inflate(getLayout(), container, false);
    mListView = (PinnedHeaderListView) view.findViewById(R.id.list);
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
    mLoadMoreButton = (Button) footer.findViewById(R.id.load_more_button);
    mLoadMoreButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (mListAdapter != null) {
          mListAdapter.addDays(30);
        }
      }
    });

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
    mJustInitialized = true;
    return view;
  }

  @Override
  public void onPause() {
    mHandler.removeMessages(REFRESH_MSG);
    if (mPopupHelper != null) {
      mPopupHelper.dismissPopup();
    }
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();

    long midnight = QamarTime.getMidnightMillis();
    long now = Calendar.getInstance().getTimeInMillis();

    long delay = midnight - now;
    mHandler.sendEmptyMessageDelayed(REFRESH_MSG, delay);

    if (!mJustInitialized) {
      refreshData();
    }
    mJustInitialized = false;
  }

  /**
   * dismisses any selector popup if it exists.
   *
   * @return boolean as to whether or not it dismissed a popup.
   */
  public boolean dismissPopup() {
    if (mPopupHelper != null && mPopupHelper.isShowing()) {
      mPopupHelper.dismissPopup();
      return true;
    }
    return false;
  }

  protected int getLayout() {
    return R.layout.qamar_list;
  }

  protected void initializePopup(Context context) {
    mPopupHelper = new QamarSelectorHelper(context);
  }

  public void refreshData() {
    if (mListAdapter != null) {
      if (mLoadingTask != null) {
        mLoadingTask.cancel(true);
        mLoadingTask = null;
      }
      mListAdapter.requeryData();
      return;
    }
  }

  /**
   * gets data for a range of times
   *
   * @param maxDate the max date to get data for.  may be null.
   * @param minDate the min date to get data for.  may be null.
   */
  protected void requestRangeData(Long maxDate, Long minDate) {
    if (mLoadingTask != null) {
      mLoadingTask.cancel(true);
    }

    Calendar calendar = QamarTime.getTodayCalendar();
    if (maxDate != null) {
      calendar.setTimeInMillis(maxDate);
    }

    // need ts of 12:00:00 on the max day in gmt
    maxDate = QamarTime.getGMTTimeFromLocal(calendar);

    if (minDate == null) {
      // if no min date, backup 30 days
      calendar.add(Calendar.DATE, -1 * 30);
      minDate = calendar.getTimeInMillis();
    } else {
      calendar.setTimeInMillis(minDate);
    }

    // need ts 12:00:00 on the min day in gmt
    minDate = QamarTime.getGMTTimeFromLocal(calendar);

    // get the data from the database
    mLoadingTask = getDataReadingTask();
    mLoadingTask.execute(maxDate, minDate);
  }

  @Override
  public void onItemSelected(int row, int item, int selection) {
  }

  @Override
  public void onMultipleItemsSelected(int row, List<Integer> selection) {
  }

  protected abstract QamarListAdapter createAdapter(Context context);

  protected abstract int getHeaderLayout();

  protected abstract AsyncTask<Long, Void, Cursor> getDataReadingTask();
}
