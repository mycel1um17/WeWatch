package com.example.wewewwatch.data

data class WatchMovie(
    val imdbId: String,
    val title: String,
    val year: String,
    val genre: String = "",
    val posterUrl: String = "",
    val isMarked: Boolean = false,
)

data class SearchMovie(
    val imdbId: String,
    val title: String,
    val year: String,
    val genre: String = "",
    val posterUrl: String = "",
)
