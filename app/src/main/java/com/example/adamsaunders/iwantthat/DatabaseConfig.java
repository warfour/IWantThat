package com.example.adamsaunders.iwantthat;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseConfig extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "ItemBase.db";
    private static final int DATABASE_VERSION = 1;

    // Sets a easily callable standard for the base level command of my DB
    public DatabaseConfig(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}