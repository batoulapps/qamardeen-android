package com.batoulapps.QamarDeen.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.batoulapps.QamarDeen.R;

public class SelectorWidget extends LinearLayout {

   private Context mContext = null;
   private List<TextView> mOptionItems = null;
   
   public SelectorWidget(Context context){
      super(context);
      init(context);
   }
   
   public SelectorWidget(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }
   
   public SelectorWidget(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }
   
   private void init(Context context){
      mContext = context;
      setOrientation(HORIZONTAL);
   }
   
   public void setSelectionItems(String[] labels, int[] imageIds){
      removeAllViews();
      
      // left layout
      LinearLayout leftLayout = new LinearLayout(mContext);
      leftLayout.setOrientation(VERTICAL);

      // right layout
      LinearLayout rightLayout = new LinearLayout(mContext);
      rightLayout.setOrientation(VERTICAL);
      
      // make and add the text views
      mOptionItems = new ArrayList<TextView>();
      LinearLayout.LayoutParams textParams =
            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                  LayoutParams.WRAP_CONTENT);
      for (int i=0; i<labels.length; i++){
         TextView tv = new TextView(mContext);
         tv.setTextAppearance(mContext, R.style.popup_text_style);
         tv.setText(labels[i]);
         
         // add images to the textviews
         //tv.setCompoundDrawablesWithIntrinsicBounds(imageIds[i], 0, 0, 0);
         
         if (i % 2 == 0){
            leftLayout.addView(tv, textParams);
         }
         else {
            rightLayout.addView(tv, textParams);
         }
         mOptionItems.add(tv);
      }

      // add layouts to view
      LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
      params.weight = 1.0f;
      addView(leftLayout, params);
      addView(rightLayout, params);
      
      // request layout
      requestLayout();
   }
}
