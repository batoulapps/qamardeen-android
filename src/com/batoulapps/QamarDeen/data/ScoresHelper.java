package com.batoulapps.QamarDeen.data;

import android.database.Cursor;
import android.util.Log;
import android.util.SparseArray;
import com.batoulapps.QamarDeen.utils.QamarTime;

import java.util.*;

public class ScoresHelper {

   public static class ScoreResult {
      public Map<Long, Integer> scores;
      public SparseArray<Integer> statistics;
   }

   public static ScoreResult getPrayerScores(QamarDbAdapter dbAdapter,
                                             long maxDate, long minDate){
      Map<Long, Integer> scores = new LinkedHashMap<Long, Integer>();
      Cursor cursor = dbAdapter.getPrayerEntries(maxDate, minDate);

      SparseArray<Integer> stats = new SparseArray<Integer>();
      if (cursor != null){
         long timestamp = minDate * 1000;
         if (timestamp == 0 && cursor.moveToLast()){
            timestamp = cursor.getLong(1) * 1000;
         }
         else if (timestamp == 0){
            timestamp = (maxDate * 1000) - (30 * QamarConstants.MS_PER_DAY);
         }

         Calendar gmtCal = QamarTime.getGMTCalendar();
         gmtCal.setTimeInMillis(maxDate * 1000);
         long maxDateMillis = QamarTime.getLocalTimeFromGMT(gmtCal);

         // fill all dates in the range with 0 score
         gmtCal.setTimeInMillis(timestamp);
         long minDateMillis = QamarTime.getLocalTimeFromGMT(gmtCal);
         long currentMillis = maxDateMillis;
         while (currentMillis >= minDateMillis){
            scores.put(currentMillis, 0);
            currentMillis -= QamarConstants.MS_PER_DAY;
         }

         if (cursor.moveToFirst()){
            do {
               timestamp = cursor.getLong(1) * 1000;
               int prayer = cursor.getInt(2);
               int status = cursor.getInt(3);

               if (status != QamarConstants.PrayerType.NOT_SET){
                  int typeCount = stats.get(status, 0);
                  typeCount++;
                  stats.put(status, typeCount);
               }

               // time calculations
               gmtCal = QamarTime.getGMTCalendar();
               gmtCal.setTimeInMillis(timestamp);
               long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

               // get or make columns for the data
               Integer score = scores.get(localTimestamp);
               if (score == null){ score = 0; }
               score += getPrayerScore(prayer, status);

               scores.put(localTimestamp, score);
            }
            while (cursor.moveToNext());
         }
         cursor.close();
      }

      ScoreResult result = new ScoreResult();
      result.scores = scores;
      result.statistics = stats;
      return result;
   }

   private static int getPrayerScore(int prayer, int status){
      int score = 0;
      switch (status){
         case QamarConstants.PrayerType.ALONE:
            if (prayer == QamarConstants.Prayers.DUHA){
               score = 200;
            }
            else if (prayer == QamarConstants.Prayers.QIYYAM){
               score = 300;
            }
            else { score = 100; }
            break;
         case QamarConstants.PrayerType.ALONE_WITH_VOLUNTARY:
            score = 200;
            break;
         case QamarConstants.PrayerType.GROUP:
            if (prayer == QamarConstants.Prayers.QIYYAM){
               score = 300;
            }
            else { score = 400; }
            break;
         case QamarConstants.PrayerType.GROUP_WITH_VOLUNTARY:
            score = 500;
            break;
         case QamarConstants.PrayerType.LATE:
            score = 25;
            break;
         case QamarConstants.PrayerType.EXCUSED:
            score = 300;
            break;
         case QamarConstants.PrayerType.NOT_SET:
         default:
            break;
      }
      return score;
   }
}
