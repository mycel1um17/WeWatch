package com.example.wewewwatch

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wewewwatch.data.AppDatabase
import com.example.wewewwatch.data.MovieRepository
import com.example.wewewwatch.data.SearchMovie
import com.example.wewewwatch.data.WatchMovie
import com.example.wewewwatch.ui.theme.WEWEWWATCHTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WEWEWWATCHTheme {
                WeWatchApp()
            }
        }
    }
}

@Composable
private fun WeWatchApp() {
    val context = LocalContext.current
    val repository = remember {
        MovieRepository(AppDatabase.getInstance(context.applicationContext).movieDao())
    }
    val scope = rememberCoroutineScope()
    var route by remember { mutableStateOf<AppRoute>(AppRoute.Main) }
    var selectedMovie by remember { mutableStateOf<SearchMovie?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    when (val currentRoute = route) {
        AppRoute.Main -> MainScreen(
            repository = repository,
            refreshKey = refreshKey,
            onAddClick = {
                selectedMovie = null
                route = AppRoute.Add
            },
        )

        AppRoute.Add -> AddScreen(
            selectedMovie = selectedMovie,
            onBack = { route = AppRoute.Main },
            onSearchClick = { title, year ->
                route = AppRoute.Search(title.trim(), year.trim())
            },
            onAddMovie = { movie ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        repository.addMovie(movie)
                    }
                    refreshKey++
                    route = AppRoute.Main
                }
            },
        )

        is AppRoute.Search -> SearchScreen(
            query = currentRoute.query,
            year = currentRoute.year,
            repository = repository,
            onBack = { route = AppRoute.Add },
            onMovieSelected = { movie ->
                selectedMovie = movie
                route = AppRoute.Add
            },
        )
    }
}

private sealed interface AppRoute {
    data object Main : AppRoute
    data object Add : AppRoute
    data class Search(val query: String, val year: String) : AppRoute
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    repository: MovieRepository,
    refreshKey: Int,
    onAddClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var movies by remember { mutableStateOf(emptyList<WatchMovie>()) }
    val markedIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(refreshKey) {
        repository.observeWatchList().collect { storedMovies ->
            movies = storedMovies
            markedIds.removeAll { markedId -> storedMovies.none { it.imdbId == markedId } }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WeWatch") },
                actions = {
                    Button(
                        enabled = markedIds.isNotEmpty(),
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    repository.deleteMovies(markedIds.toSet())
                                }
                                markedIds.clear()
                            }
                        },
                    ) {
                        Text("Delete")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { innerPadding ->
        if (movies.isEmpty()) {
            EmptyWatchList(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        } else {
            MovieWatchList(
                movies = movies,
                markedIds = markedIds,
                onMarkedChange = { imdbId, checked ->
                    if (checked) {
                        if (!markedIds.contains(imdbId)) markedIds.add(imdbId)
                    } else {
                        markedIds.remove(imdbId)
                    }
                },
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddScreen(
    selectedMovie: SearchMovie?,
    onBack: () -> Unit,
    onSearchClick: (String, String) -> Unit,
    onAddMovie: (SearchMovie) -> Unit,
) {
    var title by remember(selectedMovie) { mutableStateOf(selectedMovie?.title.orEmpty()) }
    var year by remember(selectedMovie) { mutableStateOf(selectedMovie?.year.orEmpty()) }
    val canSearch = title.isNotBlank()
    val canAdd = title.isNotBlank() && year.isNotBlank()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add movie") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Movie title") },
                singleLine = true,
                isError = title.isBlank(),
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Release year") },
                singleLine = true,
            )
            if (selectedMovie != null) {
                MoviePoster(
                    posterUrl = selectedMovie.posterUrl,
                    modifier = Modifier
                        .size(width = 132.dp, height = 190.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    enabled = canSearch,
                    onClick = { onSearchClick(title, year) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Search")
                }
                Button(
                    enabled = canAdd,
                    onClick = {
                        onAddMovie(
                            selectedMovie ?: SearchMovie(
                                imdbId = "manual-${title.trim().lowercase()}-${year.trim()}",
                                title = title.trim(),
                                year = year.trim(),
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Add movie")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    query: String,
    year: String,
    repository: MovieRepository,
    onBack: () -> Unit,
    onMovieSelected: (SearchMovie) -> Unit,
) {
    var isLoading by remember(query, year) { mutableStateOf(true) }
    var errorMessage by remember(query, year) { mutableStateOf<String?>(null) }
    var movies by remember(query, year) { mutableStateOf(emptyList<SearchMovie>()) }

    LaunchedEffect(query, year) {
        isLoading = true
        errorMessage = null
        val result = withContext(Dispatchers.IO) {
            runCatching { repository.searchMovies(query, year).getOrThrow() }
        }
        result
            .onSuccess { movies = it }
            .onFailure { errorMessage = it.message ?: "Search failed" }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> SearchMessage(errorMessage.orEmpty())
                movies.isEmpty() -> SearchMessage("No movies found")
                else -> SearchMovieList(
                    movies = movies,
                    onMovieSelected = onMovieSelected,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SearchMessage(message: String) {
    Text(
        text = message,
        modifier = Modifier.padding(24.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun SearchMovieList(
    movies: List<SearchMovie>,
    onMovieSelected: (SearchMovie) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(movies, key = { it.imdbId }) { movie ->
            SearchMovieItem(
                movie = movie,
                onClick = { onMovieSelected(movie) },
            )
        }
    }
}

@Composable
private fun SearchMovieItem(
    movie: SearchMovie,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MoviePoster(posterUrl = movie.posterUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = movie.year,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (movie.genre.isNotBlank()) {
                    Text(
                        text = movie.genre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWatchList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        EmptyFrameIcon()
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No movies selected",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add a movie to your watch list.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyFrameIcon() {
    Box(
        modifier = Modifier
            .size(144.dp, 96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Frame",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MovieWatchList(
    movies: List<WatchMovie>,
    markedIds: List<String>,
    onMarkedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(movies, key = { it.imdbId }) { movie ->
            WatchMovieItem(
                movie = movie,
                checked = markedIds.contains(movie.imdbId),
                onCheckedChange = { onMarkedChange(movie.imdbId, it) },
            )
        }
    }
}

@Composable
private fun WatchMovieItem(
    movie: WatchMovie,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MoviePoster(posterUrl = movie.posterUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = movie.year,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun MoviePoster(
    posterUrl: String,
    modifier: Modifier = Modifier.size(width = 54.dp, height = 78.dp),
) {
    var bitmap by remember(posterUrl) { mutableStateOf<Bitmap?>(null) }
    val canLoadPoster = posterUrl.isNotBlank() && posterUrl != "N/A"

    LaunchedEffect(posterUrl) {
        bitmap = null
        if (canLoadPoster) {
            bitmap = withContext(Dispatchers.IO) {
                runCatching {
                    URL(posterUrl).openStream().use { input ->
                        BitmapFactory.decodeStream(input)
                    }
                }.getOrNull()
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop,
        )
    } else {
        PosterPlaceholder(modifier = modifier)
    }
}

@Composable
private fun PosterPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 54.dp, height = 78.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE7E0EC)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Poster",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF5D526A),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    WEWEWWATCHTheme {
        EmptyWatchList(modifier = Modifier.fillMaxSize())
    }
}
