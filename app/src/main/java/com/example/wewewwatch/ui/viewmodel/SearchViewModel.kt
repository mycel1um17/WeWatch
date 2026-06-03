package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wewewwatch.data.MovieRepository
import com.example.wewewwatch.data.SearchMovie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SearchUiState(
    val isLoading: Boolean = false,
    val movies: List<SearchMovie> = emptyList(),
    val errorMessage: String? = null,
)

class SearchViewModel(
    private val repository: MovieRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun search(query: String, year: String) {
        _uiState.update { it.copy(isLoading = true, movies = emptyList(), errorMessage = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching { repository.searchMovies(query, year).getOrThrow() }
            }
            result
                .onSuccess { movies ->
                    _uiState.update {
                        it.copy(isLoading = false, movies = movies, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movies = emptyList(),
                            errorMessage = error.message ?: "Search failed",
                        )
                    }
                }
        }
    }
}
