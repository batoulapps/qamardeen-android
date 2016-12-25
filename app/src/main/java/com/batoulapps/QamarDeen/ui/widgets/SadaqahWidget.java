package com.batoulapps.QamarDeen.ui.widgets;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.batoulapps.QamarDeen.R;

public class SadaqahWidget extends LinearLayout {

   private Context mContext = null;
   
   public static int[] SADAQAH_SELECTOR_IMAGES = new int[]{
      R.drawable.sadaqah_icon_money, R.drawable.sadaqah_icon_effort,
      R.drawable.sadaqah_icon_food, R.drawable.sadaqah_icon_clothes,
      R.drawable.sadaqah_icon_smile, R.drawable.sadaqah_icon_other
   };
   
   public SadaqahWidget(Context context){
      super(context);
      init(context);
   }
   
   public SadaqahWidget(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }
   
   public SadaqahWidget(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }
   
   private void init(Context context){
      mContext = context;
   }
   
   private void addPlaceHolderView(){
      ImageView emptyImage = new ImageView(mContext);
      addView(emptyImage, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
   }
   
   private void addSadaqahView(int sadaqaType){
      ImageView image = new ImageView(mContext);
      image.setScaleType(ScaleType.CENTER);
      image.setImageResource(SADAQAH_SELECTOR_IMAGES[sadaqaType]);
      addView(image, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
   }
   
   /**
    * set the sadaqat for the widget
    * @param sadaqat a list of sadaqat, see 9:60
    */
   public void setSadaqat(List<Integer> sadaqat){
      removeAllViews();
      if (sadaqat == null || sadaqat.size() == 0){
         addPlaceHolderView();
         return;
      }
      
      for (Integer sadaqah : sadaqat){
         addSadaqahView(sadaqah);
      }
   }
   
}
