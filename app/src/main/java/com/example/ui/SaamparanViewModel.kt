package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Comment
import com.example.model.UserProfile
import com.example.model.VideoPost
import com.example.repository.SaamparanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SaamparanViewModel : ViewModel() {

    private val repository = SaamparanRepository.getInstance()

    // Active logged-in user simulation
    val currentSessionUsername = "mojang_priangan"
    val currentSessionName = "Neng Geulis"

    // Lembur list
    val lemburs: List<String> = repository.getLemburs()

    // States
    private val _selectedLembur = MutableStateFlow("Semua Lembur")
    val selectedLembur: StateFlow<String> = _selectedLembur.asStateFlow()

    private val _videoPosts = MutableStateFlow<List<VideoPost>>(emptyList())
    val videoPosts: StateFlow<List<VideoPost>> = _videoPosts.asStateFlow()

    private val _activePostIndex = MutableStateFlow(0)
    val activePostIndex: StateFlow<Int> = _activePostIndex.asStateFlow()

    private val _selectedCreatorProfile = MutableStateFlow<UserProfile?>(null)
    val selectedCreatorProfile: StateFlow<UserProfile?> = _selectedCreatorProfile.asStateFlow()

    private val _isProfileViewOpen = MutableStateFlow(false)
    val isProfileViewOpen: StateFlow<Boolean> = _isProfileViewOpen.asStateFlow()

    private val _isUploadDialogOpen = MutableStateFlow(false)
    val isUploadDialogOpen: StateFlow<Boolean> = _isUploadDialogOpen.asStateFlow()

    // Notification banner state
    private val _activeNotification = MutableStateFlow<VideoPost?>(null)
    val activeNotification: StateFlow<VideoPost?> = _activeNotification.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPostsForLembur("Semua Lembur")

        // Open the user profile by default on startup as requested
        openCreatorProfile(currentSessionUsername)

        // Catch real-time simulated uploads and show high-fidelity notifications
        viewModelScope.launch {
            repository.newPostNotification.collectLatest { newPost ->
                // Check if current user is subscribed to this lembur or if on "Semua Lembur"
                val activeLembur = _selectedLembur.value
                if (activeLembur == "Semua Lembur" || activeLembur.equals(newPost.lembur, ignoreCase = true)) {
                    // Update the active video list to show the new post
                    val currentList = _videoPosts.value.toMutableList()
                    currentList.add(0, newPost)
                    _videoPosts.value = currentList
                }
                
                // Always show elegant in-app notification banner
                _activeNotification.value = newPost
            }
        }
    }

    fun selectLembur(lembur: String) {
        _selectedLembur.value = lembur
        _activePostIndex.value = 0
        loadPostsForLembur(lembur)
    }

    private fun loadPostsForLembur(lembur: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val posts = repository.getPosts(lembur)
            _videoPosts.value = posts
            _isLoading.value = false
        }
    }

    fun setActivePostIndex(index: Int) {
        if (index in 0 until _videoPosts.value.size) {
            _activePostIndex.value = index
        }
    }

    fun openCreatorProfile(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val profile = repository.getUserProfile(username)
            _selectedCreatorProfile.value = profile
            _isProfileViewOpen.value = true
            _isLoading.value = false
        }
    }

    fun closeProfileView() {
        _isProfileViewOpen.value = false
    }

    fun toggleLikePost(postId: String) {
        viewModelScope.launch {
            try {
                val updatedPost = repository.likePost(postId)
                // Sync current post list
                val list = _videoPosts.value.map { if (it.id == postId) updatedPost else it }
                _videoPosts.value = list
                
                // Update creator profile posts if open
                val currentProfile = _selectedCreatorProfile.value
                if (currentProfile != null && currentProfile.username == updatedPost.creatorUsername) {
                    val updatedProfilePosts = currentProfile.posts.map { if (it.id == postId) updatedPost else it }
                    _selectedCreatorProfile.value = currentProfile.copy(posts = updatedProfilePosts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addComment(postId: String, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            try {
                val comment = repository.addComment(
                    postId = postId,
                    text = commentText,
                    username = currentSessionUsername,
                    name = currentSessionName
                )
                // Sync comments locally
                val updatedPosts = _videoPosts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(comments = post.comments + comment)
                    } else post
                }
                _videoPosts.value = updatedPosts

                // Also update profile view if active
                val currentProfile = _selectedCreatorProfile.value
                val activePost = updatedPosts.firstOrNull { it.id == postId }
                if (currentProfile != null && activePost != null) {
                    val updatedProfilePosts = currentProfile.posts.map { if (it.id == postId) activePost else it }
                    _selectedCreatorProfile.value = currentProfile.copy(posts = updatedProfilePosts)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissNotification() {
        _activeNotification.value = null
    }

    fun showUploadDialog() {
        _isUploadDialogOpen.value = true
    }

    fun hideUploadDialog() {
        _isUploadDialogOpen.value = false
    }

    fun createNewPost(title: String, lembur: String, customVideoUrl: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            // High fidelity video lists
            val verticalVideos = listOf(
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
            )
            val selectedUrl = customVideoUrl ?: verticalVideos.random()
            
            repository.uploadVideoPost(
                title = title,
                lembur = lembur,
                videoUrl = selectedUrl,
                creatorUsername = currentSessionUsername,
                creatorName = currentSessionName
            )
            _isUploadDialogOpen.value = false
        }
    }
}
