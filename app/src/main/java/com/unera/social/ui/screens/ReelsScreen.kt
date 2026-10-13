package com.unera.social.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import com.unera.social.ui.components.VerifiedBadge
import com.unera.social.ui.components.InstagramVideoCard
import com.unera.social.ui.components.SavePostButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unera.social.model.Post
import com.unera.social.model.Reel
import com.unera.social.model.User
import com.unera.social.ui.theme.*

// Web Palette Constants
private val WebDarkBg = Color(0xFF0B1120)
private val WebCardBg = Color(0xFF0F172A)
private val WebBorder = Color(0xFF1E293B)
private val WebHover = Color(0xFF334155)
private val WebBlue = Color(0xFF1877F2)
private val WebSkyBlue = Color(0xFF38BDF8)
private val WebAmber = Color(0xFFF59E0B)
private val WebGreen = Color(0xFF22C55E)
private val WebTextPrimary = Color(0xFFF8FAFC)
private val WebTextSecondary = Color(0xFF94A3B8)
private val WebTextMuted = Color(0xFF64748B)

data class VideoItem(
    val id: String,
    val author: User,
    val videoUrl: String,
    val thumbnailUrl: String,
    val caption: String,
    val musicTitle: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean = false,
    val isSponsored: Boolean = false,
    val timestamp: String = "2h ago",
    val originalPost: Post? = null,
    val originalReel: Reel? = null
)

