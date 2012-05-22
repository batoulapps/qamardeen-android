package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class SadaqahWidget extends LinearLayout {

   private Context mContext = null;
   private List<ImageView> mImages = null;
   private List<Integer> mValues = null;

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
      mImages = new ArrayList<ImageView>();
      mValues = new ArrayList<Integer>();
      
      ImageView emptyImage = new ImageView(mContext);
      addView(emptyImage, LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
      mImages.add(emptyImage);
      mValues.add(0);
   }
   
}
