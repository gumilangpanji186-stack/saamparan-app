package com.example.ui

import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.Comment
import com.example.model.UserProfile
import com.example.model.VideoPost
import kotlinx.coroutines.launch

// Color constants matching user specs
val KremPekat = Color(0xFFD2B48C)
val DeepBlack = Color(0xFF000000)
val SemiTranslucentDark = Color(0xE6101010)

// Warm Tone color constants
val CokelatTanahHangat = Color(0xFF3E2723)
val CokelatMudaHangat = Color(0xFF4E342E)
val KremPekatHangat = Color(0xFFD2B48C)
val KremKuningHangat = Color(0xFFE5C158)
val OranyeBataLembut = Color(0xFFFFCC80)
val MerahBataHangat = Color(0xFFD84315)
val SemburatKremHangat = Color(0xFFFFF3E0)

// Helper function to convert dynamic title & village info into Indonesian hashtags format
fun convertToHashtags(title: String, lembur: String): String {
    val clean = title.replace(Regex("[^a-zA-Z0-9 ]"), " ")
    val words = clean.split(Regex("\\s+"))
        .filter { it.isNotBlank() && it.length >= 3 }
        .map { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
    
    val hashtags = mutableListOf<String>()
    if (words.isNotEmpty()) {
        if (words.size > 1) {
            hashtags.add("#" + words.take(3).joinToString(""))
        } else {
            hashtags.add("#" + words[0])
        }
        words.take(3).forEach { word ->
            hashtags.add("#$word")
        }
    }
    if (lembur.isNotBlank()) {
        val cleanLembur = lembur.replace(Regex("[^a-zA-Z0-9]"), "")
        if (cleanLembur.isNotBlank()) {
            hashtags.add("#$cleanLembur")
        }
    }
    hashtags.add("#Sumedang")
    return hashtags.distinct().take(4).joinToString(" ")
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SaamparanApp(
    viewModel: SaamparanViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // States from view model
    val rawPosts by viewModel.videoPosts.collectAsState()
    val isProfileOpen by viewModel.isProfileViewOpen.collectAsState()
    val creatorProfile by viewModel.selectedCreatorProfile.collectAsState()
    val isUploadOpen by viewModel.isUploadDialogOpen.collectAsState()
    val activeNotification by viewModel.activeNotification.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userSaldo by viewModel.userSaldo.collectAsState()

    // 20 video FIFO constraint
    val displayedPosts = remember(rawPosts) {
        rawPosts.take(20)
    }

    // Media picker setup
    val context = LocalContext.current
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf<String?>(null) }
    
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        if (uri != null) {
            selectedVideoName = uri.lastPathSegment ?: "video_pilihan.mp4"
            viewModel.showUploadDialog()
        }
    }

    val safeLaunchVideoPicker = {
        try {
            videoLauncher.launch("video/*")
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(
                context,
                "Aplikasi pemilih video (Gallery/File Manager) tidak tersedia di perangkat ini.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Vertical Pager state
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { displayedPosts.size }
    )

    // Notify VM when active post changes
    LaunchedEffect(pagerState.currentPage, displayedPosts) {
        if (displayedPosts.isNotEmpty() && pagerState.currentPage in displayedPosts.indices) {
            viewModel.setActivePostIndex(pagerState.currentPage)
        }
    }

    // Modal drawer state for comments
    var showCommentsDrawer by remember { mutableStateOf(false) }
    var isKartuProfilOpen by remember { mutableStateOf(false) }

    // DEFINISI PALET WARNA HANGAT SAAMPARAN (ANTI-HITAM & ANTI-PUTIH)
    val warnaLatarCokelatTanah = Color(0xFF4A3B32)  // Pengganti Hitam Pekat
    val warnaKremLembutHangat = Color(0xFFF5F5DC)   // Pengganti Putih Standar (Ikon & Teks Atas)
    val warnaKremPekatKertas = Color(0xFFEFEBE9)    // Wadah Kartu Note Mini
    val warnaCokelatTeksTua = Color(0xFF2E241E)     // Teks di dalam Kartu Note

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(warnaLatarCokelatTanah)
    ) {
        if (displayedPosts.isNotEmpty()) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val currentPost = displayedPosts[page]
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // Fullscreen Video Player
                    VideoPlayerComponent(
                        videoUrl = currentPost.videoUrl,
                        onDoubleTap = { showCommentsDrawer = true },
                        onTap = { safeLaunchVideoPicker() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scrim overlay behind text for readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.4f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(0.85f))
                                )
                            )
                    )
                }
            }
        } else {
            // Empty state placeholder
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Belum ada video",
                        tint = warnaKremLembutHangat,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada konten video yang tersedia.",
                        color = warnaKremLembutHangat,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // KONTROL ATAS HALAMAN (DINAMIS & FULLSCREEN BLEND)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // POJOK ATAS KIRI: TEKS INDIKATOR HALAMAN
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (displayedPosts.isNotEmpty()) "${pagerState.currentPage + 1} dari ${displayedPosts.size}" else "1 dari 1",
                    color = warnaKremLembutHangat, // Teks Krem Lembut Hangat
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("active_user_nickname")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Green, shape = RoundedCornerShape(50)) // Radar Hijau Mikro
                )
            }

            // Foto Profil Mikro Pengguna (24dp) di pojok kanan atas
            AsyncImage(
                model = creatorProfile?.avatarUrl ?: "https://api.dicebear.com/7.x/adventurer/svg?seed=mojang_priangan",
                contentDescription = "Profil Sesi",
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(1.1.dp, warnaKremLembutHangat.copy(0.6f), CircleShape)
                    .clickable {
                        viewModel.openCreatorProfile(viewModel.currentSessionUsername)
                    }
                    .testTag("session_profile_avatar"),
                contentScale = ContentScale.Crop
            )
        }

        // ==========================================
        // SISI KANAN SAMPING: STRUKTUR STRIP TOMBOL KREM LENGKAP
        // ==========================================
        if (displayedPosts.isNotEmpty()) {
            val activePost = displayedPosts[pagerState.currentPage]
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp) // Jarak renggang antar-tombol yang presisi
            ) {
                // [ATAS]: TOMBOL LIKE / SUKA (WARNA KREM)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Suka Karya",
                        tint = if (activePost.isLiked) Color(0xFFE57373) else warnaKremLembutHangat,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { viewModel.toggleLikePost(activePost.id) }
                            .testTag("exchange_like_to_saldo_button")
                    )
                    Text(
                        text = activePost.likesCount.toString(), 
                        color = warnaKremLembutHangat, 
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // [TENGAH]: TOMBOL KOMENTAR / SAWALA (WARNA KREM)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Buka Sawala Komentar",
                        tint = warnaKremLembutHangat,
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { showCommentsDrawer = true }
                    )
                    Text(
                        text = activePost.comments.size.toString(), 
                        color = warnaKremLembutHangat, 
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // [TOMBOL UNGGAH/UNDUH DI SINI TELAH DIHAPUS TOTAL DEMI PRIVASI]

                // [BAWAH]: TOMBOL HOME / BERANDA (WARNA KREM)
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Kembali ke Beranda Utama",
                    tint = warnaKremLembutHangat,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable {
                            coroutineScope.launch {
                                // Run Radar search to find nearest video relative to the active post's lembur
                                val radarUseCase = com.example.domain.SearchNearbyVideosUseCase()
                                val nearestList = radarUseCase.findNearestVideos(displayedPosts, activePost.lembur)
                                
                                // Show nearby content detection in Indonesian format
                                val otherNearest = nearestList.filter { it.first.id != activePost.id }
                                    .take(2)
                                
                                val message = if (otherNearest.isNotEmpty()) {
                                    val itemsText = otherNearest.joinToString("\n") { 
                                        "📍 ${it.first.title} (${String.format(java.util.Locale.US, "%.1f", it.second)} km di ${it.first.lembur})" 
                                    }
                                    "📡 Radar Saamparan mendeteksi video terdekat dari ${activePost.lembur}:\n$itemsText"
                                } else {
                                    "📡 Radar Saamparan: Tidak ada video sekitar dalam jangkauan radar."
                                }
                                
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()

                                viewModel.selectLembur("Semua Lembur")
                                pagerState.scrollToPage(0)
                            }
                        }
                )
            }
        }

        // ==========================================
        // 3. POJOK KIRI BAWAH: KARTU PROFIL NOTE SANGAT KECIL
        // ==========================================
        if (isKartuProfilOpen && displayedPosts.isNotEmpty()) {
            val activePost = displayedPosts[pagerState.currentPage]
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .width(220.dp) // Ukuran diperkecil secara ekstrem (Sangat Mikro)
                    .background(
                        warnaKremPekatKertas, 
                        shape = RoundedCornerShape(topEnd = 10.dp) // Sudut melengkung kecil yang estetik
                    )
                    .padding(10.dp)
                    .testTag("creator_profile_floating_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SAAMPARAN",
                            fontWeight = FontWeight.Bold,
                            color = warnaCokelatTeksTua,
                            fontSize = 11.sp // Teks judul diperkecil
                        )
                        Text(
                            text = "✕",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { isKartuProfilOpen = false }
                                .padding(2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = activePost.creatorAvatarUrl,
                            contentDescription = "Foto Profil",
                            modifier = Modifier
                                .size(20.dp) // Foto mikro kreativitas di dalam kartu (20dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                                .clickable {
                                    viewModel.openCreatorProfile(activePost.creatorUsername)
                                }
                                .testTag("creator_profile_avatar_bottom"),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "@${activePost.creatorUsername}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = warnaCokelatTeksTua,
                                modifier = Modifier.clickable {
                                    viewModel.openCreatorProfile(activePost.creatorUsername)
                                }
                            )
                            Text(
                                text = "di ${activePost.lembur}",
                                fontSize = 9.sp, // Teks sub-lokasi diperkecil maksimal
                                color = warnaCokelatTeksTua.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // DOCKABLE CUSTOM SLIDING COMMENT DRAWER
        if (showCommentsDrawer && displayedPosts.isNotEmpty()) {
            val activePost = displayedPosts[pagerState.currentPage]
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.4f))
                    .clickable { showCommentsDrawer = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(CokelatTanahHangat) // COKELAT TANAH HANGAT
                        .border(
                            1.dp,
                            KremKuningHangat.copy(0.3f), // KREM PEKAT HANGAT
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .clickable(enabled = false) { }
                        .navigationBarsPadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header Drawer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sawala Komentar (${activePost.comments.size})",
                                color = OranyeBataLembut, // ORANYE BATA LEMBUT
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showCommentsDrawer = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = OranyeBataLembut // ORANYE BATA LEMBUT
                                )
                            }
                        }

                        // Baris Ekonomi & Penukaran Saldo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Saldo Komentar: $userSaldo token",
                                color = KremPekatHangat, // KREM PEKAT HANGAT
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Button(
                                onClick = { viewModel.tambahSaldo() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = KremKuningHangat, // KREM PEKAT HANGAT
                                    contentColor = CokelatTanahHangat // Cokelat tanah hangat
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("exchange_saldo_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Tukar Saldo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
 
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = OranyeBataLembut.copy(0.15f)
                        )

                        // Comment list
                        if (activePost.comments.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(activePost.comments) { comment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(KremPekatHangat.copy(0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = comment.username.take(1).uppercase(),
                                                color = KremPekatHangat, // KREM PEKAT HANGAT
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "@${comment.username}",
                                                    color = KremPekatHangat, // KREM PEKAT HANGAT
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = comment.timestamp,
                                                    color = KremPekatHangat.copy(0.5f),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Text(
                                                text = comment.text,
                                                color = SemburatKremHangat, // Semburat krem lembut hangat
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada komentar. Jadilah yang pertama!",
                                    color = KremPekatHangat.copy(0.6f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Input field tucked inside comments drawer
                        var commentText by remember { mutableStateOf("") }
                        val forbiddenInputs = listOf("halo", "salam", "kenalin", "nama saya", "perkenalkan", "dari kota", "asal", "follback")
                        val containsIntroSpam = remember(commentText) {
                            val lower = commentText.lowercase().trim()
                            forbiddenInputs.any { lower.contains(it) }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding()
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val sisaSaldoLike = userSaldo
                            if (sisaSaldoLike <= 0) {
                                // Jika sisaSaldoLike <= 0, kunci input teks komentar dan gantikan dengan Tombol Krem bertuliskan "Tukarkan 1 Like Karya Jadi Saldo Komentar"
                                Button(
                                    onClick = { viewModel.tambahSaldo() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = KremKuningHangat,
                                        contentColor = CokelatTanahHangat
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("exchange_like_to_saldo_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Tukarkan 1 Like Karya Jadi Saldo Komentar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = commentText,
                                        onValueChange = { commentText = it },
                                        enabled = sisaSaldoLike > 0,
                                        placeholder = {
                                            Text(
                                                text = "Tulis komentar...",
                                                color = KremPekatHangat.copy(0.5f),
                                                fontSize = 12.sp
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("comment_input_drawer"),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                                            color = SemburatKremHangat,
                                            fontSize = 12.sp
                                        ),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            cursorColor = KremKuningHangat,
                                            focusedBorderColor = if (containsIntroSpam) MerahBataHangat else KremKuningHangat,
                                            unfocusedBorderColor = if (containsIntroSpam) MerahBataHangat.copy(0.7f) else KremPekatHangat.copy(0.5f),
                                            focusedContainerColor = CokelatMudaHangat,
                                            unfocusedContainerColor = CokelatMudaHangat,
                                            disabledContainerColor = CokelatMudaHangat.copy(0.6f),
                                            disabledBorderColor = KremPekatHangat.copy(0.2f)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        maxLines = 2
                                    )

                                    IconButton(
                                        onClick = {
                                            if (commentText.isNotBlank() && !containsIntroSpam && sisaSaldoLike > 0) {
                                                viewModel.addComment(activePost.id, commentText)
                                                commentText = ""
                                            }
                                        },
                                        enabled = commentText.isNotBlank() && !containsIntroSpam && sisaSaldoLike > 0,
                                        modifier = Modifier.testTag("submit_comment_drawer_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Kirim",
                                            tint = if (commentText.isNotBlank() && !containsIntroSpam && sisaSaldoLike > 0) KremKuningHangat else KremKuningHangat.copy(0.3f)
                                        )
                                    }
                                }

                                if (containsIntroSpam) {
                                    Text(
                                        text = "Dilarang melakukan perkenalan! Komentar khusus hanya boleh membahas tema konten video kreator.",
                                        color = MerahBataHangat, // Merah bata senada dengan tema hangat
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // LOADING OVERLAY
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KremPekat)
            }
        }

        // REAL-TIME BROADCAST NOTIFICATION BANNER
        activeNotification?.let { notifPost ->
            NotificationBanner(
                post = notifPost,
                onDismiss = { viewModel.dismissNotification() }
            )
        }


    }

    // MODERN INTEGRATED UPLOAD FORM DIALOG (TRIGGERS FROM VIDEO LAUNCHER)
    if (isUploadOpen) {
        UploadDialog(
            lemburs = viewModel.lemburs.filter { it != "Semua Lembur" },
            selectedVideoUri = selectedVideoUri,
            selectedVideoName = selectedVideoName,
            onPickVideo = { safeLaunchVideoPicker() },
            onClearVideo = {
                selectedVideoUri = null
                selectedVideoName = null
                viewModel.hideUploadDialog()
            },
            onPost = { title, lembur, videoUrl ->
                viewModel.createNewPost(title, lembur, videoUrl)
                selectedVideoUri = null
                selectedVideoName = null
            },
            onDismiss = {
                viewModel.hideUploadDialog()
                selectedVideoUri = null
                selectedVideoName = null
            }
        )
    }


}

@Composable
fun VideoPlayerComponent(
    videoUrl: String,
    onDoubleTap: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var buffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // Auto-reject if resolution is detected above 360p (represented by "720p", "1080p", "high")
    val isAbove360p = remember(videoUrl) {
        videoUrl.contains("1080p", ignoreCase = true) || 
        videoUrl.contains("720p", ignoreCase = true) || 
        videoUrl.contains("high", ignoreCase = true)
    }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }

    LaunchedEffect(videoUrl) {
        buffering = !isAbove360p
        hasError = isAbove360p
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(videoUrl) {
                detectTapGestures(
                    onDoubleTap = {
                        onDoubleTap()
                    },
                    onTap = {
                        onTap()
                    }
                )
            }
    ) {
        if (!isAbove360p) {
            key(videoUrl) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = true
                                mediaPlayer.setVolume(0f, 0f) // Mute to keep it clean on start
                                if (isPlaying) {
                                    start()
                                }
                                buffering = false
                                hasError = false
                            }
                            setOnErrorListener { _, _, _ ->
                                buffering = false
                                hasError = true
                                true // Return true so that the native error alerts do not show up/cause crashes
                            }
                            try {
                                setVideoURI(Uri.parse(videoUrl))
                            } catch (e: Exception) {
                                buffering = false
                                hasError = true
                                e.printStackTrace()
                            }
                            videoViewRef = this
                        }
                    },
                    update = { view ->
                        if (isPlaying) {
                            view.start()
                        } else {
                            view.pause()
                        }
                    },
                    onRelease = { view ->
                        view.stopPlayback()
                        videoViewRef = null
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (buffering && !isAbove360p) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KremPekat)
            }
        }

        if (isAbove360p) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Auto-Reject: Resolusi di atas 360p terdeteksi! Hanya mendukung resolusi 360p offline.",
                    color = MerahBataHangat,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else if (hasError) {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak dapat memutar video ini / Offline (Hanya mendukung 360p)",
                    color = Color.White.copy(0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NotificationBanner(
    post: VideoPost,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(post) {
        kotlinx.coroutines.delay(4500)
        visible = false
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            colors = CardDefaults.cardColors(containerColor = SemiTranslucentDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, KremPekat.copy(0.3f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifikasi",
                    tint = KremPekat,
                    modifier = Modifier.size(24.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Konten Baru di Wilayah ${post.lembur}!",
                        color = KremPekat,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${post.title} - @${post.creatorUsername}",
                        color = Color.White.copy(0.9f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = {
                    visible = false
                    onDismiss()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = KremPekat,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UploadDialog(
    lemburs: List<String>,
    selectedVideoUri: Uri?,
    selectedVideoName: String?,
    onPickVideo: () -> Unit,
    onClearVideo: () -> Unit,
    onPost: (title: String, lembur: String, videoUrl: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf(if (lemburs.isNotEmpty()) lemburs[0] else "Situraja") }
    var expandedVillage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(SemiTranslucentDark)
                .border(1.dp, KremPekat.copy(0.25f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Unggah Video Konten Baru",
                    color = KremPekat,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Selected File Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(0.4f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Berkas Dipilih:",
                            color = KremPekat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedVideoName ?: "Tidak ada berkas terpilih",
                            color = Color.White.copy(0.85f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (selectedVideoUri != null) {
                        IconButton(onClick = onClearVideo) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = KremPekat,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = onPickVideo) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Pilih Berkas",
                                tint = KremPekat,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Title Input
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Judul Konten Video",
                        color = KremPekat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Tulis judul yang menarik...",
                                color = Color.White.copy(0.4f),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_title_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            cursorColor = KremPekat,
                            focusedBorderColor = KremPekat,
                            unfocusedBorderColor = KremPekat.copy(0.3f),
                            focusedContainerColor = Color.Black.copy(0.3f),
                            unfocusedContainerColor = Color.Black.copy(0.3f)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Village/Lembur Selector Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Pilih Wilayah",
                        color = KremPekat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(0.3f))
                                .border(1.dp, KremPekat.copy(0.3f), RoundedCornerShape(10.dp))
                                .clickable { expandedVillage = !expandedVillage }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedVillage,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = KremPekat
                            )
                        }

                        DropdownMenu(
                            expanded = expandedVillage,
                            onDismissRequest = { expandedVillage = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(SemiTranslucentDark)
                                .border(1.dp, KremPekat.copy(0.3f), RoundedCornerShape(8.dp))
                        ) {
                            lemburs.forEach { village ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = village,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        selectedVillage = village
                                        expandedVillage = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions Button footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Batal",
                            color = Color.White.copy(0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            onPost(title, selectedVillage, selectedVideoUri?.toString())
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KremPekat,
                            disabledContainerColor = KremPekat.copy(0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("upload_submit_button")
                    ) {
                        Text(
                            text = "Unggah",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorProfileFloatingCard(
    profile: UserProfile,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transisiDenyutRadar = rememberInfiniteTransition(label = "RadarDenyutMikro")
    val skalaDenyutRadar by transisiDenyutRadar.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SkalaDenyutRadar"
    )
    val transparansiDenyutRadar by transisiDenyutRadar.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TransparansiDenyutRadar"
    )

    Box(
        modifier = modifier
            .width(200.dp)
            .wrapContentHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(KremPekat)
            .border(1.2.dp, Color.Black.copy(0.35f), RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag("creator_profile_floating_card")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier.size(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF2E7D32).copy(alpha = transparansiDenyutRadar),
                                radius = (size.minDimension / 2) * skalaDenyutRadar
                            )
                            drawCircle(
                                color = Color(0xFF2E7D32),
                                radius = size.minDimension / 4
                            )
                        }
                    }
                    Text(
                        text = "SAAMPARAN",
                        color = Color.Black.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            HorizontalDivider(
                color = Color.Black.copy(0.15f),
                modifier = Modifier.fillMaxWidth().height(1.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profile.avatarUrl,
                    contentDescription = "Foto Profil",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "@${profile.username}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "(${profile.lembur})",
                        color = Color.Black.copy(0.6f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(0.1f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SAAMPARAN",
                        color = Color.Black.copy(0.75f),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
