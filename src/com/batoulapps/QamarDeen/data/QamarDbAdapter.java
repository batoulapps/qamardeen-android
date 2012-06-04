package com.batoulapps.QamarDeen.data;

import java.util.List;

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
   
   protected static class CharityTable {
      public static final String TABLE_NAME = "charity";
      public static final String ID = "_id";
      public static final String TIME = "ts";
      public static final String SADAQAH_TYPE = "sadaqah_type";
   }
   
   protected static class QuranTable {
      public static final String TABLE_NAME = "readings";
      public static final String ID = "_id";
      public static final String TIME = "ts";
      public static final String END_AYAH = "endayah";
      public static final String END_SURA = "endsura";
      public static final String START_AYAH = "startayah";
      public static final String START_SURA = "startsura";
      public static final String IS_EXTRA = "is_extra";
   }
   
   protected static class FastingTable {
      public static final String TABLE_NAME = "fasting";
      public static final String ID = "_id";
      public static final String TIME = "ts";
      public static final String FASTING_TYPE = "fasting_type";
   }
   
   public QamarDbAdapter(Context context){
      mContext = context;
   }
   
   public synchronized QamarDbAdapter open() throws SQLException {
      if (mDbHelper == null){
         mDbHelper = new QamarDbHelper(mContext);
         mDb = mDbHelper.getWritableDatabase();
      }
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
      if (mDb == null){ return null; }

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
      if (mDb == null){ return false; }

      ContentValues values = new ContentValues();
      values.put(PrayersTable.TIME, time);
      values.put(PrayersTable.PRAYER, salah);
      values.put(PrayersTable.STATUS, value);
      long result = mDb.replace(PrayersTable.TABLE_NAME, null, values);
      return result != -1;
   }
   
   /**
    * gets the sadaqah entries for a specific time range
    * @param max the maximum timestamp to fetch (in seconds, gmt at 12:00)
    * @param min the minimum timestamp to fetch (in seconds, gmt at 12:00)
    * @return Cursor of the results
    */
   public Cursor getSadaqahEntries(long max, long min) {
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return null; }
      Cursor cursor = mDb.query(CharityTable.TABLE_NAME,
            null, CharityTable.TIME + " > " + min + " AND " + 
                  CharityTable.TIME + " <= " + max,
            null, null, null, CharityTable.TIME + " DESC, " + 
                  CharityTable.SADAQAH_TYPE + " ASC");
      return cursor;
   }
   
   /**
    * write the sadaqah data for a specific day
    * @param time the timestamp to write (in seconds, gmt at 12:00)
    * @param types an arraylist of sadaqah types for that day
    * @return true if succeeded or false otherwise
    */
   public boolean writeSadaqahEntries(long time, List<Integer> types) {
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return false; }

      // start a transaction
      mDb.beginTransaction();
      // remove old sadaqah entries for that day
      mDb.delete(CharityTable.TABLE_NAME,
            CharityTable.TIME + "= ?", new String[]{ "" + time });
      
      if (types != null){
         // add all the values
         for (Integer type : types){
            ContentValues values = new ContentValues();
            values.put(CharityTable.TIME, time);
            values.put(CharityTable.SADAQAH_TYPE, type);
            mDb.insert(CharityTable.TABLE_NAME, null, values);
         }
      }
      
      // commit the transaction
      mDb.setTransactionSuccessful();
      mDb.endTransaction();
      
      return true;
   }
   
   /**
    * get the quran entries for a specific date range
    * @param max the maximum date (in seconds, gmt at 12:00)
    * @param min the minimum date (in seconds, gmt at 12:00)
    * @return the cursor of the results
    */
   public Cursor getQuranEntries(long max, long min) {
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return null; }
      Cursor cursor = mDb.query(QuranTable.TABLE_NAME,
            null, QuranTable.TIME + " > " + min + " AND " + 
                  QuranTable.TIME + " <= " + max,
            null, null, null, QuranTable.TIME + " DESC");
      return cursor;
   }
   
   /**
    * gets the first non-extra entry before a particular date
    * @param min the minimum date
    * @return the first entry before the minimum date
    */
   public Cursor getEarlierQuranEntry(long min){
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return null; }
      Cursor cursor = mDb.query(QuranTable.TABLE_NAME,
            null, QuranTable.TIME + " < " + min + " AND " +
            QuranTable.IS_EXTRA + " = 0",
            null, null, null, QuranTable.TIME + " DESC limit 1");
      return cursor;
   }
   
   /**
    * write or updated standard Quran entry changes
    * @param changedTime the date for entry that changed (seconds, gmt at 12)
    * @param data the new data (or null if we should remove this row)
    * @param affectedTime the date for the row that got affected (or null)
    * @param affectedData the data for the row that got affected if applicable
    * @return true if succeeded or false otherwise
    */
   public boolean writeQuranEntry(long changedTime, QuranData data,
         Long affectedTime, QuranData affectedData){
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return false; }

      // start a transaction
      mDb.beginTransaction();
      
      /* unfortunately, we can't do replace in this method because given
       * the current table definition, no conflict would occur. */
      
      // delete the entry
      mDb.delete(QuranTable.TABLE_NAME,
                 QuranTable.TIME + "= ? AND " + QuranTable.IS_EXTRA + " = ?",
                 new String[]{ "" + changedTime, "0" });
      
      if (data != null) {
         // add updated entry back if it wasn't a delete
         ContentValues values = new ContentValues();
         values.put(QuranTable.TIME, changedTime);
         values.put(QuranTable.START_AYAH, data.getStartAyah());
         values.put(QuranTable.START_SURA, data.getStartSura());
         values.put(QuranTable.END_AYAH, data.getEndAyah());
         values.put(QuranTable.END_SURA, data.getEndSura());
         values.put(QuranTable.IS_EXTRA, 0);
         mDb.insert(QuranTable.TABLE_NAME, null, values);
      }
      
      if (affectedTime != null && affectedData != null){
         // remove the old entry
         mDb.delete(QuranTable.TABLE_NAME,
               QuranTable.TIME + "= ?", new String[]{ "" + affectedTime });
         
         // add new entry
         ContentValues values = new ContentValues();
         values.put(QuranTable.TIME, affectedTime);
         values.put(QuranTable.START_AYAH, affectedData.getStartAyah());
         values.put(QuranTable.START_SURA, affectedData.getStartSura());
         values.put(QuranTable.END_AYAH, affectedData.getEndAyah());
         values.put(QuranTable.END_SURA, affectedData.getEndSura());
         values.put(QuranTable.IS_EXTRA, 0);
         mDb.insert(QuranTable.TABLE_NAME, null, values);
      }
      
      // commit the transaction
      mDb.setTransactionSuccessful();
      mDb.endTransaction();
      
      return true;
   }
   
   /**
    * write or updated extra Quran entry changes
    * @param when the date for entry that changed (seconds, gmt at 12)
    * @param data the new data
    * @return true if succeeded or false otherwise
    */
   public boolean writeExtraQuranEntries(long when, List<QuranData> suras){
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return false; }

      // start a transaction
      mDb.beginTransaction();
      
      // delete the old entries for this day
      mDb.delete(QuranTable.TABLE_NAME,
                 QuranTable.TIME + "= ? AND " + QuranTable.IS_EXTRA + " = ?",
                 new String[]{ "" + when, "1" });
      
      if (suras != null) {
         for (QuranData data : suras){
            ContentValues values = new ContentValues();
            values.put(QuranTable.TIME, when);
            values.put(QuranTable.START_AYAH, data.getStartAyah());
            values.put(QuranTable.START_SURA, data.getStartSura());
            values.put(QuranTable.END_AYAH, data.getEndAyah());
            values.put(QuranTable.END_SURA, data.getEndSura());
            values.put(QuranTable.IS_EXTRA, 1);
            mDb.insert(QuranTable.TABLE_NAME, null, values);
         }
      }
      
      // commit the transaction
      mDb.setTransactionSuccessful();
      mDb.endTransaction();
      
      return true;
   }
   
   /**
    * gets the fasting entries for a specific time range
    * @param max the maximum timestamp to fetch (in seconds, gmt at 12:00)
    * @param min the minimum timestamp to fetch (in seconds, gmt at 12:00)
    * @return Cursor of the results
    */
   public Cursor getFastingEntries(long max, long min) {
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return null; }
      Cursor cursor = mDb.query(FastingTable.TABLE_NAME,
            null, FastingTable.TIME + " > " + min + " AND " + 
                  FastingTable.TIME + " <= " + max,
            null, null, null, FastingTable.TIME + " DESC");
      return cursor;
   }
   
   /**
    * write the fasting data for a specific day
    * @param time the timestamp to write (in seconds, gmt at 12:00)
    * @param types an arraylist of sadaqah types for that day
    * @return true if succeeded or false otherwise
    */
   public boolean writeFastingEntry(long time, Integer type) {
      if (mDbHelper == null){ open(); }
      if (mDb == null){ return false; }
      
      ContentValues values = new ContentValues();
      values.put(FastingTable.TIME, time);
      values.put(FastingTable.FASTING_TYPE, type);
      long result = mDb.replace(FastingTable.TABLE_NAME, null, values);
      return result != -1;
   }
}
