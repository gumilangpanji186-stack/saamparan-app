package com.example.network

import com.example.model.Comment
import com.example.model.UserProfile
import com.example.model.VideoPost
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface SaamparanApiService {

    @GET("v1/posts")
    suspend fun getPosts(
        @Query("lembur") lembur: String? = null
    ): List<VideoPost>

    @POST("v1/posts/{id}/like")
    suspend fun likePost(
        @Path("id") id: String,
        @Body requestBody: Map<String, Boolean>
    ): VideoPost

    @FormUrlEncoded
    @POST("v1/posts/{id}/comments")
    suspend fun addComment(
        @Path("id") id: String,
        @Field("text") text: String,
        @Field("username") username: String
    ): Comment

    @GET("v1/users/{username}")
    suspend fun getUserProfile(
        @Path("username") username: String
    ): UserProfile

    @GET("v1/lemburs")
    suspend fun getLemburList(): List<String>
}

object RetrofitClient {
    // A cloud-ready base URL pointing to a scalable production API
    private const val BASE_URL = "https://api.saamparan.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: SaamparanApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SaamparanApiService::class.java)
    }
}
