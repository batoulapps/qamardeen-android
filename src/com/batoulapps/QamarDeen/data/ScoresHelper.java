package com.batoulapps.QamarDeen.data;

import android.database.Cursor;
import android.util.SparseArray;
import com.batoulapps.QamarDeen.utils.QamarTime;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScoresHelper {

   public static class ScoreResult {
      public Map<Long, Integer> scores;
      public SparseArray<Integer> statistics;
   }

   private static Map<Long, Integer> initializeEntries(Cursor cursor,
                                                         long maxDate,
                                                         long minDate){
      Map<Long, Integer> scores = new LinkedHashMap<Long, Integer>();
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
      return scores;
   }

   public static ScoreResult getPrayerScores(QamarDbAdapter dbAdapter,
                                             long maxDate, long minDate){
      Map<Long, Integer> scores = null;
      Cursor cursor = dbAdapter.getPrayerEntries(maxDate, minDate);

      SparseArray<Integer> stats = new SparseArray<Integer>();
      if (cursor != null){
         scores = initializeEntries(cursor, maxDate, minDate);

         if (cursor.moveToFirst()){
            do {
               long timestamp = cursor.getLong(1) * 1000;
               int prayer = cursor.getInt(2);
               int status = cursor.getInt(3);

               if (status != QamarConstants.PrayerType.NOT_SET){
                  int typeCount = stats.get(status, 0);
                  typeCount++;
                  stats.put(status, typeCount);
               }

               // time calculations
               Calendar gmtCal = QamarTime.getGMTCalendar();
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

   public static ScoreResult getSadaqahScores(QamarDbAdapter dbAdapter,
                                              long maxDate, long minDate){
      Map<Long, Integer> scores = null;
      Cursor cursor = dbAdapter.getSadaqahEntries(maxDate, minDate);

      int uniqueDays = 0;
      SparseArray<Integer> stats = new SparseArray<Integer>();
      if (cursor != null){
         scores = initializeEntries(cursor, maxDate, minDate);

         if (cursor.moveToFirst()){
            do {
               long timestamp = cursor.getLong(1) * 1000;
               int sadaqahType = cursor.getInt(2);

               int typeCount = stats.get(sadaqahType, 0);
               typeCount++;
               stats.put(sadaqahType, typeCount);

               // time calculations
               Calendar gmtCal = QamarTime.getGMTCalendar();
               gmtCal.setTimeInMillis(timestamp);
               long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

               // get or make columns for the data
               Integer score = scores.get(localTimestamp);
               if (score == null){ score = 0; }
               if (score == 0){ uniqueDays++; }
               score += getSadaqahScore(sadaqahType);

               scores.put(localTimestamp, score);
            }
            while (cursor.moveToNext());
         }
         cursor.close();
      }
      stats.put(QamarConstants.TOTAL_ACTIVE_DAYS, uniqueDays);

      ScoreResult result = new ScoreResult();
      result.scores = scores;
      result.statistics = stats;
      return result;
   }

   public static ScoreResult getFastingScores(QamarDbAdapter dbAdapter,
                                              long maxDate, long minDate){
      Map<Long, Integer> scores = null;
      Cursor cursor = dbAdapter.getFastingEntries(maxDate, minDate);

      int uniqueDays = 0;
      SparseArray<Integer> stats = new SparseArray<Integer>();
      if (cursor != null){
         scores = initializeEntries(cursor, maxDate, minDate);

         if (cursor.moveToFirst()){
            do {
               long timestamp = cursor.getLong(1) * 1000;
               int fastingType = cursor.getInt(2);

               int typeCount = stats.get(fastingType, 0);
               typeCount++;
               stats.put(fastingType, typeCount);

               // time calculations
               Calendar gmtCal = QamarTime.getGMTCalendar();
               gmtCal.setTimeInMillis(timestamp);
               long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

               // get or make columns for the data
               Integer score = scores.get(localTimestamp);
               if (score == null){ score = 0; }
               if (score == 0 &&
                       fastingType != QamarConstants.FastingTypes.NONE){
                  uniqueDays++;
               }
               score += getFastingScore(fastingType);

               scores.put(localTimestamp, score);
            }
            while (cursor.moveToNext());
         }
         cursor.close();
      }
      stats.put(QamarConstants.TOTAL_ACTIVE_DAYS, uniqueDays);

      ScoreResult result = new ScoreResult();
      result.scores = scores;
      result.statistics = stats;
      return result;
   }

   public static ScoreResult getQuranScores(QamarDbAdapter dbAdapter,
                                            long maxDate, long minDate){
      Map<Long, Integer> scores = null;
      Cursor cursor = dbAdapter.getQuranEntries(maxDate, minDate);

      int uniqueDays = 0;
      int numberOfAyahs = 0;
      if (cursor != null){
         scores = initializeEntries(cursor, maxDate, minDate);

         if (cursor.moveToFirst()){
            do {
               long timestamp = cursor.getLong(1) * 1000;
               int endAyah = cursor.getInt(2);
               int endSura = cursor.getInt(3);
               int startAyah = cursor.getInt(4);
               int startSura = cursor.getInt(5);
               int isExtraReading = cursor.getInt(6);
               QuranData qd = new QuranData(
                       startAyah, startSura, endAyah, endSura);
               int ayahsRead = qd.getAyahCount();
               numberOfAyahs += ayahsRead;

               // time calculations
               Calendar gmtCal = QamarTime.getGMTCalendar();
               gmtCal.setTimeInMillis(timestamp);
               long localTimestamp = QamarTime.getLocalTimeFromGMT(gmtCal);

               // get or make columns for the data
               Integer score = scores.get(localTimestamp);
               if (score == null){ score = 0; }
               if (score == 0){ uniqueDays++; }
               score += getQuranScore(ayahsRead);

               scores.put(localTimestamp, score);
            }
            while (cursor.moveToNext());
         }
         cursor.close();
      }

      SparseArray<Integer> stats = new SparseArray<Integer>();
      stats.put(QamarConstants.TOTAL_ACTIVE_DAYS, uniqueDays);
      stats.put(QamarConstants.TOTAL_AYAHS_READ, numberOfAyahs);

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

   private static int getSadaqahScore(int sadaqahType){
      int score = 100;
      if (sadaqahType == QamarConstants.CharityTypes.SMILE){
         score = 25;
      }
      return score;
   }

   private static int getFastingScore(int fastingType){
      int score = 0;
      switch (fastingType){
         case QamarConstants.FastingTypes.NAZR:
            score = 250;
            break;
         case QamarConstants.FastingTypes.KAFFARA:
            score = 100;
            break;
         case QamarConstants.FastingTypes.QADA2:
            score = 400;
            break;
         case QamarConstants.FastingTypes.SUNNAH:
         case QamarConstants.FastingTypes.FAREEDAH:
            score = 500;
            break;
         case QamarConstants.FastingTypes.NONE:
         default:
            score = 0;
            break;
      }
      return score;
   }

   private static int getQuranScore(int ayahsRead){
      return ayahsRead * 2;
   }
}
