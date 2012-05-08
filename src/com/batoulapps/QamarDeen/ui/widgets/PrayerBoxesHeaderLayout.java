package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import com.batoulapps.QamarDeen.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrayerBoxesHeaderLayout extends LinearLayout {
   private Context mContext = null;
   private List<TextView> mLabels = null;
   private boolean mIsExtendedMode = false;
   
   private static int DUHA = 1;
   private static int QIYYAM = 6;
   
   private int[] mPrayerNames = new int[]{
         R.string.fajr, R.string.dhuhr, R.string.asr,
         R.string.maghrib, R.string.isha
   };
   
   private int[] mExtendedPrayerNames = new int[]{
         R.string.fajr, R.string.duha, R.string.dhuhr,
         R.string.asr, R.string.maghrib, R.string.isha,
         R.string.qiyyam
   };

   public PrayerBoxesHeaderLayout(Context context) {
      super(context);
      init(context);
   }
   
   public PrayerBoxesHeaderLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }
   
   public PrayerBoxesHeaderLayout(Context context,
         AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(context);
   }
   
   private void init(Context context){
      mContext = context;
      mLabels = new ArrayList<TextView>();
      for (int i=0; i<5; i++){
         TextView label = getLabelView();
         addView(label, i);
         mLabels.add(label);
      }
   }
   
   private TextView getLabelView(){
      TextView label = new TextView(mContext);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
            LayoutParams.MATCH_PARENT, 1);
      label.setLayoutParams(lp);
      label.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
      return label;
   }
   
   public void setExtendedMode(boolean extendedMode){
      if (extendedMode && !mIsExtendedMode){
         // add duha after fajr
         TextView duha = getLabelView();
         addView(duha, 1);
         mLabels.add(DUHA, duha);
         
         // add qiyyam to the end
         TextView qiyyam = getLabelView();
         addView(qiyyam);
         mLabels.add(qiyyam);
         
         // set extend mode
         mIsExtendedMode = true;
      }
      else if (!extendedMode && mIsExtendedMode){
         // remove qiyyam
         TextView tahajjud = mLabels.remove(QIYYAM);
         removeView(tahajjud);
         
         // remove duha
         TextView shuruq = mLabels.remove(DUHA);
         removeView(shuruq);
       
         // unset extended mode
         mIsExtendedMode = false;
      }      
   }
   
   public void showSalahLabels(){
      int size = mLabels.size();
      int style = R.style.prayer_hdr;
      if (mIsExtendedMode){ style = R.style.prayer_extended_hdr; }
      int[] resources = mIsExtendedMode? mExtendedPrayerNames : mPrayerNames;
      for (int i=0; i<size; i++){
         TextView label = mLabels.get(i);
         label.setText(resources[i]);
         label.setTextAppearance(mContext, style);
      }
   }
}
