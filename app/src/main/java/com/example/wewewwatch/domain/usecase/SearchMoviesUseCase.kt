package com.example.wewewwatch.domain.usecase

import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.domain.repository.MovieRepositoryContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchMoviesUseCase(
    private val repository: MovieRepositoryContract,
) {
    suspend operator fun invoke(query: String, year: String): Result<List<SearchMovie>> {
        return withContext(Dispatchers.IO) {
            repository.searchMovies(query.trim(), year.trim())
        }
    }
}
