package com.example.wewewwatch.domain.usecase

import com.example.wewewwatch.data.WatchMovie
import com.example.wewewwatch.domain.repository.MovieRepositoryContract
import kotlinx.coroutines.flow.Flow

class ObserveWatchListUseCase(
    private val repository: MovieRepositoryContract,
) {
    operator fun invoke(): Flow<List<WatchMovie>> = repository.observeWatchList()
}
