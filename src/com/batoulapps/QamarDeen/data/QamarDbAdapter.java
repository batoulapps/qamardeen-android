package com.batoulapps.QamarDeen.data;

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
      public static final String TIME = "when";
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
      mDbHelper.close();
   }
   
   public Cursor getPrayerEntries(long minDate) throws SQLException {
      Cursor cursor = mDb.query(PrayersTable.TABLE_NAME,
            null, PrayersTable.TIME + " > " + minDate,
            null, null, null, PrayersTable.TIME + " DESC");
      if (cursor != null){
         cursor.moveToFirst();
      }
      return cursor;
   }
}
