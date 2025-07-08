package com.example.medifind;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "mediFind.db";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Constructor
    public DataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Example table for storing biodata
        String sql = "CREATE TABLE userProfile (" +
                "no INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "age TEXT, " +
                "gender TEXT, " +
                "phoneNum TEXT" +
                ");";
        Log.d("Data", "onCreate: " + sql);
        db.execSQL(sql);

        // 👉 You can create other tables here too, e.g. appointments
        String createAppointmentTable = "CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "clinic_name TEXT, " +
                "date TEXT, " +
                "time TEXT, " +
                "notes TEXT" +
                ");";
        db.execSQL(createAppointmentTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS userProfile");
        db.execSQL("DROP TABLE IF EXISTS appointments");
        onCreate(db);
    }
}