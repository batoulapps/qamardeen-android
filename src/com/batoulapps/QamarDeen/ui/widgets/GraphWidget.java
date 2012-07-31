package com.batoulapps.QamarDeen.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.*;

public class GraphWidget extends RelativeLayout {

   private Context mContext;
   private long mMinimumDate;

   public GraphWidget(Context context){
      super(context);
      init(context);
   }

   public GraphWidget(Context context, AttributeSet attrs){
      super(context, attrs);
      init(context);
   }

   public GraphWidget(Context context, AttributeSet attrs, int defStyle){
      super(context, attrs, defStyle);
      init(context);
   }

   private void init(Context context){
      mContext = context;
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

   /**
    * convenience method to return the minimum date shown by this graph
    * @return the minimum date (or 0 if there isn't one)
    */
   public long getMinimumDate(){
      return mMinimumDate;
   }

   public void renderGraph(Map<Long, Integer> scores){
      removeAllViews();
      String[] titles = new String[] { "" };
      List<Date[]> dates = new ArrayList<Date[]>();
      List<double[]> values = new ArrayList<double[]>();

      int maxScore = 100;
      Set<Long> keys = scores.keySet();
      int i = keys.size() - 1;
      Date[] dateValues = new Date[keys.size()];
      double[] scoreValues = new double[keys.size()];
      for (Long when : keys){
         dateValues[i] = new Date(when);
         int score = scores.get(when);
         if (score > maxScore){ maxScore = score; }
         scoreValues[i--] = score;

         // store the last minimum date for later use by QamarGraphActivity
         mMinimumDate = when;
      }

      dates.add(dateValues);
      values.add(scoreValues);

      int[] colors = new int[] { Color.BLUE };
      PointStyle[] styles = new PointStyle[] { PointStyle.POINT };
      XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
      setChartSettings(renderer, dateValues[0].getTime(),
              dateValues[dateValues.length - 1].getTime(),
              0, maxScore + 100, Color.DKGRAY);
      renderer.setYLabels(10);
      View view = ChartFactory.getTimeChartView(mContext,
              buildDateDataset(titles, dates, values),
              renderer, "MMM yyyy");

      addView(view, ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
   }

   /**
    * Builds an XY multiple time dataset using the provided values.
    *
    * @param titles the series titles
    * @param xValues the values for the X axis
    * @param yValues the values for the Y axis
    * @return the XY multiple time dataset
    */
   protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
                                                      List<double[]> yValues) {
      XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
      int length = titles.length;
      for (int i = 0; i < length; i++) {
         TimeSeries series = new TimeSeries(titles[i]);
         Date[] xV = xValues.get(i);
         double[] yV = yValues.get(i);
         int seriesLength = xV.length;
         for (int k = 0; k < seriesLength; k++) {
            series.add(xV[k], yV[k]);
         }
         dataset.addSeries(series);
      }
      return dataset;
   }

   /**
    * Builds an XY multiple series renderer.
    *
    * @param colors the series rendering colors
    * @param styles the series point styles
    * @return the XY multiple series renderers
    */
   private XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
      XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
      setRenderer(renderer, colors, styles);
      return renderer;
   }

   private void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
      renderer.setAxisTitleTextSize(16);
      renderer.setChartTitleTextSize(20);
      renderer.setLabelsTextSize(15);
      renderer.setLegendTextSize(15);
      renderer.setPointSize(5f);
      renderer.setMargins(new int[] { 20, 30, 15, 20 });
      int length = colors.length;
      for (int i = 0; i < length; i++) {
         XYSeriesRenderer r = new XYSeriesRenderer();
         r.setColor(colors[i]);
         r.setPointStyle(styles[i]);
         renderer.addSeriesRenderer(r);
      }
   }

   /**
    * Sets a few of the series renderer settings.
    *
    * @param renderer the renderer to set the properties to
    * @param xMin the minimum value on the X axis
    * @param xMax the maximum value on the X axis
    * @param yMin the minimum value on the Y axis
    * @param yMax the maximum value on the Y axis
    * @param axesColor the axes color
    */
   protected void setChartSettings(XYMultipleSeriesRenderer renderer,
                                   double xMin, double xMax, double yMin,
                                   double yMax, int axesColor) {
      renderer.setXAxisMin(xMin);
      renderer.setXAxisMax(xMax);
      renderer.setYAxisMin(yMin);
      renderer.setYAxisMax(yMax);
      renderer.setAxesColor(axesColor);
      renderer.setShowLabels(false);
      renderer.setShowLegend(false);
      renderer.setMarginsColor(Color.WHITE);
   }
}
