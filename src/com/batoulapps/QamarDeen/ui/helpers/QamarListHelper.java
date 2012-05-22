package com.batoulapps.QamarDeen.ui.helpers;

import android.app.Activity;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.ui.widgets.PinnedHeaderListView;

public class QamarListHelper {

   /**
    * convenience method to setup the standard QamarList.
    * sets up the footer and pinned headers.  sets the adapter for
    * the listview and returns the height of the header
    * 
    * @param inflater the layout inflater
    * @param activity the activity (needed for getting display info)
    * @param list the list view (subclass of pinned header list view)
    * @param adapter the list adapter (qamar list adapter subclass)
    * @param headerLayout the id of the header layout
    * @return the height of the header
    */
   public static int setupQamarList(LayoutInflater inflater,
         Activity activity, PinnedHeaderListView list,
         QamarListAdapter adapter, int headerLayout){
      
      // read some dimensions
      Resources res = activity.getResources();
      int headerHeight = res.getDimensionPixelSize(R.dimen.header_height);
      int itemHeight = res.getDimensionPixelSize(R.dimen.list_item_height);
      int abHeight = 
            res.getDimensionPixelSize(R.dimen.abs__action_bar_default_height);
      
      // set the footer
      DisplayMetrics metrics = new DisplayMetrics();
      activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      
      int footerHeight = metrics.heightPixels -
            ((2 * abHeight) + itemHeight + headerHeight);
      View footer = inflater.inflate(R.layout.list_footer, null, false);
      AbsListView.LayoutParams ll = new AbsListView.LayoutParams(
            LayoutParams.MATCH_PARENT, footerHeight);
      footer.setLayoutParams(ll);
      list.addFooterView(footer);
      
      // set the adapter
      list.setAdapter(adapter);
      
      // set pinned header
      list.setPinnedHeaderView(inflater.inflate(headerLayout, list, false));
      list.setOnScrollListener(adapter);
      list.setDividerHeight(0);
      
      return headerHeight;
   }
}
