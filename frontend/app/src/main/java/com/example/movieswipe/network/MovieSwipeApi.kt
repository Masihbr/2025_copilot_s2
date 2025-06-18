package com.example.movieswipe.network

import retrofit2.http.*
import retrofit2.Response

// --- Data models (simplified for brevity) ---
data class UserInfo(val id: String, val email: String, val name: String?)
data class Group(val id: String, val name: String, val inviteCode: String, val members: List<UserInfo>, val ownerId: String)
data class GroupList(val groups: List<Group>)
data class Genre(val id: String, val name: String)
data class Movie(val id: String, val title: String, val overview: String?, val posterPath: String?)
data class VotingSession(val sessionId: String, val groupId: String, val movies: List<Movie>, val status: String)
data class VotingResult(val matchedMovie: Movie?, val votes: Map<String, String>)

// --- API interface ---
interface MovieSwipeApi {
    @GET("/user/me")
    suspend fun getCurrentUser(): Response<UserInfo>

    @POST("/user/preferences")
    suspend fun setPreferences(@Body body: Map<String, List<String>>): Response<Unit>

    @POST("/group/create")
    suspend fun createGroup(@Body body: Map<String, String>): Response<Group>

    @POST("/group/join")
    suspend fun joinGroup(@Body body: Map<String, String>): Response<Group>

    @GET("/group/{groupId}")
    suspend fun getGroup(@Path("groupId") groupId: String): Response<Group>

    @DELETE("/group/{groupId}")
    suspend fun deleteGroup(@Path("groupId") groupId: String): Response<Unit>

    @GET("/group/{groupId}/recommend")
    suspend fun getRecommendedGenres(@Path("groupId") groupId: String): Response<List<Genre>>

    @GET("/movie/genres")
    suspend fun getGenres(): Response<List<Genre>>

    @GET("/movie/discover")
    suspend fun discoverMovies(@Query("genres") genres: String?, @Query("page") page: Int?): Response<List<Movie>>

    @GET("/movie/movie/{movieId}")
    suspend fun getMovieDetails(@Path("movieId") movieId: String): Response<Movie>

    @POST("/voting/start")
    suspend fun startVoting(@Body body: Map<String, Any>): Response<VotingSession>

    @POST("/voting/end")
    suspend fun endVoting(@Body body: Map<String, String>): Response<Unit>

    @POST("/voting/vote")
    suspend fun vote(@Body body: Map<String, String>): Response<Unit>

    @GET("/voting/results/{sessionId}")
    suspend fun getVotingResults(@Path("sessionId") sessionId: String): Response<VotingResult>
}

val api: MovieSwipeApi = ApiClient.retrofit.create(MovieSwipeApi::class.java)

