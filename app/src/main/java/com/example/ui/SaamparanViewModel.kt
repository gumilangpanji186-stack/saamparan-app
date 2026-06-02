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
    private val _userSaldo = MutableStateFlow(2) // Saldo awal komentar
    val userSaldo: StateFlow<Int> = _userSaldo.asStateFlow()

    fun tambahSaldo() {
        _userSaldo.value += 3 // Menambah saldo komentar
    }

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

        // Buka profil pembuat bawaan sejak awal sesuai instruksi
        openCreatorProfile(currentSessionUsername)

        // Tangkap unggahan waktu nyata dan tampilkan spanduk notifikasi elegan
        viewModelScope.launch {
            repository.newPostNotification.collectLatest { newPost ->
                // Periksa apakah pengguna berlangganan wilayah tersebut atau berada di "Semua Lembur"
                val activeLembur = _selectedLembur.value
                if (activeLembur == "Semua Lembur" || activeLembur.equals(newPost.lembur, ignoreCase = true)) {
                    // Hanya perbarui daftar beranda utama
                    val currentList = _videoPosts.value.toMutableList()
                    currentList.add(0, newPost)
                    _videoPosts.value = currentList
                }
                
                // Selalu tampilkan spanduk notifikasi di dalam aplikasi
                _activeNotification.value = newPost
            }
        }

        // GOOGLE NEARBY CONNECTIONS API: Simulasi Jaringan Mesh Latar Belakang
        // Otomatis mencari, mendeteksi, menangkap, dan mengunduh file video 360p dari HP pengguna lain.
        // ATURAN MANDATORI: Mengalirkan data hibrida ini HANYA untuk memperbarui Beranda Utama (main feed).
        // Area Profil Kreator sama sekali tidak boleh dipengaruhi oleh aliran data mesh eksternal ini.
        startMeshNetworkSimulatedSync()
    }

    private fun startMeshNetworkSimulatedSync() {
        viewModelScope.launch {
            // Simulasi deteksi berkala setiap 50 detik dari perangkat sekitar melalui Nearby Connections API
            while (true) {
                kotlinx.coroutines.delay(50000)
                
                // Membuat payload postingan video 360p terkompresi dari peer pengguna lain
                val randomVillage = lemburs.filterNot { it == "Semua Lembur" }.random()
                val peerMeshPost = VideoPost(
                    id = "mesh_p2p_${java.util.UUID.randomUUID()}",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", // Representasi file video 360p hemat bandwidth
                    title = "Video Berbagi Lewat Jaringan Mesh Terdekat",
                    lembur = randomVillage,
                    creatorUsername = "warga_sekitar_${(1..5).random()}",
                    creatorName = "Warga Sumedang Sekitar",
                    creatorAvatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80",
                    creatorBio = "Kreator luar terhubung otomatis menggunakan Google Nearby Connections.",
                    likesCount = (10..55).random(),
                    isLiked = false,
                    comments = emptyList()
                )

                // ATURAN 2: Hanya aktif menyegarkan dan memperbarui daftar feed di Beranda Utama saja
                val currentLembur = _selectedLembur.value
                if (currentLembur == "Semua Lembur" || currentLembur.equals(peerMeshPost.lembur, ignoreCase = true)) {
                    val currentList = _videoPosts.value.toMutableList()
                    currentList.add(0, peerMeshPost)
                    _videoPosts.value = currentList
                }

                // Tampilkan spanduk notifikasi di atas layar utama
                _activeNotification.value = peerMeshPost
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
            
            // ATURAN 1: Pastikan daftar video yang muncul saat Kartu Profil ini aktif HANYA menampilkan video internal milik kreator itu sendiri.
            // Tidak boleh diprogram untuk ikut menangkap atau menampilkan video dari jaringan mesh luar.
            // Bersifat privat dan terkunci hanya untuk konten kreator yang bersangkutan.
            val filteredPosts = profile.posts.filter { it.creatorUsername == username }
            val strictlyCreatorProfile = profile.copy(posts = filteredPosts)
            
            _selectedCreatorProfile.value = strictlyCreatorProfile
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
        if (_userSaldo.value <= 0) return
        viewModelScope.launch {
            try {
                // Kurangi saldo komentar
                _userSaldo.value = (_userSaldo.value - 1).coerceAtLeast(0)

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
