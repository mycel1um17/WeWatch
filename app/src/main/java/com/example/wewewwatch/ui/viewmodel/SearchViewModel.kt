package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wewewwatch.domain.usecase.SearchMoviesUseCase
import com.example.wewewwatch.ui.mvi.SearchIntent
import com.example.wewewwatch.ui.mvi.SearchState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMoviesUseCase: SearchMoviesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState.asStateFlow()

    fun handleIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.SearchRequested -> search(intent)
            is SearchIntent.SearchSucceeded -> reduceSearchSucceeded(intent)
            is SearchIntent.SearchFailed -> reduceSearchFailed(intent)
        }
    }

    private fun search(intent: SearchIntent.SearchRequested) {
        val query = intent.query.trim()
        val year = intent.year.trim()
        _uiState.update {
            it.copy(
                query = query,
                year = year,
                isLoading = true,
                movies = emptyList(),
                errorMessage = null,
            )
        }
        viewModelScope.launch {
            val result = searchMoviesUseCase(query, year)
            result
                .onSuccess { movies ->
                    handleIntent(SearchIntent.SearchSucceeded(movies))
                }
                .onFailure { error ->
                    handleIntent(SearchIntent.SearchFailed(error.message ?: "Search failed"))
                }
        }
    }

    private fun reduceSearchSucceeded(intent: SearchIntent.SearchSucceeded) {
        _uiState.update {
            it.copy(isLoading = false, movies = intent.movies, errorMessage = null)
        }
    }

    private fun reduceSearchFailed(intent: SearchIntent.SearchFailed) {
        _uiState.update {
            it.copy(isLoading = false, movies = emptyList(), errorMessage = intent.message)
        }
    }
}
