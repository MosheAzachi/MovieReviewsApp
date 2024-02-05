import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieResponse(
    @Json(name = "results")
    val results: List<Movie>
)

@JsonClass(generateAdapter = true)
data class Movie(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "title")
    val title: String?,
    @Json(name = "poster_path")
    val poster_path: String?,
    @Json(name = "vote_average")
    val vote_average: Double?,
    @Json(name = "overview")
    val overview: String?,
    @Json(name = "release_date")
    val release_date: String?
) {
    constructor() : this(null, null, null, null, null, null)
}