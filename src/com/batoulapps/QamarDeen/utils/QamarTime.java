package com.batoulapps.QamarDeen.utils;

import java.util.Calendar;
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
      return c;
   }
}
