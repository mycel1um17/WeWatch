package com.example.wewewwatch.domain.usecase

import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.data.WatchMovie
import com.example.wewewwatch.domain.repository.MovieRepositoryContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteMoviesUseCaseTest {
    @Test
    fun `does not call repository for empty ids`() = runBlocking {
        val repository = FakeMovieRepository()
        val useCase = DeleteMoviesUseCase(repository)

        useCase(emptySet())

        assertEquals(emptyList<Set<String>>(), repository.deletedIds)
    }

    @Test
    fun `passes selected ids to repository`() = runBlocking {
        val repository = FakeMovieRepository()
        val useCase = DeleteMoviesUseCase(repository)

        useCase(setOf("tt001", "tt002"))

        assertEquals(listOf(setOf("tt001", "tt002")), repository.deletedIds)
    }

    private class FakeMovieRepository : MovieRepositoryContract {
        val deletedIds = mutableListOf<Set<String>>()

        override fun observeWatchList(): Flow<List<WatchMovie>> = emptyFlow()

        override suspend fun addMovie(movie: SearchMovie) = Unit

        override suspend fun deleteMovies(imdbIds: Set<String>) {
            deletedIds += imdbIds
        }

        override fun searchMovies(query: String, year: String): Result<List<SearchMovie>> {
            return Result.success(emptyList())
        }
    }
}
