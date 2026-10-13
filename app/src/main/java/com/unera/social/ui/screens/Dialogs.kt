package com.unera.social.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.unera.social.model.*
import com.unera.social.ui.components.CommentActionModal
import com.unera.social.ui.components.VerifiedBadge
import com.unera.social.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CreatePostDialog(
    currentUser: User,
    onDismiss: () -> Unit,
    onSubmit: (content: String, mediaUrl: String?, feeling: String?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("") }
    var selectedFeeling by remember { mutableStateOf<String?>(null) }
    var showImageUrlField by remember { mutableStateOf(false) }

    val feelings = listOf("happy 😊", "relaxed 😌", "excited 🚀", "grateful 🙏", "blessed ✨")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_post_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Navy800)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Author Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = currentUser.profileImageUrl,
                        contentDescription = currentUser.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentUser.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        if (selectedFeeling != null) {
                            Text(
                                text = "feeling $selectedFeeling",
                                fontSize = 11.sp,
                                color = Carrot500
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content TextField
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text("What's on your mind?", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("create_post_text_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedBorderColor = Carrot500,
                        unfocusedBorderColor = Navy700
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showImageUrlField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = mediaUrl,
                        onValueChange = { mediaUrl = it },
                        placeholder = { Text("Image URL…", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_post_image_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900,
                            focusedBorderColor = Carrot500,
                            unfocusedBorderColor = Navy700
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick buttons: Attach Image & Feeling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showImageUrlField = !showImageUrlField },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (showImageUrlField) Navy700 else Navy900
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Carrot500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Photo", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Feeling selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(feelings) { feel ->
                        FilterChip(
                            selected = selectedFeeling == feel,
                            onClick = {
                                selectedFeeling = if (selectedFeeling == feel) null else feel
                            },
                            label = { Text(feel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Carrot500,
                                containerColor = Navy900
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        onSubmit(
                            content,
                            if (mediaUrl.isNotBlank()) mediaUrl else null,
                            selectedFeeling
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_post_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Carrot500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Post",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CommentsDialog(
    post: Post,
    currentUser: User? = null,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit,
    onDeleteComment: ((Long) -> Unit)? = null
) {
    var commentText by remember { mutableStateOf("") }
    var selectedCommentForActions by remember { mutableStateOf<Comment?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .testTag("comments_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Navy800)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments (${post.comments.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = Navy700)

                // Comments List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (post.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Be the first to comment!",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(post.comments, key = { it.id }) { comment ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedCommentForActions = comment }
                            ) {
                                AsyncImage(
                                    model = comment.author.profileImageUrl,
                                    contentDescription = comment.author.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Navy700)
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = comment.author.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Icon(
                                            imageVector = Icons.Default.MoreHoriz,
                                            contentDescription = "Options",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.text,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = comment.timestamp,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Navy700)

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment…", color = TextSecondary, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_comment_input"),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900,
                            focusedBorderColor = Carrot500,
                            unfocusedBorderColor = Navy700
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Carrot500)
                            .testTag("submit_comment_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Comment",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Comment Action Modal for comments
    selectedCommentForActions?.let { c ->
        CommentActionModal(
            isOpen = true,
            onClose = { selectedCommentForActions = null },
            comment = c,
            currentUserId = currentUser?.id ?: 0L,
            isAdmin = currentUser?.role in listOf("admin", "moderator"),
            onDelete = { commentId ->
                onDeleteComment?.invoke(commentId)
            },
            onReply = { comment ->
                commentText = "@${comment.author.name} "
            }
        )
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onContactSeller: (User) -> Unit
) {
    val allImages = if (product.images.isNotEmpty()) product.images else listOf(product.imageUrl)
    var activeImageIndex by remember { mutableStateOf(0) }
    val displayPrice = product.discountPrice ?: product.price
    val formattedPrice = formatMarketPrice(displayPrice, product.country)

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050B18))
                .testTag("product_detail_dialog")
        ) {
            // Header matching web: `h-14 px-4 flex items-center justify-between border-b border-[#1E293B] bg-[#0B1120]`
            Surface(
                color = Color(0xFF0B1120),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF8FAFC)
                        )
                    }

                    Text(
                        text = "MarketPoint",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )

                    IconButton(
                        onClick = { /* More options */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Square Image Carousel
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(Color(0xFF0B1120))
                    ) {
                        val currentImageUrl = allImages.getOrElse(activeImageIndex) { product.imageUrl }
                        AsyncImage(
                            model = currentImageUrl,
                            contentDescription = product.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // "Ad" badge if sponsored
                        if (product.isSponsored) {
                            Surface(
                                color = Color(0xFFF97316),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Ad",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        // Prev / Next Arrows if multiple images
                        if (allImages.size > 1) {
                            IconButton(
                                onClick = {
                                    activeImageIndex = if (activeImageIndex > 0) activeImageIndex - 1 else allImages.size - 1
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(8.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = {
                                    activeImageIndex = if (activeImageIndex < allImages.size - 1) activeImageIndex + 1 else 0
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(8.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Thumbnail strip if multiple images
                if (allImages.size > 1) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0B1120))
                        ) {
                            items(allImages.indices.toList()) { idx ->
                                val isSelected = activeImageIndex == idx
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            2.dp,
                                            if (isSelected) Color(0xFF1877F2) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeImageIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = allImages[idx],
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                // Title and Pricing Section matching web
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B1120))
                            .padding(16.dp)
                    ) {
                        if (product.isSponsored) {
                            Surface(
                                color = Color(0xFFF97316).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.4f)),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Ad • Sponsored",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFB923C),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = product.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color(0xFFF8FAFC),
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = formattedPrice,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = Color(0xFFF8FAFC)
                        )

                        if (product.discountPrice != null) {
                            Text(
                                text = formatMarketPrice(product.price, product.country),
                                fontSize = 16.sp,
                                color = Color(0xFF94A3B8),
                                style = androidx.compose.ui.text.TextStyle(
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = product.location,
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Seller Card Section matching web
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF0B1120),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = product.seller.profileImageUrl,
                                contentDescription = product.seller.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = product.seller.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color(0xFFF8FAFC)
                                    )
                                    if (product.seller.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadge(size = 16.dp)
                                    }
                                }
                                Text(
                                    text = "Seller",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Message Button
                            IconButton(
                                onClick = { onContactSeller(product.seller) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1877F2).copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Message",
                                    tint = Color(0xFF1877F2),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Phone Call Button
                            if (product.phoneNumber.isNotBlank()) {
                                IconButton(
                                    onClick = { /* Call action */ },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = Color(0xFFF8FAFC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Description Section matching web
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B1120))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Description",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.description.ifBlank { "No description provided for this item." },
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom Sticky "Contact Seller" Button matching web
            Surface(
                color = Color(0xFF0B1120),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { onContactSeller(product.seller) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("contact_seller_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Message Seller",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CreateProductDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        price: Double,
        discountPrice: Double?,
        category: String,
        location: String,
        country: String,
        description: String,
        imageUrl: String,
        images: List<String>,
        phone: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var discountPriceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("electronics") }
    var location by remember { mutableStateOf("Dar es Salaam, Tanzania") }
    var countryCode by remember { mutableStateOf("TZ") }
    var description by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+255 712 345 678") }
    var imageUrlInput by remember { mutableStateOf("") }
    var imagesList by remember { mutableStateOf(listOf<String>()) }
    var isPublishing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050B18))
                .testTag("create_product_dialog")
        ) {
            // Header matching web: `h-14 px-3 flex items-center justify-between border-b border-[#1E293B] bg-[#0B1120]`
            Surface(
                color = Color(0xFF0B1120),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFF8FAFC)
                        )
                    }

                    Text(
                        text = "New listing",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )

                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && priceText.isNotBlank()) {
                                isPublishing = true
                                val priceVal = priceText.toDoubleOrNull() ?: 0.0
                                val discountVal = discountPriceText.toDoubleOrNull()
                                val finalImages = if (imagesList.isNotEmpty()) imagesList else listOf(imageUrlInput.ifBlank { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800" })
                                onSubmit(
                                    title,
                                    priceVal,
                                    discountVal,
                                    category,
                                    location,
                                    countryCode,
                                    description,
                                    finalImages.first(),
                                    finalImages,
                                    phone
                                )
                            }
                        },
                        enabled = !isPublishing
                    ) {
                        Text(
                            text = if (isPublishing) "Publishing…" else "Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1877F2)
                        )
                    }
                }
            }

            // Scrollable Form matching web
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Photos Section matching web
                item {
                    Column {
                        Text(
                            text = "Photos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFF8FAFC)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Add photo card
                        Surface(
                            onClick = {
                                // Add quick photo or prompt
                                val samplePhotos = listOf(
                                    "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800",
                                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800",
                                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"
                                )
                                if (imagesList.size < 10) {
                                    val nextPhoto = samplePhotos[imagesList.size % samplePhotos.size]
                                    imagesList = imagesList + nextPhoto
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0F172A),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add photos",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF8FAFC)
                                )
                                Text(
                                    text = "Up to 10 images",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Uploaded previews grid
                        if (imagesList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(imagesList.indices.toList()) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = imagesList[idx],
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        IconButton(
                                            onClick = {
                                                imagesList = imagesList.filterIndexed { i, _ -> i != idx }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Category selector matching web
                item {
                    Column {
                        Text(
                            text = "Category",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WEB_MARKETPLACE_CATEGORIES.filter { it.id != "all" }) { cat ->
                                val isSelected = category.equals(cat.id, ignoreCase = true)
                                Surface(
                                    onClick = { category = cat.id },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF1877F2) else Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    ) {
                                        Text(
                                            text = cat.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFFF8FAFC)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Title input: "What are you selling?"
                item {
                    Column {
                        Text(
                            text = "What are you selling?",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Title", color = Color(0xFF64748B)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_title_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF1877F2),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color(0xFFF8FAFC),
                                unfocusedTextColor = Color(0xFFF8FAFC)
                            ),
                            singleLine = true
                        )
                    }
                }

                // Price input with currency prefix matching web
                item {
                    Column {
                        Text(
                            text = "Price",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getCurrencyLabel(countryCode),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 14.sp
                                )
                            }
                            VerticalDivider(color = Color(0xFF1E293B))
                            TextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                placeholder = { Text("Price", color = Color(0xFF64748B)) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("product_price_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color(0xFFF8FAFC),
                                    unfocusedTextColor = Color(0xFFF8FAFC)
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                // Discount price input matching web
                item {
                    Column {
                        Text(
                            text = "Discount price (optional)",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getCurrencyLabel(countryCode),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC),
                                    fontSize = 14.sp
                                )
                            }
                            VerticalDivider(color = Color(0xFF1E293B))
                            TextField(
                                value = discountPriceText,
                                onValueChange = { discountPriceText = it },
                                placeholder = { Text("Discount price", color = Color(0xFF64748B)) },
                                modifier = Modifier.fillMaxSize(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color(0xFFF8FAFC),
                                    unfocusedTextColor = Color(0xFFF8FAFC)
                                ),
                                singleLine = true
                            )
                        }
                    }
                }

                // Location input matching web
                item {
                    Column {
                        Text(
                            text = "Location",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = { Text("City, street or region…", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF1877F2),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color(0xFFF8FAFC),
                                unfocusedTextColor = Color(0xFFF8FAFC)
                            ),
                            singleLine = true
                        )
                    }
                }

                // Country selector matching web
                item {
                    Column {
                        Text(
                            text = "Country",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WEB_MARKETPLACE_COUNTRIES.filter { it.code != "all" }) { c ->
                                val isSelected = countryCode.equals(c.code, ignoreCase = true)
                                Surface(
                                    onClick = { countryCode = c.code },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF1877F2) else Color(0xFF0F172A),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                                    modifier = Modifier.height(42.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    ) {
                                        Text(text = c.flag, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = c.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFFF8FAFC)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Description input matching web
                item {
                    Column {
                        Text(
                            text = "Description",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Describe what you are selling…", color = Color(0xFF64748B)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF1877F2),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color(0xFFF8FAFC),
                                unfocusedTextColor = Color(0xFFF8FAFC)
                            )
                        )
                    }
                }

                // Phone input matching web
                item {
                    Column {
                        Text(
                            text = "Phone Number",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("Phone number", color = Color(0xFF64748B)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F172A),
                                unfocusedContainerColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF1877F2),
                                unfocusedBorderColor = Color(0xFF1E293B),
                                focusedTextColor = Color(0xFFF8FAFC),
                                unfocusedTextColor = Color(0xFFF8FAFC)
                            ),
                            singleLine = true
                        )
                    }
                }

                // Submit button matching web
                item {
                    Button(
                        onClick = {
                            if (title.isNotBlank() && priceText.isNotBlank()) {
                                isPublishing = true
                                val priceVal = priceText.toDoubleOrNull() ?: 0.0
                                val discountVal = discountPriceText.toDoubleOrNull()
                                val finalImages = if (imagesList.isNotEmpty()) imagesList else listOf(imageUrlInput.ifBlank { "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800" })
                                onSubmit(
                                    title,
                                    priceVal,
                                    discountVal,
                                    category,
                                    location,
                                    countryCode,
                                    description,
                                    finalImages.first(),
                                    finalImages,
                                    phone
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_product_btn")
                    ) {
                        Text(
                            text = if (isPublishing) "Publishing…" else "Publish",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onSubmit: (title: String, date: String, location: String, description: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_event_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Navy800)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Create an Event",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Event Name", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedBorderColor = Carrot500,
                        unfocusedBorderColor = Navy700
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    placeholder = { Text("Date & Time (e.g. Nov 20 • 6 PM)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedBorderColor = Carrot500,
                        unfocusedBorderColor = Navy700
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Location / Venue", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedBorderColor = Carrot500,
                        unfocusedBorderColor = Navy700
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Event Description", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedBorderColor = Carrot500,
                        unfocusedBorderColor = Navy700
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmit(
                            title,
                            date.ifBlank { "Next Weekend" },
                            location.ifBlank { "Dar es Salaam, Tanzania" },
                            description,
                            imageUrl
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_event_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Carrot500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Publish Event",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun SearchDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    posts: List<Post>,
    products: List<Product>,
    users: List<User>,
    onSelectUser: (User) -> Unit,
    onDismiss: () -> Unit
) {
    val filteredPosts = if (query.isBlank()) emptyList() else posts.filter {
        it.content.contains(query, ignoreCase = true)
    }
    val filteredProducts = if (query.isBlank()) emptyList() else products.filter {
        it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
    }
    val filteredUsers = if (query.isBlank()) emptyList() else users.filter {
        it.name.contains(query, ignoreCase = true) || it.username.contains(query, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .testTag("search_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Navy800)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Input Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search Feathered…", color = TextSecondary, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Carrot500,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Navy900,
                            unfocusedContainerColor = Navy900,
                            focusedBorderColor = Carrot500,
                            unfocusedBorderColor = Navy700
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("global_search_input")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = Navy700)

                // Results
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (query.isBlank()) {
                        item {
                            Text(
                                text = "Search for friends, topics, products or events.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        // People
                        if (filteredUsers.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PEOPLE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Carrot500
                                )
                            }
                            items(filteredUsers) { u ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSelectUser(u)
                                            onDismiss()
                                        }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = u.profileImageUrl,
                                        contentDescription = u.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(u.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                        Text("@${u.username}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }

                        // Products
                        if (filteredProducts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "MARKETPLACE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Carrot500
                                )
                            }
                            items(filteredProducts) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = p.imageUrl,
                                        contentDescription = p.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(p.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                        Text("${p.currency} ${p.price}", fontSize = 11.sp, color = Carrot500)
                                    }
                                }
                            }
                        }

                        // Posts
                        if (filteredPosts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "POSTS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Carrot500
                                )
                            }
                            items(filteredPosts) { pst ->
                                Text(
                                    text = pst.content,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Navy700, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
