package com.example.wewewwatch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY title COLLATE NOCASE")
    fun observeMovies(): Flow<List<WatchMovie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovie(movie: WatchMovie)

    @Query("DELETE FROM movies WHERE imdbId IN (:imdbIds)")
    suspend fun deleteMovies(imdbIds: Set<String>)

    @Delete
    suspend fun deleteMovie(movie: WatchMovie)
}
