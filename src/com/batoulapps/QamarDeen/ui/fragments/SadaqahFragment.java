package com.batoulapps.QamarDeen.ui.fragments;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.helpers.QamarListAdapter;
import com.batoulapps.QamarDeen.ui.helpers.QamarListHelper;
import com.batoulapps.QamarDeen.ui.helpers.QamarSelectorHelper;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;

public class SadaqahFragment extends SherlockFragment {

   private PinnedHeaderListView mListView = null;
   private SadaqahListAdapter mListAdapter = null;
   private QamarSelectorHelper mPopupHelper = null;
   
   public static SadaqahFragment newInstance(){
      return new SadaqahFragment();
   }
   
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
      mListAdapter = new SadaqahListAdapter(activity);
      
      // setup the list and adapter
      QamarListHelper.setupQamarList(inflater, activity,
            mListView, mListAdapter, R.layout.sadaqah_hdr);
      
      mPopupHelper = new QamarSelectorHelper(activity);
      return view;
   }
   
   @Override
   public void onPause() {
      if (mPopupHelper != null){
         mPopupHelper.dismissPopup();
      }
      super.onPause();
   }
   
   private class SadaqahListAdapter extends QamarListAdapter {
      public SadaqahListAdapter(Context context){
         super(context);
      }
      
      @Override
      public void requestData(Long maxDate, Long minDate){
      }
      
      @Override
      public View getView(int position, View convertView, ViewGroup parent){
         ViewHolder holder;
         Date date = (Date)getItem(position);
         
         if (convertView == null){
            ViewHolder h = new ViewHolder();
            convertView = mInflater.inflate(R.layout.sadaqah_layout, null);
            populateDayInfoInHolder(h, convertView, R.id.sadaqah_hdr);
            
            holder = h;
            convertView.setTag(holder);
         }
         else { holder = (ViewHolder)convertView.getTag(); }
         
         // initialize generic row stuff (date, header, etc)
         initializeRow(holder, date, position);
         
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
      }
   }
}