@Composable
fun ReelsScreen(
    reels: List<Reel>,
    onLikeReel: (Long) -> Unit,
    posts: List<Post> = emptyList(),
    currentUser: User? = null,
    onLikePost: ((Long) -> Unit)? = null,
    onCommentPost: ((Post) -> Unit)? = null,
    onSavePost: ((Long) -> Unit)? = null,
    onUserClick: ((User) -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isImmersiveMode by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    val itemsPerPage = 8

    // Combine and unify videos from reels and posts
    val allVideoItems = remember(reels, posts) {
        val list = mutableListOf<VideoItem>()

        // 1. Video posts
        posts.filter { it.mediaUrl != null }.forEach { post ->
            list.add(
                VideoItem(
                    id = "post_${post.id}",
                    author = post.author,
                    videoUrl = post.mediaUrl ?: "",
                    thumbnailUrl = post.mediaUrl ?: "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=800",
                    caption = post.content,
                    musicTitle = "Original Audio",
                    likesCount = post.likesCount,
                    commentsCount = post.commentsCount,
                    sharesCount = post.sharesCount,
                    isLiked = post.isLiked,
                    isSaved = post.isSaved,
                    isSponsored = false,
                    timestamp = post.timestamp,
                    originalPost = post
                )
            )
        }

        // 2. Reels
        reels.forEach { reel ->
            list.add(
                VideoItem(
                    id = "reel_${reel.id}",
                    author = reel.author,
                    videoUrl = reel.videoUrl,
                    thumbnailUrl = reel.thumbnailUrl,
                    caption = reel.caption,
                    musicTitle = reel.musicTitle,
                    likesCount = reel.likesCount,
                    commentsCount = reel.commentsCount,
                    sharesCount = reel.sharesCount,
                    isLiked = reel.isLiked,
                    isSaved = false,
                    isSponsored = false,
                    timestamp = "1d ago",
                    originalReel = reel
                )
            )
        }

        list
    }

    val totalPages = remember(allVideoItems.size) {
        maxOf(1, (allVideoItems.size + itemsPerPage - 1) / itemsPerPage)
    }

    val pagedVideos = remember(allVideoItems, currentPage) {
        val start = (currentPage - 1) * itemsPerPage
        val end = minOf(start + itemsPerPage, allVideoItems.size)
        if (start < allVideoItems.size) allVideoItems.subList(start, end) else emptyList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WebDarkBg)
            .testTag("videos_page_container")
    ) {
        // Sticky Header matching web
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WebDarkBg)
                .border(width = 1.dp, color = WebBorder)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WebCardBg)
                            .border(1.dp, WebBorder, RoundedCornerShape(8.dp))
                            .testTag("videos_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = WebTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = WebBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Videos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WebTextPrimary
                    )
                }
            }

            // View Mode Toggle (Feed vs Immersive)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(WebCardBg)
                    .border(1.dp, WebBorder, RoundedCornerShape(8.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isImmersiveMode) WebBlue else Color.Transparent)
                        .clickable { isImmersiveMode = false }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Feed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isImmersiveMode) Color.White else WebTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isImmersiveMode) WebBlue else Color.Transparent)
                        .clickable { isImmersiveMode = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Reels",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isImmersiveMode) Color.White else WebTextSecondary
                    )
                }
            }
        }

        if (isImmersiveMode) {
            // Immersive Full View
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("immersive_reels_list")
            ) {
                items(reels, key = { it.id }) { reel ->
                    ReelCard(
                        reel = reel,
                        onLike = { onLikeReel(reel.id) }
                    )
                }
            }
        } else {
            // InstagramVideoCard Feed View (Web Parity)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("videos_feed_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (pagedVideos.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp, horizontal = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(WebCardBg)
                                    .border(1.dp, WebBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Movie,
                                    contentDescription = null,
                                    tint = WebSkyBlue,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Videos Available",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WebTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Videos from the feed and reels will appear here.",
                                fontSize = 13.sp,
                                color = WebTextSecondary
                            )
                        }
                    }
                } else {
                    items(pagedVideos, key = { it.id }) { item ->
                        InstagramVideoCard(
                            videoItem = item,
                            currentUser = currentUser,
                            onLike = {
                                if (item.originalReel != null) {
                                    onLikeReel(item.originalReel.id)
                                } else if (item.originalPost != null && onLikePost != null) {
                                    onLikePost(item.originalPost.id)
                                }
                            },
                            onComment = {
                                if (item.originalPost != null && onCommentPost != null) {
                                    onCommentPost(item.originalPost)
                                }
                            },
                            onSave = {
                                if (item.originalPost != null && onSavePost != null) {
                                    onSavePost(item.originalPost.id)
                                }
                            },
                            onUserClick = {
                                onUserClick?.invoke(item.author)
                            }
                        )
                        Spacer(modifier = Modifier.height(1.dp).background(WebBorder))
                    }

                    // Pagination Control matching web
                    if (totalPages > 1) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                color = WebDarkBg,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WebBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val startIdx = (currentPage - 1) * itemsPerPage + 1
                                    val endIdx = minOf(currentPage * itemsPerPage, allVideoItems.size)
                                    Text(
                                        text = "Showing $startIdx–$endIdx of ${allVideoItems.size} videos",
                                        fontSize = 12.sp,
                                        color = WebTextSecondary
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { if (currentPage > 1) currentPage-- },
                                            enabled = currentPage > 1,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WebCardBg,
                                                contentColor = WebTextPrimary,
                                                disabledContainerColor = WebCardBg.copy(alpha = 0.5f),
                                                disabledContentColor = WebTextMuted
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Prev", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Text(
                                            text = "$currentPage / $totalPages",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebTextPrimary
                                        )

                                        Button(
                                            onClick = { if (currentPage < totalPages) currentPage++ },
                                            enabled = currentPage < totalPages,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = WebCardBg,
                                                contentColor = WebTextPrimary,
                                                disabledContainerColor = WebCardBg.copy(alpha = 0.5f),
                                                disabledContentColor = WebTextMuted
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 1:1 Implementation of web's InstagramVideoCard component.
 * Reproduces headers, story-ring avatars, verification, audio subtitles, video player container,
 * HD badge, audio pills, mute toggles, progress bar, reactions summary, and action bar.
 */
@Composable
fun InstagramVideoCard(
    videoItem: VideoItem,
    currentUser: User?,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onSave: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isExpandedCaption by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }
    var showDoubleTapHeart by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WebCardBg)
            .border(width = 0.5.dp, color = WebBorder)
            .testTag("instagram_video_card_${videoItem.id}")
    ) {
        // 1. Author & Info Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                // Story gradient avatar with online badge
                Box(
                    modifier = Modifier
                        .clickable(onClick = onUserClick)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(WebBlue, Color(0xFF0284C7), WebSkyBlue, WebBlue)
                                )
                            )
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = videoItem.author.profileImageUrl,
                            contentDescription = videoItem.author.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(WebCardBg)
                        )
                    }

                    // Green online indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(WebGreen)
                            .border(1.5.dp, WebCardBg, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = videoItem.author.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = WebTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable(onClick = onUserClick)
                        )
                        if (videoItem.author.isVerified) {
                            VerifiedBadge(size = 15.dp)
                        }
                        Text(text = "•", color = WebTextMuted, fontSize = 12.sp)
                        Text(text = videoItem.timestamp, color = WebTextSecondary, fontSize = 12.sp)
                    }

                    // Audio track pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = WebSkyBlue,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${videoItem.musicTitle} • ${videoItem.author.name}",
                            color = WebTextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Top Right Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Follow Button
                Button(
                    onClick = { isFollowing = !isFollowing },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) WebBorder else WebBlue,
                        contentColor = if (isFollowing) WebTextSecondary else Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Videos Pill Tag
                Surface(
                    color = WebBorder,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = WebSkyBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Videos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WebSkyBlue
                        )
                    }
                }
            }
        }

        // 2. Video Media Frame
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(Color.Black)
                .clickable {
                    isPlaying = !isPlaying
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = videoItem.thumbnailUrl,
                contentDescription = videoItem.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // HD Badge in top-left
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebSkyBlue
                )
            }

            // Top-Right Quick Expand Icon
            IconButton(
                onClick = { /* Expand video */ },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInFull,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Animated / Static Play/Pause Overlay
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = WebSkyBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Double tap heart indicator
            if (showDoubleTapHeart) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = LikeRed,
                    modifier = Modifier.size(72.dp)
                )
            }

            // Bottom-Left Audio Pill
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                        contentDescription = null,
                        tint = WebSkyBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${videoItem.musicTitle} - ${videoItem.author.name}",
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom-Right Mute Button
            IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute" else "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Bottom Blue Progress Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (isPlaying) 0.55f else 0.25f)
                        .background(WebBlue)
                )
            }
        }

        // 3. Reactions & Counts Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Emoji stack (👍 ❤️)
                Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                    Text("👍", fontSize = 14.sp)
                    Text("❤️", fontSize = 14.sp)
                }
                Text(
                    text = "${videoItem.likesCount} Reactions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebTextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "${videoItem.commentsCount} Discussions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = WebTextSecondary,
                    modifier = Modifier.clickable(onClick = onComment)
                )
                Text(
                    text = "${videoItem.sharesCount} Shares",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = WebTextSecondary
                )
            }
        }

        Divider(color = WebBorder, thickness = 0.5.dp)

        // 4. Action Buttons (Like, Discuss, Share, Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Like Button
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (videoItem.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (videoItem.isLiked) LikeRed else WebTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Comment Button
                IconButton(onClick = onComment) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Discuss",
                        tint = WebTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share Button
                IconButton(onClick = { /* Share */ }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Share",
                        tint = WebTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Save / Bookmark Button
            SavePostButton(
                isSaved = videoItem.isSaved,
                onToggle = onSave,
                isVideo = true,
                size = 22.dp
            )
        }

        // 5. Caption & Comment Input Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (videoItem.caption.isNotBlank()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = buildString {
                            append(videoItem.author.name)
                            append(" ")
                            append(if (isExpandedCaption || videoItem.caption.length <= 80) videoItem.caption else "${videoItem.caption.take(80)}...")
                        },
                        fontSize = 13.5.sp,
                        color = WebTextPrimary,
                        lineHeight = 18.sp
                    )
                }
                if (videoItem.caption.length > 80) {
                    Text(
                        text = if (isExpandedCaption) "less" else "more",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WebTextSecondary,
                        modifier = Modifier.clickable { isExpandedCaption = !isExpandedCaption }
                    )
                }
            }

            if (videoItem.commentsCount > 0) {
                Text(
                    text = "View all ${videoItem.commentsCount} discussions",
                    fontSize = 12.5.sp,
                    color = WebTextSecondary,
                    modifier = Modifier.clickable(onClick = onComment)
                )
            }

            // Inline quick discussion composer row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(WebDarkBg)
                    .border(1.dp, WebBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onComment)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = currentUser?.profileImageUrl ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                )
                Text(
                    text = "Add a discussion…",
                    fontSize = 12.5.sp,
                    color = WebTextMuted,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = WebTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Full-bleed Reel Card for the immersive view.
 */
@Composable
fun ReelCard(
    reel: Reel,
    onLike: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(580.dp)
            .testTag("reel_card_${reel.id}")
    ) {
        // Thumbnail / Visual
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = reel.caption,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66000000),
                            WebDarkBg.copy(alpha = 0.95f)
                        ),
                        startY = 200f
                    )
                )
        )

        // Right side Action Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WebCardBg.copy(alpha = 0.85f))
                        .testTag("like_reel_btn_${reel.id}")
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) LikeRed else WebTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.likesCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebTextPrimary
                )
            }

            // Comment
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { /* Comment */ },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WebCardBg.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Comments",
                        tint = WebTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.commentsCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebTextPrimary
                )
            }

            // Share
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(WebCardBg.copy(alpha = 0.85f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = WebTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = reel.sharesCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = WebTextPrimary
                )
            }
        }

        // Bottom Left Info: Author, Caption, Audio Pill
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 16.dp, bottom = 28.dp)
        ) {
            // Author
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = reel.author.profileImageUrl,
                    contentDescription = reel.author.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, WebBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = reel.author.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = WebTextPrimary
                )
                if (reel.author.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(size = 14.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caption
            Text(
                text = reel.caption,
                fontSize = 13.sp,
                color = WebTextPrimary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Music track pill
            Surface(
                color = WebCardBg.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = WebSkyBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = reel.musicTitle,
                        fontSize = 11.sp,
                        color = WebTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
