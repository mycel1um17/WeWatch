package com.example.wewewwatch.data

import com.example.wewewwatch.domain.repository.MovieRepositoryContract
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao,
    private val omdbClient: OmdbClient = OmdbClient(),
) : MovieRepositoryContract {
    override fun observeWatchList(): Flow<List<WatchMovie>> = movieDao.observeMovies()

    override suspend fun addMovie(movie: SearchMovie) {
        movieDao.addMovie(movie.toWatchMovie())
    }

    override suspend fun deleteMovies(imdbIds: Set<String>) {
        movieDao.deleteMovies(imdbIds)
    }

    override fun searchMovies(query: String, year: String): Result<List<SearchMovie>> =
        omdbClient.searchMovies(query, year)
}
