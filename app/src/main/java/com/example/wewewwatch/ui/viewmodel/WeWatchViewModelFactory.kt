package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wewewwatch.data.MovieRepository

class WeWatchViewModelFactory(
    private val repository: MovieRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MovieListViewModel::class.java) ->
                MovieListViewModel(repository) as T

            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(repository) as T

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
