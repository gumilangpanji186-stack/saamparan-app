package com.example.repository

import android.util.Log
import com.example.model.Comment
import com.example.model.UserProfile
import com.example.model.VideoPost
import com.example.network.SaamparanKtorClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

class SaamparanRepository private constructor() {

    // Notification broadcast for new posts
    private val _newPostNotification = MutableSharedFlow<VideoPost>(extraBufferCapacity = 10)
    val newPostNotification: SharedFlow<VideoPost> = _newPostNotification.asSharedFlow()

    // Local in-memory state representing remote-like storage for seamless fallback
    private var localPosts = mutableListOf<VideoPost>()
    private var localUsers = mutableMapOf<String, UserProfile>()

    init {
        initializeFallbackData()
    }

    private fun initializeFallbackData() {
        val commentsPost1 = listOf(
            Comment("c1", "teh_ratih", "Ratih Sukmawati", "Asli seger pisan kang, tiis cai sumur oge eleh! 😍", "2 jam lalu"),
            Comment("c2", "mang_didin", "Didin Sumedang", "Saturasi hejo na mantap pisan. Dimana eta percisna?", "1 jam lalu")
        )

        val commentsPost2 = listOf(
            Comment("c3", "kabayan_is_back", "Asep Kabayan", "Ditiup angin sawah bari ngadahar simping meujeuhna pisan neng! 🌾", "3 jam lalu"),
            Comment("c4", "sumedang_creative", "Cecep Saamparan", "Cimalaka emang rajana sawah subur!", "30 mnt lalu")
        )

        val p1 = VideoPost(
            id = "post_1",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            title = "Keindahan curug Situraja",
            lembur = "Situraja",
            creatorUsername = "kabayan_is_back",
            creatorName = "Asep Kabayan",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Moal loba carita, urang Sumedang asli resep ulin ka leuweung jeung nyaba lembur kuring. Sampurasun!",
            likesCount = 142,
            isLiked = false,
            comments = commentsPost1
        )

        val p2 = VideoPost(
            id = "post_2",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            title = "Sawah asri Cimalaka",
            lembur = "Cimalaka",
            creatorUsername = "neng_lisna",
            creatorName = "Lisnawati",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Resep masak, jalan-jalan di sawah, tur reueus janten mojang Sumedang. Hayu sami-sami urang ngamajukeun lembur!",
            likesCount = 98,
            isLiked = true,
            comments = commentsPost2
        )

        val p3 = VideoPost(
            id = "post_3",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            title = "Bintang malam Ganeas",
            lembur = "Ganeas",
            creatorUsername = "sumedang_creative",
            creatorName = "Cecep Saamparan",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Kreator konten hyperlocal khusus ngeunaan wisata, kuliner, sarta kaendahan alam lembur urang Sumedang.",
            likesCount = 203,
            isLiked = false,
            comments = listOf(
                Comment("c5", "neng_lisna", "Lisnawati", "Sok hoyong camping di Ganeas ari ningali langit bengras kieu mah kang!", "4 jam lalu")
            )
        )

        val p4 = VideoPost(
            id = "post_4",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            title = "Sinar pagi hutan",
            lembur = "Sumedang Utara",
            creatorUsername = "mang_didin",
            creatorName = "Didin Sumedang",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Juru foto amatir nu resep neangan sela barna cahya isuk-isuk di sakuliah Sumedang Utara. Hatur nuhun.",
            likesCount = 76,
            isLiked = false,
            comments = emptyList()
        )

        val p5 = VideoPost(
            id = "post_5",
            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            title = "Kesegaran Air Tampomas",
            lembur = "Sumedang Selatan",
            creatorUsername = "teh_ratih",
            creatorName = "Ratih Sukmawati",
            creatorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Spesialis kulinér sarta ulin alam. Hirup mah kudu loba seuri, loba ulin, sarta asih ka sasama mahluk di lembur. 💚",
            likesCount = 312,
            isLiked = true,
            comments = listOf(
                Comment("c6", "mang_didin", "Didin Sumedang", "Lokasi pastina palih mana teh? Curug na herang pisan!", "5 jam lalu"),
                Comment("c7", "kabayan_is_back", "Asep Kabayan", "Ke atuh urang nyieun liwet di dinya teh, sedep sigana haneut-haneut amun beuteung lapar ahahaha", "2 jam lalu")
            )
        )

        localPosts.addAll(listOf(p1, p2, p3, p4, p5))

        // Populate users map
        for (post in localPosts) {
            val userPosts = localPosts.filter { it.creatorUsername == post.creatorUsername }
            localUsers[post.creatorUsername] = UserProfile(
                username = post.creatorUsername,
                name = post.creatorName,
                bio = post.creatorBio,
                avatarUrl = post.creatorAvatarUrl,
                lembur = post.lembur,
                followersCount = (150..1200).random(),
                followingCount = (100..400).random(),
                posts = userPosts
            )
        }
    }

