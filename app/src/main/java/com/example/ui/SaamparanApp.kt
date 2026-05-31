package com.example.ui

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Videocam
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.model.VideoPost
import com.example.model.UserProfile
import com.example.model.Comment
import com.example.ui.theme.DarkSoil
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.EarthBrownDark
import com.example.ui.theme.EarthBrownLight
import com.example.ui.theme.WarmIvoryBg
import com.example.ui.theme.WarmIvorySurface
import com.example.ui.theme.WarmGray
import com.example.ui.theme.LeafGreenDark
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaamparanApp(
    viewModel: SaamparanViewModel,
    modifier: Modifier = Modifier
) {
    val selectedLembur by viewModel.selectedLembur.collectAsState()
    val posts by viewModel.videoPosts.collectAsState()
    val activeIndex by viewModel.activePostIndex.collectAsState()
    val isProfileOpen by viewModel.isProfileViewOpen.collectAsState()
    val selectedProfile by viewModel.selectedCreatorProfile.collectAsState()
    val isUploadOpen by viewModel.isUploadDialogOpen.collectAsState()
    val activeNotification by viewModel.activeNotification.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf<String?>(null) }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
        if (uri != null) {
            selectedVideoName = uri.lastPathSegment ?: "video_galeri_pilihan.mp4"
        }
    }

    val activePost = if (posts.isNotEmpty() && activeIndex in posts.indices) {
        posts[activeIndex]
    } else {
        null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmIvoryBg
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main Content Column: Top warm cream bar, Middle Video player, Bottom warm cream bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WarmIvoryBg)
            ) {
                // 1. BAGIAN ATAS: Krem Hangat bar containing:
                // - Minimalist profile of currently playing video uploader (or placeholder if empty)
                // - Dropdown filter for "Lembur"
                // - "Profil Saya" button to open the Profile overlay
                Surface(
                    color = WarmIvoryBg,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Creator Profile Info (Minimal)
                        if (activePost != null) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.openCreatorProfile(activePost.creatorUsername) }
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = activePost.creatorAvatarUrl,
                                    contentDescription = "Avatar ${activePost.creatorName}",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, EarthBrownDark, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = activePost.creatorName,
                                        color = DarkSoil,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = EarthBrown,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Lembur ${activePost.lembur}",
                                            color = EarthBrown,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            // Sawah placeholder when empty
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = EarthBrown,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SAAMPARAN",
                                    color = DarkSoil,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right side: Lembur selector + My Profile access
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dropdown selector for Lembur (Village)
                            Surface(
                                color = WarmIvorySurface,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight),
                                modifier = Modifier
                                    .clickable { dropdownExpanded = true }
                                    .testTag("lembur_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = EarthBrownDark,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = selectedLembur,
                                        color = EarthBrownDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 100.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown Lembur",
                                        tint = EarthBrownDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // "Profil Saya" button
                            IconButton(
                                onClick = {
                                    viewModel.openCreatorProfile(viewModel.currentSessionUsername)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(EarthBrownLight.copy(alpha = 0.3f), CircleShape)
                                    .testTag("my_profile_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Profil Saya",
                                    tint = EarthBrownDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.background(WarmIvorySurface)
                ) {
                    viewModel.lemburs.forEach { lembur ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = lembur,
                                    color = DarkSoil,
                                    fontWeight = if (lembur == selectedLembur) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.selectLembur(lembur)
                                dropdownExpanded = false
                            },
                            modifier = Modifier.testTag("lembur_item_$lembur")
                        )
                    }
                }

                // 2. BAGIAN TENGAH: Full video player taking available space
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                ) {
                    if (isLoading && posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = EarthBrownDark)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nuju nyandak video ti sawah...",
                                    color = WarmIvorySurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else if (posts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Videocam,
                                    contentDescription = null,
                                    tint = EarthBrownLight,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Teu aya video di area $selectedLembur",
                                    color = WarmIvoryBg,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Mangga ketok tombol Profil (ikon katuhu luhur) atanapi pilih Lembur sanesna.",
                                    color = EarthBrownLight,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }
                        }
                    } else {
                        activePost?.let { post ->
                            VideoPlayerComponent(
                                videoUrl = post.videoUrl,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // 3. BAGIAN BAWAH: Krem Hangat bar containing only the title
                if (activePost != null) {
                    Surface(
                        color = WarmIvoryBg,
                        modifier = Modifier
                            .fillMaxWidth(),
                        shadowElevation = 0.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = activePost.title,
                                color = DarkSoil,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }

            // Real-time notification system
            AnimatedVisibility(
                visible = activeNotification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                activeNotification?.let { notif ->
                    NotificationBanner(
                        title = notif.title,
                        lembur = notif.lembur,
                        creator = notif.creatorName,
                        onAction = {
                            val index = posts.indexOfFirst { it.id == notif.id }
                            if (index != -1) {
                                viewModel.setActivePostIndex(index)
                            } else {
                                viewModel.selectLembur(notif.lembur)
                                viewModel.setActivePostIndex(0)
                            }
                            viewModel.dismissNotification()
                        },
                        onDismiss = { viewModel.dismissNotification() }
                    )
                }
            }

            // Interactive Profile Overlay sheet containing ALL interaction buttons, navigation control deck, and upload panel!
            AnimatedVisibility(
                visible = isProfileOpen && selectedProfile != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                selectedProfile?.let { profile ->
                    val currentPost = posts.firstOrNull { it.id == activePost?.id } ?: activePost
                    UserProfileOverlay(
                        profile = profile,
                        activeVideoPost = currentPost,
                        activePostIndex = activeIndex,
                        totalPostsSize = posts.size,
                        currentUsername = viewModel.currentSessionUsername,
                        currentName = viewModel.currentSessionName,
                        allPosts = posts,
                        onSelectVideo = { post ->
                            val index = posts.indexOfFirst { it.id == post.id }
                            if (index != -1) {
                                viewModel.setActivePostIndex(index)
                            }
                            viewModel.closeProfileView()
                        },
                        onPrevVideo = {
                            if (activeIndex > 0) viewModel.setActivePostIndex(activeIndex - 1)
                        },
                        onNextVideo = {
                            if (activeIndex < posts.size - 1) viewModel.setActivePostIndex(activeIndex + 1)
                        },
                        onLikeClicked = { currentPost?.id?.let { viewModel.toggleLikePost(it) } },
                        onCommentPosted = { txt -> currentPost?.id?.let { viewModel.addComment(it, txt) } },
                        onUploadRequested = { viewModel.showUploadDialog() },
                        onBack = { viewModel.closeProfileView() }
                    )
                }
            }

            // Create Video Dialog Screen
            if (isUploadOpen) {
                UploadDialog(
                    lemburs = viewModel.lemburs.filter { it != "Semua Lembur" },
                    selectedVideoUri = selectedVideoUri,
                    selectedVideoName = selectedVideoName,
                    onPickVideo = { videoLauncher.launch("video/*") },
                    onClearVideo = {
                        selectedVideoUri = null
                        selectedVideoName = null
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
    }
}

@Composable
fun VideoPlayerComponent(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var buffering by remember { mutableStateOf(true) }

    val videoView = remember(videoUrl) {
        VideoView(context).apply {
            setVideoURI(Uri.parse(videoUrl))
            setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = true
                mediaPlayer.setVolume(0f, 0f) // Mute to keep it clean on start
                start()
                buffering = false
            }
            setOnErrorListener { _, _, _ ->
                buffering = false
                false
            }
        }
    }

    DisposableEffect(videoUrl) {
        onDispose {
            videoView.stopPlayback()
        }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable {
                isPlaying = !isPlaying
                if (isPlaying) {
                    videoView.start()
                } else {
                    videoView.pause()
                }
            }
    ) {
        AndroidView(
            factory = { videoView },
            modifier = Modifier.fillMaxSize()
        )

        if (buffering) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EarthBrownLight)
            }
        }
    }
}

@Composable
fun UserProfileOverlay(
    profile: UserProfile,
    activeVideoPost: VideoPost?,
    activePostIndex: Int,
    totalPostsSize: Int,
    currentUsername: String,
    currentName: String,
    allPosts: List<VideoPost>,
    onSelectVideo: (VideoPost) -> Unit,
    onPrevVideo: () -> Unit,
    onNextVideo: () -> Unit,
    onLikeClicked: () -> Unit,
    onCommentPosted: (String) -> Unit,
    onUploadRequested: () -> Unit,
    onBack: () -> Unit
) {
    var commentInputText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val likedPosts = remember(allPosts) { allPosts.filter { it.isLiked } }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("creator_profile_sheet"),
        color = WarmIvoryBg
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WarmIvorySurface)
                    .border(
                        width = 1.dp,
                        color = EarthBrownLight.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_to_feed_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kembali ke Beranda",
                            tint = DarkSoil
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kontrol & Profil",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSoil
                    )
                }

                // Add Upload/Posting feature (+) button directly on the Profile header
                Button(
                    onClick = onUploadRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = EarthBrownDark),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("profile_upload_video_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Unggah Video",
                        tint = WarmIvorySurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Video Anyar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvorySurface
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Identity Panel
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmIvorySurface, RoundedCornerShape(16.dp))
                            .border(1.dp, EarthBrownLight.copy(0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = profile.avatarUrl,
                            contentDescription = profile.name,
                            modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, EarthBrownDark, CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = profile.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = DarkSoil
                        )

                        Text(
                            text = "@${profile.username}",
                            color = EarthBrown,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EarthBrown,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Lembur ${profile.lembur}",
                                color = DarkSoil,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = profile.followersCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkSoil
                                )
                                Text(
                                    text = "Pangjeujeuh (Followers)",
                                    fontSize = 10.sp,
                                    color = EarthBrown
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = profile.followingCount.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DarkSoil
                                )
                                Text(
                                    text = "Dijeujeuhkeun (Following)",
                                    fontSize = 10.sp,
                                    color = EarthBrown
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = profile.bio,
                            fontSize = 12.sp,
                            color = DarkSoil,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // 2. High-Polished Organic Pill Tab Selector
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(WarmGray, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf(
                            Triple(0, "Kagiatan", Icons.Default.Forum),
                            Triple(1, "Nu Disuka", Icons.Default.Favorite),
                            Triple(2, "Setélan", Icons.Default.Settings)
                        )
                        tabs.forEach { (idx, title, icon) ->
                            val isSelected = selectedTab == idx
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EarthBrownDark else Color.Transparent)
                                    .clickable { selectedTab = idx }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) WarmIvorySurface else EarthBrownDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) WarmIvorySurface else EarthBrownDark
                                )
                            }
                        }
                    }
                }

                // 3. Conditional Tab Content Rendering
                if (selectedTab == 0) {
                    // TAB 0: ORIGINAL KAGIATAN DECK AND DISCUSSION
                    if (totalPostsSize > 1) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "KONTROL NAVIGASI FEED VIDEO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EarthBrownDark,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = onPrevVideo,
                                            enabled = activePostIndex > 0,
                                            modifier = Modifier.testTag("nav_prev_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowLeft,
                                                contentDescription = "Sebelumnya",
                                                tint = if (activePostIndex > 0) EarthBrownDark else EarthBrownLight,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Text(
                                            text = "${activePostIndex + 1} ti $totalPostsSize",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = DarkSoil
                                        )

                                        IconButton(
                                            onClick = onNextVideo,
                                            enabled = activePostIndex < totalPostsSize - 1,
                                            modifier = Modifier.testTag("nav_next_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Terasna",
                                                tint = if (activePostIndex < totalPostsSize - 1) EarthBrownDark else EarthBrownLight,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    activeVideoPost?.let { post ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "DILALON-NYAWALA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EarthBrownDark,
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = post.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSoil
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Like counter action panel
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onLikeClicked() }
                                                .background(EarthBrownLight.copy(0.3f))
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = if (post.isLiked) Color.Red else EarthBrown,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${post.likesCount} Dirorojong",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = DarkSoil
                                            )
                                        }

                                        // Display comments tally
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Forum,
                                                contentDescription = null,
                                                tint = EarthBrown,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${post.comments.size} Sawala",
                                                color = DarkSoil,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Comments Section List Title
                        item {
                            Text(
                                text = "Babakan Sawala (Diskusi)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSoil,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        if (post.comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Sok, janten jalmi kahiji anu nyawala di video ieu!",
                                        color = EarthBrown,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(post.comments) { comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(WarmIvorySurface, RoundedCornerShape(12.dp))
                                        .border(1.dp, WarmGray, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(EarthBrownLight.copy(alpha = 0.3f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.name.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = DarkSoil
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = comment.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = DarkSoil
                                            )
                                            Text(
                                                text = comment.timestamp,
                                                fontSize = 9.sp,
                                                color = EarthBrown
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = comment.text,
                                            fontSize = 11.sp,
                                            color = DarkSoil
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // TAB 1: LIKED VIDEOS GRID/LIST
                    item {
                        Text(
                            text = "Pidéo Nu Disuka (Favorit kuring)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSoil,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (likedPosts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.3f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = EarthBrownLight,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Teu aya pidéo nu disuka",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DarkSoil
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ketok ikon cinta beureum ❤️ dina pidéo feed kanggo nambihan karesep anjeun didieu.",
                                        fontSize = 11.sp,
                                        color = EarthBrown,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(likedPosts) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectVideo(post) }
                                    .testTag("liked_video_card_${post.id}"),
                                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EarthBrownDark),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(WarmIvorySurface.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Puter",
                                                tint = WarmIvorySurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = post.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = DarkSoil,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Ku: ${post.creatorName}",
                                            fontSize = 10.sp,
                                            color = EarthBrown
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = EarthBrown,
                                                modifier = Modifier.size(10.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Lembur ${post.lembur}",
                                                color = EarthBrown,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color.Red,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = post.likesCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSoil
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 2) {
                    // TAB 2: SIMPLE SETTINGS INTERACTION
                    item {
                        var muteDefault by remember { mutableStateOf(false) }
                        var alertDefault by remember { mutableStateOf(true) }
                        var selectBasa by remember { mutableStateOf("Sunda") }
                        var selectBasaExpanded by remember { mutableStateOf(false) }
                        var lightweightMode by remember { mutableStateOf(false) }
                        var isCacheCleared by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "PENGATURAN UTAMA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EarthBrownDark,
                                    letterSpacing = 0.5.sp
                                )

                                // Alert switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Wara-Wara Lembur (Notifikasi)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSoil)
                                        Text("Tembongkeun bewara unggahan anyar", fontSize = 10.sp, color = EarthBrown)
                                    }
                                    Switch(
                                        checked = alertDefault,
                                        onCheckedChange = { alertDefault = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = WarmIvorySurface,
                                            checkedTrackColor = EarthBrownDark,
                                            uncheckedThumbColor = EarthBrown,
                                            uncheckedTrackColor = WarmGray
                                        ),
                                        modifier = Modifier.testTag("settings_notif_switch")
                                    )
                                }

                                // Mute switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Heningkeun Standar (Mute Video)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSoil)
                                        Text("Setel sora video diheningkeun sacara otomatis", fontSize = 10.sp, color = EarthBrown)
                                    }
                                    Switch(
                                        checked = muteDefault,
                                        onCheckedChange = { muteDefault = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = WarmIvorySurface,
                                            checkedTrackColor = EarthBrownDark,
                                            uncheckedThumbColor = EarthBrown,
                                            uncheckedTrackColor = WarmGray
                                        ),
                                        modifier = Modifier.testTag("settings_mute_switch")
                                    )
                                }

                                // Lightweight Mode switch
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Kualitas Hemat Kuota", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSoil)
                                        Text("Optimalkeun pancaran data ringan", fontSize = 10.sp, color = EarthBrown)
                                    }
                                    Switch(
                                        checked = lightweightMode,
                                        onCheckedChange = { lightweightMode = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = WarmIvorySurface,
                                            checkedTrackColor = EarthBrownDark,
                                            uncheckedThumbColor = EarthBrown,
                                            uncheckedTrackColor = WarmGray
                                        ),
                                        modifier = Modifier.testTag("settings_light_switch")
                                    )
                                }

                                // Selection Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Pilihan Basa", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSoil)
                                        Text("Pilih basa utama aplikasi Saamparan", fontSize = 10.sp, color = EarthBrown)
                                    }

                                    Box {
                                        Surface(
                                            color = WarmGray,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable { selectBasaExpanded = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = selectBasa, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSoil)
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = DarkSoil, modifier = Modifier.size(12.dp))
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = selectBasaExpanded,
                                            onDismissRequest = { selectBasaExpanded = false },
                                            modifier = Modifier.background(WarmIvorySurface)
                                        ) {
                                            listOf("Sunda", "Indonesia", "English").forEach { lang ->
                                                DropdownMenuItem(
                                                    text = { Text(lang, color = DarkSoil, fontSize = 12.sp) },
                                                    onClick = {
                                                        selectBasa = lang
                                                        selectBasaExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                // Clear cache button
                                Column {
                                    Button(
                                        onClick = { isCacheCleared = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = EarthBrownDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("clear_cache_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = WarmIvorySurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isCacheCleared) "Cache Parantos Beresih!" else "Hapus Data Cache (Pancacahan)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WarmIvorySurface
                                        )
                                    }
                                    if (isCacheCleared) {
                                        Text(
                                            text = "Data simpanan lokal parantos dibersihan sacara total.",
                                            fontSize = 9.sp,
                                            color = LeafGreenDark,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }

                                // About App Card
                                Surface(
                                    color = WarmGray,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = EarthBrown,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Saamparan v1.2.0", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DarkSoil)
                                            Text(
                                                "Aplikasi gotong royong sarta panyebaran video hyperlocal kanggo urang lembur makin majeng sarta bersahaja.",
                                                fontSize = 9.sp,
                                                color = DarkSoil.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick comment reply submission strip housed here inside the overlay page (ONLY when Kagiatan tab is open)
            AnimatedVisibility(
                visible = selectedTab == 0 && activeVideoPost != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                Surface(
                    color = WarmIvorySurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = EarthBrownLight.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_box"),
                            placeholder = { Text("Serat komentar sawala...", color = EarthBrown, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EarthBrownDark,
                                unfocusedBorderColor = EarthBrownLight,
                                focusedTextColor = DarkSoil,
                                unfocusedTextColor = DarkSoil
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (commentInputText.isNotBlank()) {
                                    onCommentPosted(commentInputText)
                                    commentInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(EarthBrownDark, RoundedCornerShape(10.dp))
                                .testTag("comment_submit_btn"),
                            colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(contentColor = WarmIvorySurface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Kirim Komentar",
                                tint = WarmIvorySurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationBanner(
    title: String,
    lembur: String,
    creator: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = EarthBrownDark,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_banner")
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WarmIvorySurface.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = WarmIvorySurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unggahan Anyar di Lembur!",
                    color = WarmIvorySurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
                Text(
                    text = "$creator ngabagi video di $lembur",
                    color = WarmIvorySurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = title,
                    color = WarmIvorySurface.copy(alpha = 0.75f),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = WarmIvorySurface),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("view_notification_btn")
            ) {
                Text(
                    text = "Tonton",
                    color = EarthBrownDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = WarmIvorySurface.copy(0.7f),
                    modifier = Modifier.size(16.dp)
                )
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
    var descText by remember { mutableStateOf("") }
    var selectedVillage by remember { mutableStateOf(if (lemburs.isNotEmpty()) lemburs[0] else "Situraja") }
    var expandedVillage by remember { mutableStateOf(false) }

    val presets = listOf(
        Triple("Curug Pamutuh (Air Terjun)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", "⛰️"),
        Triple("Pasawahan Hejo (Sawah Hijau)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", "🌾"),
        Triple("Kagiatan Nyieun Liwet (Sunda)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4", "🪵"),
        Triple("Ulin Sapeda di Lembur", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4", "🚲")
    )
    var selectedPresetUrl by remember { mutableStateOf<String?>(presets[0].second) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WarmIvorySurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Bagikeun Video Anyar",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = DarkSoil
                )
                Text(
                    text = "Pedarkeun carita, kagiatan, jeung sumanget anyar lembur anjeun!",
                    fontSize = 11.sp,
                    color = EarthBrown,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Text(
                    text = "Pilih Lembur (Wilayah)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSoil,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarmIvoryBg, RoundedCornerShape(8.dp))
                        .border(1.dp, EarthBrownLight, RoundedCornerShape(8.dp))
                        .clickable { expandedVillage = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedVillage, color = DarkSoil, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = EarthBrownDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedVillage,
                        onDismissRequest = { expandedVillage = false },
                        modifier = Modifier.background(WarmIvorySurface)
                    ) {
                        lemburs.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(text = v, color = DarkSoil) },
                                onClick = {
                                    selectedVillage = v
                                    expandedVillage = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sumber Pidéo (File Video)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSoil,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPickVideo() }
                        .testTag("upload_gallery_picker"),
                    colors = CardDefaults.cardColors(containerColor = WarmIvoryBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EarthBrownLight.copy(0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = null,
                            tint = EarthBrownDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pilih tina Galéri HP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSoil
                            )
                            Text(
                                text = if (selectedVideoName != null) {
                                    "Terpilih: $selectedVideoName"
                                } else {
                                    "Pilih berkas video lokal ti memori gallery"
                                },
                                fontSize = 10.sp,
                                color = if (selectedVideoName != null) LeafGreenDark else EarthBrown
                            )
                        }
                        if (selectedVideoName != null) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Batal Pilih",
                                tint = Color.Red,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable {
                                        onClearVideo()
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Atanapi pilih sawatara Pidéo Contoh:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = EarthBrown
                )
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { (name, url, emoji) ->
                        val isPresetSelected = (selectedPresetUrl == url && selectedVideoUri == null)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isPresetSelected) EarthBrownDark else WarmIvoryBg)
                                .border(
                                    1.dp,
                                    if (isPresetSelected) EarthBrownDark else EarthBrownLight.copy(0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedPresetUrl = url
                                    onClearVideo()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = name.split(" ")[0],
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPresetSelected) WarmIvorySurface else DarkSoil,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Judul Singget (Maksimal 3-4 Kecap)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSoil,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    placeholder = { Text("Contoh: Bikin kopi lembur", color = EarthBrown, fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("upload_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EarthBrownDark,
                        unfocusedBorderColor = EarthBrownLight,
                        focusedTextColor = DarkSoil,
                        unfocusedTextColor = DarkSoil
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Batal", color = EarthBrownDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (descText.isNotBlank()) {
                                val words = descText.split("\\s+".toRegex())
                                val briefText = if (words.size > 4) {
                                    words.take(4).joinToString(" ")
                                } else {
                                    descText
                                }
                                val finalVideoUrl = selectedVideoUri?.toString() ?: selectedPresetUrl
                                onPost(briefText, selectedVillage, finalVideoUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EarthBrownDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("upload_submit_dialog_btn")
                    ) {
                        Text(text = "Kintun (Kirim)", fontWeight = FontWeight.Bold, color = WarmIvorySurface, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
