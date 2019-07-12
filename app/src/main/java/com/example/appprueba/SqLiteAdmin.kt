package com.example.appprueba

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory

class SqLiteAdmin(context: Context, name: String, factory: CursorFactory?, version: Int):  SQLiteOpenHelper(context, name, factory, version){

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table Registros(id int primary key, nombre text, total int)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}