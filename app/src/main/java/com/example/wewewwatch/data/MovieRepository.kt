package com.example.wewewwatch.data

import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao,
    private val omdbClient: OmdbClient = OmdbClient(),
) {
    fun observeWatchList(): Flow<List<WatchMovie>> = movieDao.observeMovies()

    suspend fun addMovie(movie: SearchMovie) {
        movieDao.addMovie(movie.toWatchMovie())
    }

    suspend fun deleteMovies(imdbIds: Set<String>) {
        movieDao.deleteMovies(imdbIds)
    }

    fun searchMovies(query: String, year: String): Result<List<SearchMovie>> =
        omdbClient.searchMovies(query, year)
}
