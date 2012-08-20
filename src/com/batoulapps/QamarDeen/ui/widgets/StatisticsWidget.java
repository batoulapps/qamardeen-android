package com.batoulapps.QamarDeen.ui.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.batoulapps.QamarDeen.QamarGraphActivity;
import com.batoulapps.QamarDeen.R;
import com.batoulapps.QamarDeen.data.QamarConstants;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatisticsWidget extends RelativeLayout {

   private Context mContext;
   private Resources mResources;
   private int mCountColor = 0;
   private int mStatsMargin = 0;
   private int[] mLabelStrings = new int[]{ R.array.prayer_options_m,
           0, R.array.charity_options, R.array.fasting_options,
           R.array.overview_options };
   private int[] mLabelValues = new int[]{ R.array.prayer_values,
           0, R.array.charity_values, R.array.fasting_values,
           R.array.overview_values };
   private int[] mSummaryStrings = new int[]{ 0, R.array.quran_summary,
           R.array.sadaqah_summary, R.array.fasting_summary, 0 };
   private NumberFormat mNumberFormatter;

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
      updateLanguage();
      showProgressView();
   }

   private void updateLanguage(){
      SharedPreferences prefs =
              PreferenceManager.getDefaultSharedPreferences(mContext);
      boolean isArabic = prefs.getBoolean(
              QamarConstants.PreferenceKeys.USE_ARABIC, false);
      isArabic = isArabic || "ar".equals(Locale.getDefault().getLanguage());

      if (isArabic){
         mNumberFormatter = NumberFormat.getIntegerInstance(
                 new Locale("ar"));
      }
      else {
         mNumberFormatter = NumberFormat.getIntegerInstance();
      }
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

   public void showStats(int tab, SparseArray<Integer> statistics,
                         int totalDays){
      removeAllViews();

      String[] labels = null;
      if (mLabelStrings[tab] > 0){
         labels = mResources.getStringArray(mLabelStrings[tab]);
      }

      int[] options = null;
      if (mLabelValues[tab] > 0){
         options = mResources.getIntArray(mLabelValues[tab]);
      }

      String[] summaries = null;
      if (mSummaryStrings[tab] > 0){
         summaries = mResources.getStringArray(mSummaryStrings[tab]);
      }

      int total = 0;
      if (options != null){
         for (int i = 0; i < options.length; i++){
            int key = options[i];
            total += statistics.get(key, 0);
         }
      }

      boolean showPercentage = (tab != QamarGraphActivity.GRAPH_OVERVIEW_TAB);

      List<TextView> textViews = new ArrayList<TextView>();
      if (summaries != null){
         int key = QamarConstants.TOTAL_ACTIVE_DAYS;
         int days = statistics.get(key, 0);
         int percent =  (int)(100.0 * days / totalDays);
         TextView textView = makeTextView(summaries[0], days,
                                          percent, showPercentage);
         textViews.add(textView);

         percent =  (int)(100.0 * (totalDays - days) / totalDays);
         textView = makeTextView(summaries[1], totalDays - days, percent,
                                 showPercentage);
         textViews.add(textView);
      }

      if (labels != null && options != null){
         for (int i = 0; i < labels.length; i++){
            int key = options[i];
            int days = statistics.get(key, 0);
            int percent =  (int)(100.0 * days / total);
            TextView textView = makeTextView(labels[i], days, percent,
                    showPercentage);
            textViews.add(textView);
         }
      }

      if (tab == QamarGraphActivity.GRAPH_QURAN_TAB){
         DecimalFormat df = new DecimalFormat("###.00");
         int ayahsRead = statistics.get(QamarConstants.TOTAL_ACTIONS_DONE, 0);
         String average = "" + df.format(1.0 * ayahsRead / totalDays);

         String label = mContext.getString(R.string.avg_ayahs_per_day);
         SpannableStringBuilder builder = new SpannableStringBuilder();
         builder.append(label);
         builder.append(" ");

         int start = builder.length();
         builder.append("(" + average + ")");
         builder.setSpan(new ForegroundColorSpan(mCountColor),
                 start, builder.length(), 0);
         builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), 0);

         TextView textView = new TextView(mContext);
         textView.setText(builder);
         textViews.add(textView);
      }

      LinearLayout container = new LinearLayout(mContext);
      container.setOrientation(LinearLayout.HORIZONTAL);

      LinearLayout leftLayout = new LinearLayout(mContext);
      leftLayout.setOrientation(LinearLayout.VERTICAL);

      LinearLayout rightLayout = new LinearLayout(mContext);
      rightLayout.setOrientation(LinearLayout.VERTICAL);

      int size = textViews.size();
      for (int i = 0; i<size; i++){
         if (i % 2 == 0){
            leftLayout.addView(textViews.get(i),
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
         }
         else {
            rightLayout.addView(textViews.get(i),
                    ViewGroup.LayoutParams.MATCH_PARENT,
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

   private TextView makeTextView(String label, int days, int percent,
                                 boolean showPercentage){
      SpannableStringBuilder builder = new SpannableStringBuilder();
      builder.append(label);
      builder.append(" ");

      int start = builder.length();
      builder.append("(" + mNumberFormatter.format(days) + ")");
      builder.setSpan(new ForegroundColorSpan(mCountColor),
              start, builder.length(), 0);
      builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), 0);

      if (showPercentage){
         builder.append(" ");

         start = builder.length();
         builder.append(mNumberFormatter.format(percent) + "%");
         builder.setSpan(new ForegroundColorSpan(Color.LTGRAY),
                 start, builder.length(), 0);
      }

      TextView textView = new TextView(mContext);
      textView.setText(builder);
      return textView;
   }

}
