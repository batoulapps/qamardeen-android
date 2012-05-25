package com.batoulapps.QamarDeen.ui.helpers;

import java.util.ArrayList;
import java.util.List;

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
   private int mElementId = -1;
   private Context mContext = null;
   private SelectorWidget mSelectorWidget = null;
   private View mDoneButton = null;
   
   public QamarSelectorHelper(Context context){
      mContext = context;
   }
   
   /**
    * shows the popup when you can only select one item
    * @param listener the listener
    * @param anchorView the view to anchor the popup from
    * @param row the selected row (used for the callback)
    * @param item an id (only used for the callback)
    * @param selected the current selected value
    * @param textArrayId the array id of the strings array
    * @param valuesId the array id of the values array
    */
   public void showPopup(OnQamarSelectionListener listener, View anchorView,
                         int row, int item, int selected, int textArrayId,
                         int valuesId){
      mElementId = item;
      List<Integer> selectedItems = new ArrayList<Integer>();
      selectedItems.add(selected);
      
      initializePopup(listener, row, textArrayId, valuesId, selectedItems);
      mSelectorWidget.setMultipleChoiceMode(false);
      mDoneButton.setVisibility(View.GONE);

      // show dropdown
      mPopupWindow.showAsDropDown(anchorView);
   }
   
   /**
    * used when there are multiple choices in the seelctor
    * @param listener the listener
    * @param anchorView anchor view to show the popup from
    * @param row the row that is clicked (used in callback)
    * @param selected the items that start off selected
    * @param textArrayId the array id of the strings array
    * @param valuesId the array id of the integer array of values
    */
   public void showMultipleChoicePopup(OnQamarSelectionListener listener,
         View anchorView, int row, List<Integer> selected, int textArrayId,
         int valuesId){
      initializePopup(listener, row, textArrayId, valuesId, selected);
      mSelectorWidget.setMultipleChoiceMode(true);
      mDoneButton.setVisibility(View.VISIBLE);

      // show dropdown
      mPopupWindow.showAsDropDown(anchorView);
   }
   
   private void initializePopup(OnQamarSelectionListener listener,
         int row, int textArrayId, int valuesId, List<Integer> selected){
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
         
         mDoneButton =  mPopupWindowView.findViewById(R.id.done_button);
         mDoneButton.setOnClickListener(mButtonClickListener);
      }
      
      // set state variables for callbacks
      mSelectedRow = row;
      mSelectionListener = listener;
      
      // get the resources that are needed
      Resources res = mContext.getResources();
      int[] values = res.getIntArray(R.array.prayer_values);
      String[] textIds = res.getStringArray(textArrayId);
      
      // initialize selector widget
      mSelectorWidget = (SelectorWidget)mPopupWindowView
            .findViewById(R.id.selector_widget);      
      mSelectorWidget.setSelectionItems(textIds, values, null, selected);
      mSelectorWidget.setItemSelectListener(this);
   }
   
   @Override
   public void itemSelected(int selection){
      dismissPopup();
      if (mSelectionListener != null && mSelectedRow >= 0){
         mSelectionListener.onItemSelected(
               mSelectedRow, mElementId, selection);
      }
      
      mSelectionListener = null;
      mSelectedRow = -1;
      mElementId = -1;
   }
   
   
   /**
    * interface used to communicate to the Fragment or Activity
    * using this helper class
    */
   public interface OnQamarSelectionListener {
      public void onItemSelected(int row, int item, int selection);
      public void onMultipleItemsSelected(int row, List<Integer> selection);
   };
   
   protected OnClickListener mButtonClickListener = new OnClickListener(){
      @Override
      public void onClick(View v) {
         if (v.getId() == R.id.cancel_button){
            // just dismiss the popup
            dismissPopup();
         }
         else if (v.getId() == R.id.done_button){
            // used for "multiple choice" mode
            List<Integer> selected = mSelectorWidget.getSelectedItems();
            if (mSelectionListener != null && mSelectedRow >= 0){
               mSelectionListener.onMultipleItemsSelected(
                     mSelectedRow, selected);
            }
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
