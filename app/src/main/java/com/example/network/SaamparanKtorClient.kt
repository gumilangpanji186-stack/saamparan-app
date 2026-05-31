package com.example.network

import android.util.Log
import com.example.model.Comment
import com.example.model.UserProfile
import com.example.model.VideoPost
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.concurrent.TimeUnit

object SaamparanKtorClient {
    private const val BASE_URL = "https://api.saamparan.com/v1"

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(5, TimeUnit.SECONDS)
                readTimeout(5, TimeUnit.SECONDS)
                writeTimeout(5, TimeUnit.SECONDS)
            }
        }
    }

    suspend fun getPosts(lembur: String?): List<VideoPost> {
        Log.d("SaamparanKtorClient", "Ktor GET: $BASE_URL/posts")
        val responseText = client.get("$BASE_URL/posts") {
            if (lembur != null) {
                parameter("lembur", lembur)
            }
        }.bodyAsText()

        val type = Types.newParameterizedType(List::class.java, VideoPost::class.java)
        val adapter = moshi.adapter<List<VideoPost>>(type)
        return adapter.fromJson(responseText) ?: emptyList()
    }

    suspend fun likePost(id: String, liked: Boolean): VideoPost {
        Log.d("SaamparanKtorClient", "Ktor POST: $BASE_URL/posts/$id/like")
        val mapAdapter = moshi.adapter<Map<String, Boolean>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Boolean::class.java)
        )
        val jsonPayload = mapAdapter.toJson(mapOf("liked" to liked))

        val responseText = client.post("$BASE_URL/posts/$id/like") {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }.bodyAsText()

        val adapter = moshi.adapter(VideoPost::class.java)
        return adapter.fromJson(responseText) ?: throw Exception("Failed to parse like response")
    }

    suspend fun addComment(id: String, text: String, username: String): Comment {
        Log.d("SaamparanKtorClient", "Ktor POST: $BASE_URL/posts/$id/comments")
        val payload = mapOf("text" to text, "username" to username)
        val mapAdapter = moshi.adapter<Map<String, String>>(
            Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
        )
        val jsonPayload = mapAdapter.toJson(payload)

        val responseText = client.post("$BASE_URL/posts/$id/comments") {
            contentType(ContentType.Application.Json)
            setBody(jsonPayload)
        }.bodyAsText()

        val adapter = moshi.adapter(Comment::class.java)
        return adapter.fromJson(responseText) ?: throw Exception("Failed to parse comment response")
    }

    suspend fun getUserProfile(username: String): UserProfile {
        Log.d("SaamparanKtorClient", "Ktor GET: $BASE_URL/users/$username")
        val responseText = client.get("$BASE_URL/users/$username").bodyAsText()
        val adapter = moshi.adapter(UserProfile::class.java)
        return adapter.fromJson(responseText) ?: throw Exception("Failed to parse profile response")
    }

    suspend fun getLemburList(): List<String> {
        Log.d("SaamparanKtorClient", "Ktor GET: $BASE_URL/lemburs")
        val responseText = client.get("$BASE_URL/lemburs").bodyAsText()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(responseText) ?: emptyList()
    }
}
