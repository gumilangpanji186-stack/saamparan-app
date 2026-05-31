package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Comment(
    val id: String,
    val username: String,
    val name: String,
    val text: String,
    val timestamp: String
)

@JsonClass(generateAdapter = true)
data class VideoPost(
    val id: String,
    val videoUrl: String,
    val title: String,
    val lembur: String,
    val creatorUsername: String,
    val creatorName: String,
    val creatorAvatarUrl: String,
    val creatorBio: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val comments: List<Comment> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UserProfile(
    val username: String,
    val name: String,
    val bio: String,
    val avatarUrl: String,
    val lembur: String,
    val followersCount: Int,
    val followingCount: Int,
    val posts: List<VideoPost> = emptyList()
)
