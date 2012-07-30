package com.batoulapps.QamarDeen.data;



public class QamarConstants {

   public static final int MS_PER_DAY = 24 * 60 * 60 * 1000;
   public static final int TOTAL_AYAHS_READ = 998;
   public static final int TOTAL_ACTIVE_DAYS = 999;

   public static class PreferenceKeys {
     public static final String GENDER_PREF = "user_gender"; 
     public static final String SHOW_EXTRA_PRAYERS = "show_extra_prayers";
   };
   
   public static class Prayers {
      public static final int FAJR = 0;
      public static final int DUHA = 1;
      public static final int DHUHR = 2;
      public static final int ASR = 3;
      public static final int MAGHRIB = 4;
      public static final int ISHA = 5;
      public static final int QIYYAM = 6;
    };
    
    public static int[] PRAYER_LIST = new int[]{ 
       Prayers.FAJR, Prayers.DHUHR, Prayers.ASR,
       Prayers.MAGHRIB, Prayers.ISHA };

   public static class PrayerType {
      public static final int GROUP_WITH_VOLUNTARY = 4;
      public static final int GROUP = 3;
      public static final int ALONE_WITH_VOLUNTARY = 2;
      public static final int ALONE = 1;
      public static final int LATE = 5;
      public static final int NOT_SET = 0;
      public static final int EXCUSED = 6;
   }
    
    public static class CharityTypes {
       public static final int MONEY = 0;
       public static final int EFFORT = 1;
       public static final int FOOD = 2;
       public static final int CLOTHES = 3;
       public static final int SMILE = 4;
       public static final int OTHER = 5;
    }

    public static class FastingTypes {
       public static final int FAREEDAH = 1;
       public static final int SUNNAH = 2;
       public static final int QADA2 = 3;
       public static final int KAFFARA = 4;
       public static final int NAZR = 5;
       public static final int NONE = 0;
    }
    
    public static int[] SURA_NUM_AYAHS = {
       7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111,
       43, 52, 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77,
       227, 93, 88, 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75,
       85, 54, 53, 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55,
       78, 96, 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52,
       44, 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25,
       22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
       11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
    };
}
