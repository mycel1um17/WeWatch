package com.example.wewewwatch.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MovieDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MOVIES (
                $COLUMN_IMDB_ID TEXT PRIMARY KEY,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_YEAR TEXT NOT NULL,
                $COLUMN_GENRE TEXT NOT NULL,
                $COLUMN_POSTER TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVIES")
        onCreate(db)
    }

    fun getMovies(): List<WatchMovie> {
        val movies = mutableListOf<WatchMovie>()
        readableDatabase.query(
            TABLE_MOVIES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_TITLE COLLATE NOCASE",
        ).use { cursor ->
            val imdbIdIndex = cursor.getColumnIndexOrThrow(COLUMN_IMDB_ID)
            val titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE)
            val yearIndex = cursor.getColumnIndexOrThrow(COLUMN_YEAR)
            val genreIndex = cursor.getColumnIndexOrThrow(COLUMN_GENRE)
            val posterIndex = cursor.getColumnIndexOrThrow(COLUMN_POSTER)
            while (cursor.moveToNext()) {
                movies += WatchMovie(
                    imdbId = cursor.getString(imdbIdIndex),
                    title = cursor.getString(titleIndex),
                    year = cursor.getString(yearIndex),
                    genre = cursor.getString(genreIndex),
                    posterUrl = cursor.getString(posterIndex),
                )
            }
        }
        return movies
    }

    fun addMovie(movie: SearchMovie) {
        val values = ContentValues().apply {
            put(COLUMN_IMDB_ID, movie.imdbId)
            put(COLUMN_TITLE, movie.title)
            put(COLUMN_YEAR, movie.year)
            put(COLUMN_GENRE, movie.genre)
            put(COLUMN_POSTER, movie.posterUrl)
        }
        writableDatabase.insertWithOnConflict(
            TABLE_MOVIES,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE,
        )
    }

    fun deleteMovies(imdbIds: Set<String>) {
        if (imdbIds.isEmpty()) return
        val placeholders = imdbIds.joinToString(",") { "?" }
        writableDatabase.delete(
            TABLE_MOVIES,
            "$COLUMN_IMDB_ID IN ($placeholders)",
            imdbIds.toTypedArray(),
        )
    }

    companion object {
        private const val DATABASE_NAME = "wewatch.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_MOVIES = "movies"
        private const val COLUMN_IMDB_ID = "imdb_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_GENRE = "genre"
        private const val COLUMN_POSTER = "poster"
    }
}
