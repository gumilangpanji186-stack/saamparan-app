package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey val id: String,
    val videoUrl: String,
    val title: String,
    val lembur: String,
    val creatorUsername: String,
    val creatorName: String,
    val creatorAvatarUrl: String,
    val creatorBio: String,
    val downloadTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "creator_likes")
data class CreatorLikeEntity(
    @PrimaryKey val creatorUsername: String,
    val likesCount: Int
)
