package com.batoulapps.QamarDeen.ui.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarConstants;

public class QuranSelectorPopupHelper {
   
   private PopupWindow mPopupWindow = null;
   private View mPopupWindowView = null;
   private Context mContext = null;
   private int mCurrentRow = -1;
   private ListView mSuraList = null;
   private ListView mAyahList = null;
   private SuraAdapter mSuraAdapter = null;
   private NumericAdapter mAyahAdapter = null;
   private OnQuranSelectionListener mQuranSelectionListener = null;
   
   public QuranSelectorPopupHelper(Context context){
      mContext = context;
   }
   
   public void showPopup(OnQuranSelectionListener listener,
         View anchorView, int row,
         int selectedSura, int selectedAyah){
      if (mPopupWindow == null || mPopupWindowView == null){
         LayoutInflater inflater = (LayoutInflater)mContext
               .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mPopupWindowView = inflater.inflate(
               R.layout.quran_selector_popup, null);
         mPopupWindow = new PopupWindow(mPopupWindowView,
               LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         Drawable drawable = mContext.getResources()
               .getDrawable(R.color.popup_background);
         mPopupWindow.setBackgroundDrawable(drawable);
         
         View button = mPopupWindowView.findViewById(R.id.cancel_button);
         button.setOnClickListener(mButtonClickListener);
         
         button =  mPopupWindowView.findViewById(R.id.none_button);
         button.setOnClickListener(mButtonClickListener);
         
         button =  mPopupWindowView.findViewById(R.id.done_button);
         button.setOnClickListener(mButtonClickListener);
      }
      
      mCurrentRow = row;
      mQuranSelectionListener = listener;
      if (selectedSura < 1){ selectedSura = 1; }
      if (selectedAyah < 1){ selectedAyah = 1; }
      
      mSuraList = (ListView)mPopupWindowView
            .findViewById(R.id.sura_list);
      mSuraList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      mSuraAdapter = new SuraAdapter(mContext);
      mSuraAdapter.setSelectedRow(null, selectedSura - 1);
      mSuraList.setAdapter(mSuraAdapter);
      mSuraList.setSelectionFromTop(selectedSura -1, 0);

      mAyahList = (ListView)mPopupWindowView
            .findViewById(R.id.ayah_list);
      mAyahAdapter = new NumericAdapter(mContext);
      mAyahList.setAdapter(mAyahAdapter);
      updateAyahsForSuraPosition(selectedSura - 1, selectedAyah - 1);   

      // show dropdown
      mPopupWindow.setFocusable(true);
      mPopupWindow.showAsDropDown(anchorView);
   }
   
   private void updateAyahsForSuraPosition(int position){
      updateAyahsForSuraPosition(position, 0);
   }
   
   private void updateAyahsForSuraPosition(int position, int ayahPosition){
      int count = QamarConstants.SURA_NUM_AYAHS[position];
      if (ayahPosition >= count){ ayahPosition = 0; }
      mAyahAdapter.setCount(count);
      mAyahAdapter.setSelectedRow(null, ayahPosition);
      mAyahAdapter.notifyDataSetChanged();
      mAyahList.setSelectionFromTop(ayahPosition, 0);
   }
   
   private abstract class StringAdapter extends BaseAdapter {
      protected int mSelectedRow = -1;
      protected LayoutInflater mInflater = null;
      
      public StringAdapter(Context context){
         mInflater = LayoutInflater.from(context);
      }
      
      public int getSelectedRow(){ return mSelectedRow; }
      
      public void setSelectedRow(ListView listview, int row){
         int previousRow = mSelectedRow;
         mSelectedRow = row;
         
         if ((previousRow != -1) && (listview != null)){
            int start = listview.getFirstVisiblePosition();
            int end = listview.getLastVisiblePosition();

            if (previousRow >= start && previousRow <= end){
               View view = listview.getChildAt(previousRow - start);
               if (view != null){
                  getView(previousRow, view, listview);
               }
            }
         }
      }
      
      @Override
      public long getItemId(int position) { return position; }

      @Override
      public View getView(final int position,
            View convertView, ViewGroup parent) {
         if (convertView == null){
            convertView = mInflater.inflate(R.layout.sura_popup_row, null);
         }
         ((TextView)convertView).setText(getItem(position) + "");
         
         int color = Color.TRANSPARENT;
         if (mSelectedRow == position){
            color = 0xaaff0000;
         }
         convertView.setBackgroundColor(color);
         return convertView;
      }
   }
   
   private class SuraAdapter extends StringAdapter {
      private String[] mSuras = null;
      
      public SuraAdapter(Context context){
         super(context);
         mSuras = context.getResources().getStringArray(R.array.sura_names);
      }
      
      @Override
      public int getCount() { return mSuras.length; }

      @Override
      public Object getItem(int position) { return mSuras[position]; }
      
      @Override
      public View getView(final int position,
            View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);
         v.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
               v.setBackgroundColor(0xaaff0000);
               setSelectedRow(mSuraList, position);
               updateAyahsForSuraPosition(position);
            }
         });
         return v;
      }
   }
   
   private class NumericAdapter extends StringAdapter {
      private int mCount = 0;
      
      public NumericAdapter(Context context){
         super(context);
      }
      
      public void setCount(int count){ mCount = count; }
      
      @Override
      public int getCount() {
         return mCount;
      }

      @Override
      public Object getItem(int position) { return position + 1;  }

      @Override
      public View getView(final int position,
            View convertView, ViewGroup parent) {
         View v = super.getView(position, convertView, parent);
         
         v.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) { 
               v.setBackgroundColor(0xaaff0000);
               setSelectedRow(mAyahList, position);
            }
         });
         
         return v;
      }
   }
   
   /**
    * interface used to communicate to the Fragment or Activity
    * using this helper class
    */
   public interface OnQuranSelectionListener {
      public void onSuraAyahSelected(int row, int sura, int ayah);
      public void onNoneSelected(int row);
   };
   
   protected OnClickListener mButtonClickListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         if (v.getId() == R.id.cancel_button){
            // just dismiss the popup
            dismissPopup();
         }
         else if (v.getId() == R.id.none_button){
            if (mQuranSelectionListener != null){
               mQuranSelectionListener.onNoneSelected(mCurrentRow);
            }
            dismissPopup();
         }
         else if (v.getId() == R.id.done_button){
            if (mQuranSelectionListener != null){
               mQuranSelectionListener.onSuraAyahSelected(mCurrentRow,
                     mSuraAdapter.getSelectedRow() + 1,
                     mAyahAdapter.getSelectedRow() + 1);
            }
            dismissPopup();
         }
      }
   };

   public boolean isShowing(){
      if (mPopupWindow != null){
         return mPopupWindow.isShowing();
      }
      return false;
   }

   public void dismissPopup(){
      if (mPopupWindow != null){
         mPopupWindow.dismiss();
      }
   }
}
