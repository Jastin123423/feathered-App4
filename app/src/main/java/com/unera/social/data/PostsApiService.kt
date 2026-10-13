package com.unera.social.data

import com.unera.social.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object PostsApiService {
    private const val BASE_URL = "https://featheredsocial.site"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    /**
     * Fetch feed posts from api/feeds (used when user is logged in)
     */
    suspend fun fetchFeedPosts(userId: Long? = null, limit: Int = 50): List<Post> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Post>()
        try {
            val url = URL("$BASE_URL/api/feeds?limit=$limit")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                if (userId != null && userId > 0) {
                    setRequestProperty("x-user-id", userId.toString())
                }
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }.trim()
                if (responseText.startsWith("{")) {
                    val root = JSONObject(responseText)
                    val feedArray = root.optJSONArray("feed") ?: root.optJSONArray("data")
                    if (feedArray != null) {
                        for (i in 0 until feedArray.length()) {
                            val obj = feedArray.optJSONObject(i) ?: continue
                            parseFeedItemToPost(obj)?.let { result.add(it) }
                        }
                    }
                } else if (responseText.startsWith("[")) {
                    val arr = JSONArray(responseText)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        parseFeedItemToPost(obj)?.let { result.add(it) }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    /**
     * Fetch public posts from api/posts (used for non-logged-in users)
     */
    suspend fun fetchPublicPosts(): List<Post> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Post>()
        try {
            val url = URL("$BASE_URL/api/posts")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }.trim()
                if (responseText.startsWith("[")) {
                    val arr = JSONArray(responseText)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        parsePostJson(obj)?.let { result.add(it) }
                    }
                } else if (responseText.startsWith("{")) {
                    val root = JSONObject(responseText)
                    val arr = root.optJSONArray("posts") ?: root.optJSONArray("data") ?: root.optJSONArray("feed")
                    if (arr != null) {
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parsePostJson(obj)?.let { result.add(it) }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    /**
     * Fetch user posts from api/post/by-user or api/posts/by-user (used for user profiles)
     */
    suspend fun fetchUserPosts(userId: Long, limit: Int = 50): List<Post> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Post>()
        try {
            val url = URL("$BASE_URL/api/posts/by-user?user_id=$userId&limit=$limit")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("x-user-id", userId.toString())
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }.trim()
                if (responseText.startsWith("{")) {
                    val root = JSONObject(responseText)
                    val postsArray = root.optJSONArray("posts") ?: root.optJSONArray("data") ?: root.optJSONArray("feed")
                    if (postsArray != null) {
                        for (i in 0 until postsArray.length()) {
                            val obj = postsArray.optJSONObject(i) ?: continue
                            parseFeedItemToPost(obj)?.let { result.add(it) }
                        }
                    }
                } else if (responseText.startsWith("[")) {
                    val arr = JSONArray(responseText)
                    for (i in 0 until arr.length()) {
                        val obj = arr.optJSONObject(i) ?: continue
                        parseFeedItemToPost(obj)?.let { result.add(it) }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to /api/posts?user_id=$userId if empty
        if (result.isEmpty()) {
            try {
                val url = URL("$BASE_URL/api/posts?user_id=$userId")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", USER_AGENT)
                }
                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseText = reader.use { it.readText() }.trim()
                    if (responseText.startsWith("[")) {
                        val arr = JSONArray(responseText)
                        for (i in 0 until arr.length()) {
                            val obj = arr.optJSONObject(i) ?: continue
                            parsePostJson(obj)?.let { result.add(it) }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        result
    }

    private fun parseFeedItemToPost(obj: JSONObject): Post? {
        if (obj.optInt("is_deleted", 0) == 1) return null

        val id = obj.optLong("id", obj.optLong("post_id", System.currentTimeMillis()))
        val userId = obj.optLong("user_id", obj.optLong("owner_id", 1L))
        val authorObj = obj.optJSONObject("author") ?: obj.optJSONObject("user")

        val authorName = authorObj?.optString("name")?.takeIf { it.isNotBlank() }
            ?: obj.optString("name").takeIf { it.isNotBlank() && it != "null" }
            ?: authorObj?.optString("username")?.takeIf { it.isNotBlank() }
            ?: obj.optString("username").takeIf { it.isNotBlank() && it != "null" }
            ?: if (userId == 1L) "Jastin Beda" else "User $userId"

        val username = authorObj?.optString("username")?.takeIf { it.isNotBlank() }
            ?: obj.optString("username").takeIf { it.isNotBlank() && it != "null" }
            ?: "@${authorName.lowercase().replace(" ", "")}"

        val avatar = authorObj?.optString("profile_image_url")?.takeIf { it.isNotBlank() && it != "null" }
            ?: authorObj?.optString("avatar_url")?.takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("profile_image_url").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("avatar_url").takeIf { it.isNotBlank() && it != "null" }
            ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=800"

        val isVerified = authorObj?.optBoolean("is_verified", false)
            ?: obj.optBoolean("is_verified", obj.optBoolean("verified", false))
        val role = authorObj?.optString("role") ?: obj.optString("role", "user")

        val author = User(
            id = userId,
            name = authorName,
            username = username,
            profileImageUrl = avatar,
            isVerified = isVerified,
            role = role
        )

        val content = obj.optString("content").takeIf { it != "null" }
            ?: obj.optString("description").takeIf { it != "null" }
            ?: ""

        val mediaUrls = extractMediaUrls(obj)
        val mediaUrl = obj.optString("media_url").takeIf { it.isNotBlank() && it != "null" }
            ?: mediaUrls.firstOrNull()

        val backgroundStyle = obj.optString("colours").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("background_style").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("background").takeIf { it.isNotBlank() && it != "null" }

        val groupId = if (obj.has("group_id") && !obj.isNull("group_id") && obj.optLong("group_id") > 0) {
            obj.optLong("group_id")
        } else null

        val groupName = obj.optString("group_name").takeIf { it.isNotBlank() && it != "null" }
        val groupImage = obj.optString("group_image").takeIf { it.isNotBlank() && it != "null" }

        val rawDate = obj.optString("created_at").takeIf { it.isNotBlank() && it != "null" }
        val timestamp = formatTimestamp(rawDate)

        val likesCount = obj.optInt("reactions_count", obj.optInt("likesCount", obj.optInt("likes_count", 0)))
        val commentsCount = obj.optInt("comments_count", obj.optInt("commentsCount", 0))
        val sharesCount = obj.optInt("shares", obj.optInt("shares_count", 0))

        val postType = obj.optString("source").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("item_type").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("post_type").takeIf { it.isNotBlank() && it != "null" }
            ?: "post"

        return Post(
            id = id,
            author = author,
            content = content,
            mediaUrl = mediaUrl,
            mediaUrls = mediaUrls,
            timestamp = timestamp,
            likesCount = likesCount,
            commentsCount = commentsCount,
            sharesCount = sharesCount,
            isLiked = false,
            isSaved = false,
            backgroundStyle = backgroundStyle,
            groupId = groupId,
            groupName = groupName,
            groupImageUrl = groupImage,
            postType = postType
        )
    }

    private fun parsePostJson(obj: JSONObject): Post? {
        if (obj.optInt("is_deleted", 0) == 1) return null

        val id = obj.optLong("id", System.currentTimeMillis())
        val userId = obj.optLong("user_id", 1L)

        val authorName = if (userId == 1L) "Jastin Beda" else "User $userId"
        val username = if (userId == 1L) "Jastin Beda" else "@user$userId"
        val avatar = if (userId == 1L) {
            "https://media.featheredsocial.site/uploads/images/1791201400713-120d7c17f3d2e8_original.jpg?t=1791201404884"
        } else {
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=800"
        }

        val author = User(
            id = userId,
            name = authorName,
            username = username,
            profileImageUrl = avatar,
            isVerified = userId == 1L,
            role = if (userId == 1L) "admin" else "user"
        )

        val content = obj.optString("content").takeIf { it != "null" } ?: ""
        val mediaUrls = extractMediaUrls(obj)
        val mediaUrl = obj.optString("media_url").takeIf { it.isNotBlank() && it != "null" }
            ?: mediaUrls.firstOrNull()

        val backgroundStyle = obj.optString("colours").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("background_style").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("background").takeIf { it.isNotBlank() && it != "null" }

        val groupId = if (obj.has("group_id") && !obj.isNull("group_id") && obj.optLong("group_id") > 0) {
            obj.optLong("group_id")
        } else null

        val rawDate = obj.optString("created_at").takeIf { it.isNotBlank() && it != "null" }
        val timestamp = formatTimestamp(rawDate)

        val sharesCount = obj.optInt("shares", obj.optInt("shares_count", 0))

        return Post(
            id = id,
            author = author,
            content = content,
            mediaUrl = mediaUrl,
            mediaUrls = mediaUrls,
            timestamp = timestamp,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = sharesCount,
            isLiked = false,
            isSaved = false,
            backgroundStyle = backgroundStyle,
            groupId = groupId,
            postType = "post"
        )
    }

    private fun extractMediaUrls(obj: JSONObject): List<String> {
        val list = mutableListOf<String>()
        val mediaObj = obj.opt("media_urls")
        if (mediaObj is JSONArray) {
            for (i in 0 until mediaObj.length()) {
                val u = mediaObj.optString(i)
                if (u.isNotBlank() && u != "null") list.add(u)
            }
        } else if (mediaObj is String && mediaObj.startsWith("[")) {
            try {
                val arr = JSONArray(mediaObj)
                for (i in 0 until arr.length()) {
                    val u = arr.optString(i)
                    if (u.isNotBlank() && u != "null") list.add(u)
                }
            } catch (_: Exception) {}
        }

        val mediaArray = obj.optJSONArray("media")
        if (mediaArray != null && list.isEmpty()) {
            for (i in 0 until mediaArray.length()) {
                val m = mediaArray.optJSONObject(i) ?: continue
                val u = m.optString("feed").takeIf { it.isNotBlank() && it != "null" }
                    ?: m.optString("full").takeIf { it.isNotBlank() && it != "null" }
                    ?: m.optString("original").takeIf { it.isNotBlank() && it != "null" }
                    ?: m.optString("720").takeIf { it.isNotBlank() && it != "null" }
                    ?: m.optString("thumb").takeIf { it.isNotBlank() && it != "null" }
                if (u != null) list.add(u)
            }
        }

        val single = obj.optString("media_url").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("feed_url").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("full_url").takeIf { it.isNotBlank() && it != "null" }
            ?: obj.optString("thumb_url").takeIf { it.isNotBlank() && it != "null" }
        if (single != null && !list.contains(single)) {
            list.add(0, single)
        }

        return list
    }

    private fun formatTimestamp(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return "Just now"
        return try {
            val clean = raw.replace("T", " ").substringBefore(".")
            clean
        } catch (_: Exception) {
            raw
        }
    }
}
