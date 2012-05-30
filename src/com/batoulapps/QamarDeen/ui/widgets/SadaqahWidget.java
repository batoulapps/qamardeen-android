package com.batoulapps.QamarDeen.ui.widgets;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.batoulapps.QamarDeen.R;

public class SadaqahWidget extends LinearLayout {

   private Context mContext = null;
   
   private int[] mResources = new int[]{ Color.RED, Color.GREEN, Color.BLUE, 
         Color.CYAN, Color.YELLOW, Color.MAGENTA };
   
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
      //image.setImageResource(mResources[sadaqaType]);
      //addView(image, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

      // when we get images, we remove these 3 lines and restore the top 2
      image.setBackgroundColor(mResources[sadaqaType]);
      int size = mContext.getResources().getDimensionPixelSize(
            R.dimen.list_item_height);
      addView(image, size, size);
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