    suspend fun getPosts(lembur: String?): List<VideoPost> {
        return try {
            Log.d("SaamparanRepository", "Attempting cloud query for posts with lembur=$lembur")
            SaamparanKtorClient.getPosts(lembur)
        } catch (e: Exception) {
            Log.w("SaamparanRepository", "API failure. Falling back to high-fidelity local content state.", e)
            if (lembur.isNullOrEmpty() || lembur == "Semua Lembur") {
                localPosts
            } else {
                localPosts.filter { it.lembur.equals(lembur, ignoreCase = true) }
            }
        }
    }

    suspend fun likePost(postId: String): VideoPost {
        return try {
            val endpointResult = SaamparanKtorClient.likePost(postId, true)
            // Sync local state as well
            val index = localPosts.indexOfFirst { it.id == postId }
            if (index != -1) {
                localPosts[index] = endpointResult
            }
            endpointResult
        } catch (e: Exception) {
            Log.w("SaamparanRepository", "Like API failed. Modifying local in-memory state.", e)
            val index = localPosts.indexOfFirst { it.id == postId }
            if (index != -1) {
                val post = localPosts[index]
                val updatedLiked = !post.isLiked
                val updatedLikesCount = post.likesCount + if (updatedLiked) 1 else -1
                val updatedPost = post.copy(isLiked = updatedLiked, likesCount = updatedLikesCount)
                localPosts[index] = updatedPost
                
                // Keep creator posts synced
                updateCreatorProfile(updatedPost.creatorUsername)

                updatedPost
            } else {
                throw NoSuchElementException("Post with id $postId not found in local cache.")
            }
        }
    }

    suspend fun addComment(postId: String, text: String, username: String, name: String): Comment {
        return try {
            val comment = SaamparanKtorClient.addComment(postId, text, username)
            // Sync local cache
            val index = localPosts.indexOfFirst { it.id == postId }
            if (index != -1) {
                val post = localPosts[index]
                val updatedComments = post.comments + comment
                localPosts[index] = post.copy(comments = updatedComments)
                updateCreatorProfile(post.creatorUsername)
            }
            comment
        } catch (e: Exception) {
            Log.w("SaamparanRepository", "Comment API failed. Falling back to local state.", e)
            val id = "c_" + UUID.randomUUID().toString()
            val comment = Comment(
                id = id,
                username = username,
                name = name,
                text = text,
                timestamp = "Baru saja"
            )
            val index = localPosts.indexOfFirst { it.id == postId }
            if (index != -1) {
                val post = localPosts[index]
                val updatedComments = post.comments + comment
                localPosts[index] = post.copy(comments = updatedComments)
                updateCreatorProfile(post.creatorUsername)
                comment
            } else {
                throw NoSuchElementException("Post with id $postId not found in local cache.")
            }
        }
    }

    suspend fun getUserProfile(username: String): UserProfile {
        return try {
            SaamparanKtorClient.getUserProfile(username)
        } catch (e: Exception) {
            Log.w("SaamparanRepository", "User Profile API failed. Returning cached state.", e)
            localUsers[username] ?: run {
                // Return a template user
                val defaultProfile = UserProfile(
                    username = username,
                    name = username.replaceFirstChar { it.uppercase() },
                    bio = "Anggota perkumpulan rasa cinta lembur kuring.",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                    lembur = "Situraja",
                    followersCount = 45,
                    followingCount = 120,
                    posts = emptyList()
                )
                localUsers[username] = defaultProfile
                defaultProfile
            }
        }
    }

    fun getLemburs(): List<String> {
        return listOf(
            "Semua Lembur",
            "Situraja",
            "Cimalaka",
            "Ganeas",
            "Sumedang Utara",
            "Sumedang Selatan",
            "Tanjungsari",
            "Jatinangor"
        )
    }

    // High fidelity simulator to add content and trigger instant update notification for that area!
    suspend fun uploadVideoPost(
        title: String,
        lembur: String,
        videoUrl: String,
        creatorUsername: String,
        creatorName: String
    ): VideoPost {
        // Build post
        val newId = "post_" + UUID.randomUUID().toString()
        val newPost = VideoPost(
            id = newId,
            videoUrl = videoUrl,
            title = title,
            lembur = lembur,
            creatorUsername = creatorUsername,
            creatorName = creatorName,
            creatorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80",
            creatorBio = "Kreator aktif anu resep babagi kahadean di lembur $lembur.",
            likesCount = 0,
            isLiked = false,
            comments = emptyList()
        )

        // Add to local state
        localPosts.add(0, newPost)
        updateCreatorProfile(creatorUsername)

        // Dispatch instant notification
        _newPostNotification.emit(newPost)
        Log.d("SaamparanRepository", "Instantly updated local cache with ${newPost.id} and dispatched notifications!")
        return newPost
    }

    private fun updateCreatorProfile(username: String) {
        val userPosts = localPosts.filter { it.creatorUsername == username }
        val existingProfile = localUsers[username]
        if (existingProfile != null) {
            localUsers[username] = existingProfile.copy(posts = userPosts)
        }
    }

    companion object {
        @Volatile
        private var instance: SaamparanRepository? = null

        fun getInstance(): SaamparanRepository {
            return instance ?: synchronized(this) {
                instance ?: SaamparanRepository().also { instance = it }
            }
        }
    }
}
