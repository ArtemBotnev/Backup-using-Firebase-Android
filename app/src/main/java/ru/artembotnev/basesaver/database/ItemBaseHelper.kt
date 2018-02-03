package ru.artembotnev.basesaver.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import ru.artembotnev.basesaver.database.DbSchema.ItemTable

/**
 * Created by Artem Botnev on 14.12.2017.
 */

private const val DB_VERSION = 1
private const val DB_NAME = "itemBase.db"

class ItemBaseHelper(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table ${ItemTable.NAME} (" +
                " _id integer primary key autoincrement," +
                " ${ItemTable.Cols.UUID} text," +
                " ${ItemTable.Cols.TITLE} text," +
                " ${ItemTable.Cols.TYPE} integer," +
                " ${ItemTable.Cols.SIZE} integer," +
                " ${ItemTable.Cols.COLOR} integer)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //code for updating
    }
}