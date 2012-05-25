package com.batoulapps.QamarDeen.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QamarDbHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "qamardeen.db";
   private static final int DATABASE_VERSION = 1;
   
   private static final String PRAYERS_TABLE_DEF =
         "create table prayers(" +
         "_id integer primary key autoincrement, " +
         "ts timestamp not null, " +
         "salah integer not null, " +
         "status integer not null, " +
         "unique(ts, salah));";
   
   private static final String CHARITY_TABLE_DEF =
         "create table charity(" +
         "_id integer primary key autoincrement, " +
         "ts timestamp not null, " +
         "sadaqah_type integer not null, " +
         "unique(ts, sadaqah_type));";
         
   QamarDbHelper(Context context){
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }
   
   @Override
   public void onCreate(SQLiteDatabase db){
      db.execSQL(PRAYERS_TABLE_DEF);
      db.execSQL(CHARITY_TABLE_DEF);
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
   }
}
