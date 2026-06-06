package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wewewwatch.domain.usecase.AddMovieToWatchListUseCase
import com.example.wewewwatch.domain.usecase.DeleteMoviesUseCase
import com.example.wewewwatch.domain.usecase.ObserveWatchListUseCase
import com.example.wewewwatch.ui.mvi.MovieListEffect
import com.example.wewewwatch.ui.mvi.MovieListIntent
import com.example.wewewwatch.ui.mvi.MovieListState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieListViewModel(
    private val observeWatchListUseCase: ObserveWatchListUseCase,
    private val addMovieToWatchListUseCase: AddMovieToWatchListUseCase,
    private val deleteMoviesUseCase: DeleteMoviesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieListState())
    val uiState: StateFlow<MovieListState> = _uiState.asStateFlow()

    private val effectsChannel = Channel<MovieListEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeWatchListUseCase().collect { movies ->
                handleIntent(MovieListIntent.MovieLoaded(movies))
            }
        }
    }

    fun handleIntent(intent: MovieListIntent) {
        when (intent) {
            is MovieListIntent.MovieLoaded -> reduceMoviesLoaded(intent)
            is MovieListIntent.MovieMarkChanged -> reduceMovieMarkChanged(intent)
            is MovieListIntent.AddMovie -> addMovie(intent)
            MovieListIntent.DeleteMarkedMovies -> deleteMarkedMovies()
        }
    }

    private fun reduceMoviesLoaded(intent: MovieListIntent.MovieLoaded) {
        _uiState.update { state ->
            state.copy(
                movies = intent.movies,
                markedIds = state.markedIds.filterTo(mutableSetOf()) { markedId ->
                    intent.movies.any { it.imdbId == markedId }
                },
            )
        }
    }

    private fun reduceMovieMarkChanged(intent: MovieListIntent.MovieMarkChanged) {
        _uiState.update { state ->
            val nextMarkedIds = state.markedIds.toMutableSet().apply {
                if (intent.marked) {
                    add(intent.imdbId)
                } else {
                    remove(intent.imdbId)
                }
            }
            state.copy(markedIds = nextMarkedIds)
        }
    }

    private fun addMovie(intent: MovieListIntent.AddMovie) {
        viewModelScope.launch {
            addMovieToWatchListUseCase(intent.movie)
            effectsChannel.send(MovieListEffect.MovieAdded)
        }
    }

    private fun deleteMarkedMovies() {
        val idsToDelete = _uiState.value.markedIds
        if (idsToDelete.isEmpty()) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            deleteMoviesUseCase(idsToDelete)
            _uiState.update { it.copy(markedIds = emptySet(), isDeleting = false) }
            effectsChannel.send(MovieListEffect.MoviesDeleted)
        }
    }
}
