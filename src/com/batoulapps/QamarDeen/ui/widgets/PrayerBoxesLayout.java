package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarConstants;
import com.batoulapps.QamarDeen.data.QamarConstants.Prayers;

public class PrayerBoxesLayout extends LinearLayout {
   
   private Context mContext = null;
   private List<ImageView> mImages = null;
   private boolean mIsExtendedMode = false; 
   private SalahClickListener mSalahClickListener = null;

   public PrayerBoxesLayout(Context context){
      super(context);
      init(context);
   }
   
   public PrayerBoxesLayout(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }
   
   public PrayerBoxesLayout(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }
   
   private void init(Context context){
      mContext = context;
      mImages = new ArrayList<ImageView>();
      for (int i=0; i<5; i++){
         ImageView image = getImageView(QamarConstants.PRAYER_LIST[i]);
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
      image.setOnClickListener(mImageClickListener);
      return image;
   }
      
   protected OnClickListener mImageClickListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         if (mSalahClickListener != null){
            Integer salah = (Integer)v.getTag();
            mSalahClickListener.onSalahClicked(v, salah);
         }
      }
   };
   
   /**
    * salah click listener
    */
   public interface SalahClickListener {
      public void onSalahClicked(View view, int salah);
   }
   
   /**
    * handle the click of a salah box
    * @param listener a salah click listener
    */
   public void setSalahClickListener(SalahClickListener listener){
      mSalahClickListener = listener;
   }
   
   /**
    * sets the prayer squares given an array of values
    * @param items an array of 7 items with the values
    * for each salah for that day.
    */
   public void setPrayerSquares(int[] items){
      for (int i = 0; i < items.length; i++){
         togglePrayerSquare(i, items[i]);
      }
   }
   
   /**
    * set all the prayer squares to empty/unselected
    */
   public void clearPrayerSquares(){
      for (ImageView image : mImages){
         Integer tag = (Integer)image.getTag();
         togglePrayerSquare(tag, 0);
      }
   }
   
   /**
    * sets the salah square's value
    * @param salah the salah
    * @param type the type to set that prayer to
    */
   public void togglePrayerSquare(int salah, int type){
      View salahSquare = findViewWithTag(salah);
      if (salahSquare != null){
         if (type > 0){
            // set the square to the right resource
            int[] resources = new int[]{ Color.RED, Color.GREEN, Color.BLUE, 
                  Color.CYAN, Color.YELLOW, Color.MAGENTA };
            salahSquare.setBackgroundColor(resources[type-1]);
         }
         else {
            // type is 0, so we want to reset this square
            int res = R.color.transparent;
            int salahIndex = mIsExtendedMode? salah :
               (salah == 0? salah : salah-1);
            if (salahIndex % 2 != 0){ res = R.color.shaded_column_color; }
            
            salahSquare.setBackgroundResource(res);
         }
      }
   }
   
   /**
    * toggles whether or not we should show duha and qiyyam or not
    * @param extendedMode whether or not to show duha and qiyyam
    */
   public void setExtendedMode(boolean extendedMode){
      if (extendedMode && !mIsExtendedMode){
         // add duha after fajr
         ImageView duha = getImageView(Prayers.DUHA);
         addView(duha, 1);
         mImages.add(Prayers.DUHA, duha);
         
         // add qiyyam to the end
         ImageView qiyyam = getImageView(Prayers.QIYYAM);
         addView(qiyyam);
         mImages.add(qiyyam);
         
         // update patterns
         setBackgrounds();
      }
      else if (!extendedMode && mIsExtendedMode){
         // remove qiyyam
         ImageView tahajjud = mImages.remove(Prayers.QIYYAM);
         tahajjud.setBackgroundDrawable(null);
         tahajjud.setImageDrawable(null);
         removeView(tahajjud);
         
         // remove duha
         ImageView shuruq = mImages.remove(Prayers.DUHA);
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
