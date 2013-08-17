package com.batoulapps.QamarDeen.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.batoulapps.QamarDeen.R;

import java.util.ArrayList;
import java.util.List;

public class SelectorWidget extends LinearLayout {

   private Context mContext = null;
   private List<TextView> mOptionItems = null;
   private ItemSelectListener mItemSelectListener = null;
   private List<Integer> mSelectedItems = null;
   private boolean mIsMultipleChoiceMode = false;
   
   private int[] mTags = null;
   private int[] mImageIds = null;
   private int[] mSelectedStateImageIds = null;
   private boolean mUseCustomImageIdsOnSelect = false;
   
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
   
   protected OnClickListener mOnClickListener = new OnClickListener(){    
      @Override
      public void onClick(View v) {
         if (mItemSelectListener != null){
            int item = (Integer)v.getTag();
            if (mIsMultipleChoiceMode){
               Integer boxedItem = Integer.valueOf(item);
               if (mSelectedItems.contains(boxedItem)){
                  mSelectedItems.remove(boxedItem);
                  if (mUseCustomImageIdsOnSelect){
                     int i = getItemIndexFromTag(item);
                     
                     if (i < mTags.length){
                        // update the drawable
                        ((TextView)v).setCompoundDrawablesWithIntrinsicBounds(
                              mImageIds[i], 0, 0, 0);
                     }
                  }
               }
               else {
                  mSelectedItems.add(boxedItem);
                  if (mUseCustomImageIdsOnSelect){
                     int i = getItemIndexFromTag(item);
                     
                     if (i < mTags.length){
                        // update the drawable
                        ((TextView)v).setCompoundDrawablesWithIntrinsicBounds(
                              mSelectedStateImageIds[i], 0, 0, 0);
                     }
                  }
               }
            }
            else {
               mItemSelectListener.itemSelected(item);
            }
         }
      }
   };
   
   public interface ItemSelectListener {
      public void itemSelected(int item);
   }
   
   public void setItemSelectListener(ItemSelectListener listener){
      mItemSelectListener = listener;
   }
   
   public void setMultipleChoiceMode(boolean multipleChoiceMode){
      mIsMultipleChoiceMode = multipleChoiceMode;
   }
   
   public List<Integer> getSelectedItems(){ return mSelectedItems; }
   
   /**
    * get the index of a view based on its tag
    * @param tag the tag
    * @return the index of the items for drawable lookup purposes
    */
   private int getItemIndexFromTag(int tag){
      int i = 0;
      for (i = 0; i<mTags.length; i++){
         if (mTags[i] == tag){ break; }
      }
      return i;
   }
   
   public void setSelectionItems(String[] labels, int[] tags,
         int[] imageIds, List<Integer> selectedItems,
         int[] selectedStateImageIds, boolean[] enabledStates){
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
         tv.setTag(tags[i]);
         tv.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);
         
         if (selectedStateImageIds == null){
            // set default background
            tv.setBackgroundResource(R.drawable.popup_selector_bg);
         }
         
         int imageId = imageIds[i];
         if (selectedItems != null && selectedItems.contains(tags[i])){
            // this item should be selected
            tv.setSelected(true);
            if (selectedStateImageIds != null){
               imageId = selectedStateImageIds[i];
            }
         }
         
         // add images to the textviews
         if (imageIds != null){
            tv.setCompoundDrawablesWithIntrinsicBounds(imageId, 0, 0, 0);
         }

         if (enabledStates != null){
            if (enabledStates[i]){ tv.setEnabled(true); }
            else {
               tv.setEnabled(false);
               tv.setTextColor(Color.GRAY);
            }
         }
         
         // set button click listener
         tv.setOnClickListener(mOnClickListener);
         
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
      
      // set the selected items (useful for multi-mode)
      mSelectedItems = selectedItems;
      if (mSelectedItems == null){
         mSelectedItems = new ArrayList<Integer>();
      }
      
      // save resource information
      mTags = tags;
      mImageIds = imageIds;
      mUseCustomImageIdsOnSelect = false;
      mSelectedStateImageIds = selectedStateImageIds;
      if (selectedStateImageIds != null){
         mUseCustomImageIdsOnSelect = true;
      }
      
      // request layout
      requestLayout();
   }
}
