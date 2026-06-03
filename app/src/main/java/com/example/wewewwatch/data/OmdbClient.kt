package com.example.wewewwatch.data

import com.example.wewewwatch.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class OmdbClient(
    private val apiKey: String = BuildConfig.OMDB_API_KEY,
) {
    fun searchMovies(query: String, year: String): Result<List<SearchMovie>> {
        if (apiKey.isBlank()) {
            return Result.failure(IllegalStateException("Set OMDB_API_KEY in local.properties"))
        }

        val searchJson = request(
            mapOf(
                "s" to query,
                "y" to year,
                "type" to "movie",
            ),
        )
        if (!searchJson.optBoolean("Response")) {
            return Result.failure(IllegalStateException(searchJson.optString("Error", "Movie not found")))
        }

        val searchItems = searchJson.optJSONArray("Search") ?: return Result.success(emptyList())
        val movies = buildList {
            for (index in 0 until searchItems.length()) {
                val item = searchItems.getJSONObject(index)
                val imdbId = item.optString("imdbID")
                val details = request(mapOf("i" to imdbId))
                add(
                    SearchMovie(
                        imdbId = imdbId,
                        title = details.optString("Title", item.optString("Title")),
                        year = details.optString("Year", item.optString("Year")),
                        genre = details.optString("Genre"),
                        posterUrl = details.optString("Poster"),
                    ),
                )
            }
        }
        return Result.success(movies)
    }

    private fun request(params: Map<String, String>): JSONObject {
        val query = (params + ("apikey" to apiKey))
            .filterValues { it.isNotBlank() }
            .map { (key, value) ->
                "${key}=${URLEncoder.encode(value, Charsets.UTF_8.name())}"
            }
            .joinToString("&")
        val connection = URL("$BASE_URL?$query").openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.requestMethod = "GET"

        return connection.inputStream.bufferedReader().use { reader ->
            JSONObject(reader.readText())
        }
    }

    companion object {
        private const val BASE_URL = "https://www.omdbapi.com/"
        private const val TIMEOUT_MS = 15_000
    }
}
