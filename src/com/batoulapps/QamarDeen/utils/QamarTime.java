package com.batoulapps.QamarDeen.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class QamarTime {

   public static Calendar getTodayCalendar(){
      Calendar c = Calendar.getInstance();
      c.set(Calendar.HOUR, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      
      return c;
   }
   
   public static Calendar getGMTCalendar(){
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
      c.set(Calendar.HOUR, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      return c;
   }
   
   /**
    * given a gmt calendar at 12:00:00, returns the timestamp of a local
    * calendar set at the same date as the gmt calendar at 12:00:00 local
    * @param gmtCal a gmt calendar
    * @return timestamp of 12:00:00 on the same day in the local timezone
    */
   public static long getLocalTimeFromGMT(Calendar gmtCal){
      Calendar localTime = getTodayCalendar();
      localTime.set(gmtCal.get(Calendar.YEAR),
            gmtCal.get(Calendar.MONTH),
            gmtCal.get(Calendar.DATE));
      return localTime.getTimeInMillis();
   }
   
   /**
    * given a local calendar at 12:00:00, returns the timestamp of a gmt
    * calendar set at the same date as the local calendar at 12:00:00 gmt
    * @param localCal a local calendar
    * @return timestamp of 12:00:00 on the same day in gmt
    */
   public static long getGMTTimeFromLocal(Calendar localCal){
      Calendar gmtTime = getGMTCalendar();
      gmtTime.set(localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DATE));
      return gmtTime.getTimeInMillis();
   }
   
   /**
    * given a local date at 12:00:00, returns the timestamp of a gmt
    * calendar set at the same date as the local date at 12:00:00 gmt
    * @param localCal a local calendar
    * @return timestamp of 12:00:00 on the same day in gmt
    */
   public static long getGMTTimeFromLocalDate(Date localDate){
      Calendar cal = QamarTime.getTodayCalendar();
      cal.setTime(localDate);
      return QamarTime.getGMTTimeFromLocal(cal);
   }
}
