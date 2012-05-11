package com.batoulapps.QamarDeen.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class QamarDbHelper extends SQLiteOpenHelper {
   private static final String DATABASE_NAME = "qamardeen.db";
   private static final int DATABASE_VERSION = 1;
   
   private static final String DATABASE_CREATE =
         "create table prayers(" +
         "_id integer primary key autoincrement, " +
         "ts timestamp not null, " +
         "salah integer not null, " +
         "status integer not null);";
   
   QamarDbHelper(Context context){
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
   }
   
   @Override
   public void onCreate(SQLiteDatabase db){
      db.execSQL(DATABASE_CREATE);
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
   }
}
