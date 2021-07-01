package com.loan555.musicplayer.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

/**
 *
val id: String,// nếu lưu trong local thì lúc dùng sẽ convert ra long( để lấy thumbai, và
val name: String,
val artists: String,// nguowif bieeu dieexn
val duration: Int,
val size: Int,
val title: String,
val albums: String,
val linkUri: String,// để chơi nhạc
 */


object NoteReaderContract {
    // Table contents are grouped together in an anonymous object.
    object NoteEntry : BaseColumns {
        const val TABLE_NAME = "entry"

        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ARTISTS = "artists"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_SIZE = "size"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ALBUMS = "albums"
        const val COLUMN_URL = "linkUri"
    }
}

private const val SQL_CREATE_ENTRIES = "CREATE TABLE ${NoteReaderContract.NoteEntry.TABLE_NAME}(" +
        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
        "${NoteReaderContract.NoteEntry.COLUMN_ID} TEXT," +
        "${NoteReaderContract.NoteEntry.COLUMN_NAME} TEXT," +
        "${NoteReaderContract.NoteEntry.COLUMN_ARTISTS} TEXT,)" +
        "${NoteReaderContract.NoteEntry.COLUMN_DURATION} TEXT,)" +
        "${NoteReaderContract.NoteEntry.COLUMN_SIZE} TEXT,)" +
        "${NoteReaderContract.NoteEntry.COLUMN_TITLE} TEXT,)" +
        "${NoteReaderContract.NoteEntry.COLUMN_ALBUMS} TEXT,)" +
        "${NoteReaderContract.NoteEntry.COLUMN_URL} TEXT)"

private const val SQL_DELETE_ENTRIES =
    "DROP TABLE IF EXISTS ${NoteReaderContract.NoteEntry.TABLE_NAME}"

class SongReaderDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SongReader.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db?.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}