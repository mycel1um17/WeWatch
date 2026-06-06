package com.example.wewewwatch.ui.mvi

import com.example.wewewwatch.data.SearchMovie

data class SearchState(
    val query: String = "",
    val year: String = "",
    val isLoading: Boolean = false,
    val movies: List<SearchMovie> = emptyList(),
    val errorMessage: String? = null,
)

sealed interface SearchIntent {
    data class SearchRequested(val query: String, val year: String) : SearchIntent
    data class SearchSucceeded(val movies: List<SearchMovie>) : SearchIntent
    data class SearchFailed(val message: String) : SearchIntent
}
