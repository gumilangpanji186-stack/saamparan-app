package com.example.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY downloadTimestamp DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos ORDER BY downloadTimestamp DESC")
    suspend fun getAllVideos(): List<VideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Query("SELECT * FROM videos ORDER BY downloadTimestamp ASC LIMIT :limit")
    suspend fun getOldestVideos(limit: Int): List<VideoEntity>

    @Query("DELETE FROM videos WHERE id IN (:ids)")
    suspend fun deleteVideosByIds(ids: List<String>)

    @Query("DELETE FROM videos")
    suspend fun clearAllVideos()
}

@Dao
interface CreatorLikeDao {
    @Query("SELECT * FROM creator_likes WHERE creatorUsername = :username")
    suspend fun getCreatorLike(username: String): CreatorLikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCreatorLike(creatorLike: CreatorLikeEntity)

    @Query("SELECT * FROM creator_likes")
    suspend fun getAllCreatorLikes(): List<CreatorLikeEntity>
}
