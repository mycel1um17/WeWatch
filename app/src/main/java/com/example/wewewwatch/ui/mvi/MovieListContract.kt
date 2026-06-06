package com.example.wewewwatch.ui.mvi

import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.data.WatchMovie

data class MovieListState(
    val movies: List<WatchMovie> = emptyList(),
    val markedIds: Set<String> = emptySet(),
    val isDeleting: Boolean = false,
)

sealed interface MovieListIntent {
    data class MovieLoaded(val movies: List<WatchMovie>) : MovieListIntent
    data class MovieMarkChanged(val imdbId: String, val marked: Boolean) : MovieListIntent
    data class AddMovie(val movie: SearchMovie) : MovieListIntent
    data object DeleteMarkedMovies : MovieListIntent
}

sealed interface MovieListEffect {
    data object MovieAdded : MovieListEffect
    data object MoviesDeleted : MovieListEffect
}
