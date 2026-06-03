package com.example.wewewwatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class WatchMovie(
    @PrimaryKey
    val imdbId: String,
    val title: String,
    val year: String,
    val genre: String = "",
    val posterUrl: String = "",
)

data class SearchMovie(
    val imdbId: String,
    val title: String,
    val year: String,
    val genre: String = "",
    val posterUrl: String = "",
)

fun SearchMovie.toWatchMovie(): WatchMovie = WatchMovie(
    imdbId = imdbId,
    title = title,
    year = year,
    genre = genre,
    posterUrl = posterUrl,
)
