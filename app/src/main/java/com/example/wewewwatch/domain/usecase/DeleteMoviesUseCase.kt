package com.example.wewewwatch.domain.usecase

import com.example.wewewwatch.domain.repository.MovieRepositoryContract

class DeleteMoviesUseCase(
    private val repository: MovieRepositoryContract,
) {
    suspend operator fun invoke(imdbIds: Set<String>) {
        if (imdbIds.isNotEmpty()) {
            repository.deleteMovies(imdbIds)
        }
    }
}
