package com.batoulapps.QamarDeen.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.batoulapps.QamarDeen.R;

public class StatisticsWidget extends RelativeLayout {

   private Context mContext;
   private Resources mResources;
   private int mCountColor = 0;
   private int mStatsMargin = 0;
   private int[] mLabelStrings = new int[]{ R.array.prayer_options_m };
   private int[] mLabelValues = new int[]{ R.array.prayer_values };

   public StatisticsWidget(Context context){
      super(context);
      init(context);
   }

   public StatisticsWidget(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }

   public StatisticsWidget(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }

   private void init(Context context){
      mContext = context;
      mResources = mContext.getResources();
      mCountColor = mResources.getColor(R.color.stats_color);
      mStatsMargin = mResources.getDimensionPixelSize(R.dimen.stats_margin);
      showProgressView();
   }

   public void showProgressView(){
      removeAllViews();
      ProgressBar progressBar = new ProgressBar(mContext);
      progressBar.setIndeterminate(true);
      RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
              ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      lp.addRule(RelativeLayout.CENTER_IN_PARENT);
      addView(progressBar, lp);
   }

   public void showStats(int tab, SparseArray<Integer> statistics){
      removeAllViews();

      tab = 0;
      String[] labels = mResources.getStringArray(mLabelStrings[tab]);
      int[] options = mResources.getIntArray(mLabelValues[tab]);

      int total = 0;
      for (int i = 0; i < options.length; i++){
         int key = options[i];
         total += statistics.get(key, 0);
      }

      TextView[] views = new TextView[labels.length];
      for (int i = 0; i < labels.length; i++){
         SpannableStringBuilder builder = new SpannableStringBuilder();
         builder.append(labels[i]);
         builder.append(" ");

         int start = builder.length();
         int key = options[i];
         int score = statistics.get(key, 0);
         builder.append("(" + score + ")");
         builder.setSpan(new ForegroundColorSpan(mCountColor),
                 start, builder.length(), 0);
         builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), 0);

         builder.append(" ");

         int percent = (int)(100.0 * score / total);
         start = builder.length();
         builder.append(percent + "%");
         builder.setSpan(new ForegroundColorSpan(Color.LTGRAY),
                 start, builder.length(), 0);

         Log.d("are", "total: " + total + " size " + statistics.size() + " score " + score + " % " + percent);
         TextView textView = new TextView(mContext);
         textView.setText(builder);
         views[i] = textView;
      }

      LinearLayout container = new LinearLayout(mContext);
      container.setOrientation(LinearLayout.HORIZONTAL);

      LinearLayout leftLayout = new LinearLayout(mContext);
      leftLayout.setOrientation(LinearLayout.VERTICAL);

      LinearLayout rightLayout = new LinearLayout(mContext);
      rightLayout.setOrientation(LinearLayout.VERTICAL);

      for (int i = 0; i<views.length; i++){
         if (i % 2 == 0){
            leftLayout.addView(views[i], ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
         }
         else {
            rightLayout.addView(views[i], ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
         }
      }

      LinearLayout.LayoutParams params = new
              LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
      container.addView(leftLayout, params);
      container.addView(rightLayout, params);

      RelativeLayout.LayoutParams lp = new LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
      lp.setMargins(mStatsMargin, mStatsMargin, mStatsMargin, mStatsMargin);
      addView(container, lp);
   }

}
