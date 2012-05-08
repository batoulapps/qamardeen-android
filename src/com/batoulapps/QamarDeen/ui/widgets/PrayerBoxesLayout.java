package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.batoulapps.QamarDeen.R;

public class PrayerBoxesLayout extends LinearLayout {
   
   private Context mContext = null;
   private List<ImageView> mImages = null;
   private boolean mIsExtendedMode = false;
   
   private static int DUHA = 1;
   private static int QIYYAM = 6;

   public PrayerBoxesLayout(Context context) {
      super(context);
      init(context);
   }
   
   public PrayerBoxesLayout(Context context, AttributeSet attrs) {
      super(context, attrs);
      init(context);
   }
   
   public PrayerBoxesLayout(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      init(context);
   }
   
   private void init(Context context){
      mContext = context;
      mImages = new ArrayList<ImageView>();
      for (int i=0; i<5; i++){
         ImageView image = getImageView(i);
         addView(image, i);
         mImages.add(image);
      }
      
      setBackgrounds();
   }
   
   private ImageView getImageView(int tag){
      ImageView image = new ImageView(mContext);
      image.setTag(tag);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
            LayoutParams.MATCH_PARENT, 1);
      image.setLayoutParams(lp);
      return image;
   }
   
   public void setExtendedMode(boolean extendedMode){
      if (extendedMode && !mIsExtendedMode){
         // add duha after fajr
         ImageView duha = getImageView(DUHA);
         addView(duha, 1);
         mImages.add(DUHA, duha);
         
         // add qiyyam to the end
         ImageView qiyyam = getImageView(QIYYAM);
         addView(qiyyam);
         mImages.add(qiyyam);
         
         // update patterns
         setBackgrounds();
      }
      else if (!extendedMode && mIsExtendedMode){
         // remove qiyyam
         ImageView tahajjud = mImages.remove(QIYYAM);
         tahajjud.setBackgroundDrawable(null);
         tahajjud.setImageDrawable(null);
         removeView(tahajjud);
         
         // remove duha
         ImageView shuruq = mImages.remove(DUHA);
         shuruq.setBackgroundDrawable(null);
         shuruq.setImageDrawable(null);
         removeView(shuruq);     
         
         // update patterns
         setBackgrounds();
      }
      
      mIsExtendedMode = extendedMode;
   }
   
   private void setBackgrounds(){
      int i = 0;
      for (ImageView image : mImages){
         int resource = R.color.transparent;
         if (i % 2 != 0){
             resource = R.color.shaded_column_color;
         }
         image.setBackgroundResource(resource);
         i++;
      }
   }

}
