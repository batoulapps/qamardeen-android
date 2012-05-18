package com.batoulapps.QamarDeen.ui.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.widgets.SelectorWidget;
import com.batoulapps.QamarDeen.ui.widgets.SelectorWidget.ItemSelectListener;

public class QamarSelectorHelper implements ItemSelectListener {

   private PopupWindow mPopupWindow = null;
   private View mPopupWindowView = null;
   private OnQamarSelectionListener mSelectionListener;
   private int mSelectedRow = -1;
   private int mSelectedItem = -1;
   private Context mContext = null;
   
   public QamarSelectorHelper(Context context){
      mContext = context;
   }
   
   public void showPopup(OnQamarSelectionListener listener, View anchorView,
                         int row, int item, int textArrayId, int valuesId){
      if (mPopupWindow == null || mPopupWindowView == null){
         LayoutInflater inflater = (LayoutInflater)mContext
               .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         mPopupWindowView = inflater.inflate(R.layout.popup_layout, null);
         mPopupWindow = new PopupWindow(mPopupWindowView,
               LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
         Drawable drawable = mContext.getResources()
               .getDrawable(R.color.popup_background);
         mPopupWindow.setBackgroundDrawable(drawable);
         
         View button = mPopupWindowView.findViewById(R.id.cancel_button);
         button.setOnClickListener(mButtonClickListener);
      }
      
      // set state variables for callbacks
      mSelectedRow = row;
      mSelectedItem = item;
      mSelectionListener = listener;
      
      // get the resources that are needed
      Resources res = mContext.getResources();
      int[] values = res.getIntArray(R.array.prayer_values);
      String[] textIds = res.getStringArray(textArrayId);
      
      // initialize selector widget
      SelectorWidget sw = (SelectorWidget)mPopupWindowView
            .findViewById(R.id.selector_widget);      
      sw.setSelectionItems(textIds, values, null);
      sw.setItemSelectListener(this);
      
      // show dropdown
      mPopupWindow.showAsDropDown(anchorView);
   }
   
   @Override
   public void itemSelected(int selection){
      dismissPopup();
      if (mSelectionListener != null && mSelectedRow >= 0){
         mSelectionListener.onItemSelected(
               mSelectedRow, mSelectedItem, selection);
      }
      
      mSelectionListener = null;
      mSelectedRow = -1;
      mSelectedItem = -1;
   }
   
   
   /**
    * interface used to communicate to the Fragment or Activity
    * using this helper class
    */
   public interface OnQamarSelectionListener {
      public void onItemSelected(int row, int item, int selection);
   };
   
   protected OnClickListener mButtonClickListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         if (v.getId() == R.id.cancel_button){
            dismissPopup();
         }
      }
   };
   
   public void dismissPopup(){
      if (mPopupWindow != null){
         mPopupWindow.dismiss();
      }
   }
}
