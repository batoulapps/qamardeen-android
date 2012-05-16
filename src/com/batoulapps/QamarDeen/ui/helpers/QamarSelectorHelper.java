package com.batoulapps.QamarDeen.ui.helpers;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.widgets.SelectorWidget;

public class QamarSelectorHelper {

   private PopupWindow mPopupWindow = null;
   private View mPopupWindowView = null;
   private Context mContext = null;
   
   public QamarSelectorHelper(Context context){
      mContext = context;
   }
   
   public void showPopup(View anchorView, int row, int textArrayId){
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
      
      String[] textIds = mContext.getResources()
            .getStringArray(textArrayId);
      SelectorWidget sw = (SelectorWidget)mPopupWindowView
            .findViewById(R.id.selector_widget);
      sw.setSelectionItems(textIds, null);
      mPopupWindow.showAsDropDown(anchorView);
   }
   
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
