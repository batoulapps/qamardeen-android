package com.batoulapps.QamarDeen.utils;

import java.util.Calendar;

// based on Joda-Time's IslamicChronology.java
// http://joda-time.sourceforge.net/
public class HijriUtils {
   private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
   private static final long MILLIS_PER_LONG_YEAR = 355L * MILLIS_PER_DAY;
   private static final long MILLIS_PER_SHORT_YEAR = 354L * MILLIS_PER_DAY;

   // millis of year of 0001-01-01
   private static final long MILLIS_YEAR_1 = -42521587200000L;

   // 19 years are 354 days, 11 leap years of 355 days
   private static final long MILLIS_PER_CYCLE = 
       ((19L * 354L + 11L * 355L) * MILLIS_PER_DAY);

   // length of the cycle
   private static final int CYCLE = 30;

   // length of long month
   private static final int LONG_MONTH_LENGTH  = 30;

   // days in a pair of months
   private static final int MONTH_PAIR_LENGTH = 29 + 30;

   // leap year pattern - based on 16-based  pattern
   // 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29
   private static final int LEAP_YEAR_PATTERN = 623191204;

   public static class HijriDate {
      public int year;
      public int month;
      public int day;
   }
   
   /**
    * get the hijri date from a calendar
    * @param cal a calendar with the day to convert
    * @return the hijri date
    */
   public static HijriDate getHijriDate(Calendar cal){
      return getHijriDate(cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
   }
   
   /**
    * given a day, month, and year, returns the hijri date
    * @param pMonth the current month (1 based)
    * @param pDay the current day
    * @param pYear the current year
    * @return the hijri date
    */
   public static HijriDate getHijriDate(int pMonth, int pDay, int pYear){
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, pYear);
      cal.set(Calendar.MONTH, pMonth - 1);
      cal.set(Calendar.DATE, pDay);
      long timestamp = cal.getTimeInMillis();

      int hijriYear = getHijriYear(timestamp);
      int hijriMonth = getHijriMonth(timestamp, hijriYear);
      int hijriDate = getHijriDay(timestamp, hijriYear);
      
      HijriDate result = new HijriDate();
      result.year = hijriYear;
      result.month = hijriMonth;
      result.day = hijriDate;
      
      return result;
   }

   private static int getHijriYear(long timestamp){
      long millisIslamic = timestamp - MILLIS_YEAR_1;
      long cycles = millisIslamic / MILLIS_PER_CYCLE;
      long cycleRemainder = millisIslamic % MILLIS_PER_CYCLE;

      int year = (int)((cycles * CYCLE) + 1L);
      long yearMillis = isHijriLeapYear(year)? 
         MILLIS_PER_LONG_YEAR : MILLIS_PER_SHORT_YEAR;
      while (cycleRemainder >= yearMillis){
         cycleRemainder -= yearMillis;
         yearMillis = isHijriLeapYear(++year)?
            MILLIS_PER_LONG_YEAR : MILLIS_PER_SHORT_YEAR;
      }
      return year;
   }

   private static int getHijriMonth(long timestamp, int hYear){
      int doyZeroBased =
         (int)((timestamp - calculateFirstDayOfYearMillis(hYear)) / 
               MILLIS_PER_DAY);
      if (doyZeroBased == 354){ return 12; }
      return ((doyZeroBased * 2) / MONTH_PAIR_LENGTH) + 1;
   }

   private static int getHijriDay(long timestamp, int hYear){
      long yearStart = calculateFirstDayOfYearMillis(hYear);
      int dayOfYear = (int)((timestamp - yearStart) / MILLIS_PER_DAY);
      if (dayOfYear == 354){ return 30; }
      return (dayOfYear % MONTH_PAIR_LENGTH) % LONG_MONTH_LENGTH + 1;
   }

   private static long calculateFirstDayOfYearMillis(int year){
      year--;
      long cycle = year / CYCLE;
      long millis = MILLIS_YEAR_1 + cycle * MILLIS_PER_CYCLE;
      int cycleRemainder = (year % CYCLE) + 1;

      for (int i=1; i < cycleRemainder; i++){
         millis += (isHijriLeapYear(i)? MILLIS_PER_LONG_YEAR :
               MILLIS_PER_SHORT_YEAR);
      }

      return millis;
   }

   private static boolean isHijriLeapYear(int year){
      int key = 1 << (year % 30);
      return ((LEAP_YEAR_PATTERN & key) > 0);
   }
}
