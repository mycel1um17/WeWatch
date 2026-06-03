package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wewewwatch.data.MovieRepository
import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.data.WatchMovie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieListUiState(
    val movies: List<WatchMovie> = emptyList(),
    val markedIds: Set<String> = emptySet(),
)

class MovieListViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieListUiState())
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeWatchList().collect { movies ->
                _uiState.update { state ->
                    state.copy(
                        movies = movies,
                        markedIds = state.markedIds.filterTo(mutableSetOf()) { markedId ->
                            movies.any { it.imdbId == markedId }
                        },
                    )
                }
            }
        }
    }

    fun setMovieMarked(imdbId: String, marked: Boolean) {
        _uiState.update { state ->
            val nextMarkedIds = state.markedIds.toMutableSet()
            if (marked) {
                nextMarkedIds += imdbId
            } else {
                nextMarkedIds -= imdbId
            }
            state.copy(markedIds = nextMarkedIds)
        }
    }

    fun addMovie(movie: SearchMovie, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addMovie(movie)
            onComplete()
        }
    }

    fun deleteMarkedMovies() {
        val idsToDelete = _uiState.value.markedIds
        if (idsToDelete.isEmpty()) return
        viewModelScope.launch {
            repository.deleteMovies(idsToDelete)
            _uiState.update { it.copy(markedIds = emptySet()) }
        }
    }
}
