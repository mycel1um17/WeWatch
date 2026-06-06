package com.example.wewewwatch.domain.repository

import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.data.WatchMovie
import kotlinx.coroutines.flow.Flow

interface MovieRepositoryContract {
    fun observeWatchList(): Flow<List<WatchMovie>>

    suspend fun addMovie(movie: SearchMovie)

    suspend fun deleteMovies(imdbIds: Set<String>)

    fun searchMovies(query: String, year: String): Result<List<SearchMovie>>
}
