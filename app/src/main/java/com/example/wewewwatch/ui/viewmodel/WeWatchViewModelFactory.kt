package com.example.wewewwatch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wewewwatch.domain.repository.MovieRepositoryContract
import com.example.wewewwatch.domain.usecase.AddMovieToWatchListUseCase
import com.example.wewewwatch.domain.usecase.DeleteMoviesUseCase
import com.example.wewewwatch.domain.usecase.ObserveWatchListUseCase
import com.example.wewewwatch.domain.usecase.SearchMoviesUseCase

class WeWatchViewModelFactory(
    repository: MovieRepositoryContract,
) : ViewModelProvider.Factory {
    private val observeWatchListUseCase = ObserveWatchListUseCase(repository)
    private val addMovieToWatchListUseCase = AddMovieToWatchListUseCase(repository)
    private val deleteMoviesUseCase = DeleteMoviesUseCase(repository)
    private val searchMoviesUseCase = SearchMoviesUseCase(repository)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MovieListViewModel::class.java) ->
                MovieListViewModel(
                    observeWatchListUseCase = observeWatchListUseCase,
                    addMovieToWatchListUseCase = addMovieToWatchListUseCase,
                    deleteMoviesUseCase = deleteMoviesUseCase,
                ) as T

            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(searchMoviesUseCase) as T

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
