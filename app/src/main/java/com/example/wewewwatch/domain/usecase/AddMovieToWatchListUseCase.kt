package com.example.wewewwatch.domain.usecase

import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.domain.repository.MovieRepositoryContract

class AddMovieToWatchListUseCase(
    private val repository: MovieRepositoryContract,
) {
    suspend operator fun invoke(movie: SearchMovie) {
        repository.addMovie(movie)
    }
}
