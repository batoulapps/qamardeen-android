package com.batoulapps.QamarDeen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class QamarDbAdapter {
   
   private Context mContext = null;
   private QamarDbHelper mDbHelper = null;
   private SQLiteDatabase mDb = null;
   
   protected static class PrayersTable {
      public static final String TABLE_NAME = "prayers";
      public static final String ID = "_id";
      public static final String TIME = "ts";
      public static final String PRAYER = "salah";
      public static final String STATUS = "status";
   }
   
   public QamarDbAdapter(Context context){
      mContext = context;
   }
   
   public QamarDbAdapter open() throws SQLException {
      mDbHelper = new QamarDbHelper(mContext);
      mDb = mDbHelper.getWritableDatabase();
      return this;
   }
   
   public void close(){
      if (mDbHelper != null){
         mDbHelper.close();
         mDbHelper = null;
         mDb = null;
      }
   }
   
   /**
    * gets the prayer entries for a specific time range
    * @param max the maximum timestamp to fetch (in seconds, gmt at 12:00)
    * @param min the minimum timestamp to fetch (in seconds, gmt at 12:00)
    * @return Cursor of the results
    */
   public Cursor getPrayerEntries(long max, long min) {
      if (mDbHelper == null){ open(); }
      Cursor cursor = mDb.query(PrayersTable.TABLE_NAME,
            null, PrayersTable.TIME + " > " + min + " AND " + 
            PrayersTable.TIME + " <= " + max,
            null, null, null, PrayersTable.TIME + " DESC");
      return cursor;
   }
   
   /**
    * @param time the timestamp to write (in seconds, gmt at 12:00)
    * @param salah the salah to write
    * @param value the value of the entry
    * @return true if succeeded or false otherwise
    */
   public boolean writePrayerEntry(long time, int salah, int value) {
      if (mDbHelper == null){ open(); }
      ContentValues values = new ContentValues();
      values.put(PrayersTable.TIME, time);
      values.put(PrayersTable.PRAYER, salah);
      values.put(PrayersTable.STATUS, value);
      long result = mDb.replace(PrayersTable.TABLE_NAME, null, values);
      return result != -1;
   }
}
