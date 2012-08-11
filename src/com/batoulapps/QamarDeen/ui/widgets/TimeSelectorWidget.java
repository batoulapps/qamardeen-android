package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.batoulapps.QamarDeen.R;

public class TimeSelectorWidget extends LinearLayout {

   private Context mContext;
   private int mSelectedPosition;
   private String[] mGraphDurations;
   private List<TextView> mTextViews;
   private int mSelectedColor;
   private TimeSelectedListener mTimeSelectedListener;

   public TimeSelectorWidget(Context context){
      super(context);
      init(context);
   }

   public TimeSelectorWidget(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }

   public TimeSelectorWidget(Context context,
                             AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }

   private void init(Context context){
      mContext = context;
      mSelectedPosition = 1;
      Resources resources = context.getResources();
      mGraphDurations = resources.getStringArray(
              R.array.graph_durations);
      mSelectedColor = resources.getColor(R.color.stats_selected_date);
      mTextViews = new ArrayList<TextView>();
      setOrientation(HORIZONTAL);

      LinearLayout.LayoutParams lp = new LayoutParams(0,
              ViewGroup.LayoutParams.MATCH_PARENT, 1);
      for (int i = 0; i < mGraphDurations.length; i++){
         TextView textview = new TextView(mContext);
         textview.setText(mGraphDurations[i]);
         textview.setGravity(Gravity.CENTER);
         textview.setTextAppearance(mContext,
                 R.style.graph_stats_datepick_text);
         if (mSelectedPosition == i){
            textview.setBackgroundColor(mSelectedColor);
         }
         textview.setOnClickListener(mOnClickListener);
         textview.setTag(Integer.valueOf(i));
         addView(textview, lp);
         mTextViews.add(textview);
      }
   }

   private void updateSelectedItem(int newSelction){
      if (newSelction != mSelectedPosition){
         TextView currentSelection = mTextViews.get(mSelectedPosition);
         currentSelection.setBackgroundDrawable(null);
         mSelectedPosition = newSelction;
         currentSelection = mTextViews.get(mSelectedPosition);
         currentSelection.setBackgroundColor(mSelectedColor);
      }
   }

   public OnClickListener mOnClickListener = new OnClickListener() {
      @Override
      public void onClick(View view) {
         Object tag = view.getTag();
         if (tag instanceof Integer){
            Integer tagValue = (Integer)tag;
            if (tagValue != null && mTimeSelectedListener != null){
               mTimeSelectedListener.timeSelected(tagValue);
               updateSelectedItem(tagValue);
            }
         }
      }
   };

   public void setTimeSelectedListener(TimeSelectedListener listener){
      mTimeSelectedListener = listener;
   }

   public interface TimeSelectedListener {
      public void timeSelected(int position);
   }
}
