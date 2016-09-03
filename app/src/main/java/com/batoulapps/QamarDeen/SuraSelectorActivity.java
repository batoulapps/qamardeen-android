package com.batoulapps.QamarDeen;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarDbAdapter;
import com.batoulapps.QamarDeen.data.QuranData;
import com.batoulapps.QamarDeen.ui.fragments.QuranFragment;
import com.batoulapps.QamarDeen.utils.QamarTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SuraSelectorActivity extends SherlockActivity {
  private static final String SI_SELECTED_SURAS = "SI_SELECTED_SURAS";

  private SuraAdapter mListAdapter;
  private long mCurrentTime;
  private ActionMode mMode;
  private int mSelectedColor;
  private boolean mShouldSave = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.Theme_Sherlock_Light);
    super.onCreate(savedInstanceState);

    // initialize the view
    setContentView(R.layout.sura_list);
    mSelectedColor = getResources().getColor(R.color.selected_blue);
    ListView listView = (ListView) findViewById(R.id.list);
    mListAdapter = new SuraAdapter(this);

    Intent currentIntent = getIntent();
    if (currentIntent == null) {
      finish();
      return;
    }

    mCurrentTime = currentIntent.getLongExtra(QuranFragment.EXTRA_DATE, 0);
    if (mCurrentTime == 0) {
      finish();
    }

    String selectedSuras = currentIntent
        .getStringExtra(QuranFragment.EXTRA_READ);
    if (savedInstanceState != null) {
      Object sel = savedInstanceState.get(
          SI_SELECTED_SURAS);
      if (sel != null) {
        selectedSuras = sel.toString();
      }
    }

    if (!TextUtils.isEmpty(selectedSuras)) {
      String[] suras = selectedSuras.split(",");
      for (String suraStr : suras) {
        try {
          int sura = Integer.parseInt(suraStr);
          mListAdapter.selectSura(sura);
        } catch (Exception e) {
        }
      }
    }

    // set the adapter
    listView.setAdapter(mListAdapter);
    listView.setOnItemClickListener(mOnItemClickListener);

    // set the title
    mMode = startActionMode(new QuranSelectorActionMode());
    updateCount(mListAdapter.getSelectedCount());
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    Object[] selectedSuraObjs = mListAdapter.getSelectedSuras();
    if (selectedSuraObjs.length > 0) {
      String selectedSuras = TextUtils.join(",", selectedSuraObjs);
      outState.putString(SI_SELECTED_SURAS, selectedSuras);
    }
    super.onSaveInstanceState(outState);
  }

  private final class QuranSelectorActionMode implements ActionMode.Callback {
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      if (mShouldSave) {
        saveSuraData();
      } else {
        finish();
      }
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
        event.getAction() == KeyEvent.ACTION_UP) {
      mShouldSave = false;
    }
    return super.dispatchKeyEvent(event);
  }

  OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
      boolean selected = false;
      if (mListAdapter.isSuraSelected(position + 1)) {
        mListAdapter.unselectSura(position + 1);
      } else {
        selected = true;
        mListAdapter.selectSura(position + 1);
      }
      ((CheckBox) view).setChecked(selected);
      view.setBackgroundColor(selected ? mSelectedColor : 0);
    }

  };

  private class WriteExtraDataTask extends AsyncTask<Object, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Object... params) {
      Date localDate = new Date(mCurrentTime);
      long entryTime = QamarTime.getGMTTimeFromLocalDate(localDate) / 1000;

      List<QuranData> suras = new ArrayList<QuranData>();
      for (Object item : params) {
        try {
          Integer sura = Integer.parseInt(item.toString());
          int endAyah = QamarConstants.SURA_NUM_AYAHS[sura - 1];
          QuranData qd = new QuranData(1, sura, endAyah, sura);
          suras.add(qd);
        } catch (Exception e) {
        }
      }

      QamarDbAdapter databaseAdapter =
          new QamarDbAdapter(SuraSelectorActivity.this);
      boolean result =
          databaseAdapter.writeExtraQuranEntries(entryTime, suras);
      databaseAdapter.close();
      return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    }
  }

  public void saveSuraData() {
    Object[] suras = mListAdapter.getSelectedSuras();
    AsyncTask<Object, Void, Boolean> writingTask =
        new WriteExtraDataTask();
    writingTask.execute(suras);
    finish();
  }

  public void updateCount(int count) {
    String countString = getResources()
        .getQuantityString(R.plurals.surasSelected, count, count);
    mMode.setTitle(countString);
  }

  private class SuraAdapter extends BaseAdapter {
    protected LayoutInflater mInflater = null;
    private String[] mSuras = null;
    private SparseBooleanArray mSelectedSuras = new SparseBooleanArray();

    public SuraAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
      mSuras = context.getResources().getStringArray(R.array.sura_names);
    }

    @Override
    public int getCount() {
      return mSuras.length;
    }

    @Override
    public Object getItem(int position) {
      return mSuras[position];
    }

    public boolean isSuraSelected(int sura) {
      return mSelectedSuras.get(sura, false);
    }

    public void selectSura(int sura) {
      mSelectedSuras.put(sura, true);
      updateCount(mSelectedSuras.size());
    }

    public void unselectSura(int sura) {
      if (mSelectedSuras.get(sura, false)) {
        mSelectedSuras.delete(sura);
      }
      updateCount(mSelectedSuras.size());
    }

    public Object[] getSelectedSuras() {
      List<Integer> result = new ArrayList<Integer>();
      int size = mSelectedSuras.size();
      for (int i = 0; i < size; i++) {
        int key = mSelectedSuras.keyAt(i);
        if (mSelectedSuras.get(key, false)) {
          result.add(key);
        }
      }
      return result.toArray();
    }

    public int getSelectedCount() {
      return mSelectedSuras.size();
    }

    @Override
    public View getView(final int position,
                        View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mInflater.inflate(R.layout.sura_row, null);
      }

      boolean isSelected = isSuraSelected(position + 1);

      CheckBox tv = (CheckBox) convertView;
      tv.setChecked(isSelected);
      tv.setText(getItem(position).toString());
      tv.setBackgroundColor(isSelected ? mSelectedColor : 0);
      return convertView;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

  }
}
