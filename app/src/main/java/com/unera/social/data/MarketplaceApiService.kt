package com.unera.social.data

import com.unera.social.model.Product
import com.unera.social.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object MarketplaceApiService {
    private const val BASE_URL = "https://featheredsocial.site"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

    suspend fun fetchLiveProducts(): List<Product> = withContext(Dispatchers.IO) {
        val fetchedList = mutableListOf<Product>()
        try {
            val url = URL("$BASE_URL/api/products")
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
                    val array = JSONArray(responseText)
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        parseProductJson(obj)?.let { fetchedList.add(it) }
                    }
                } else if (responseText.startsWith("{")) {
                    val root = JSONObject(responseText)
                    val array = root.optJSONArray("products") ?: root.optJSONArray("data")
                    if (array != null) {
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i) ?: continue
                            parseProductJson(obj)?.let { fetchedList.add(it) }
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Also check feeds endpoint if products list is small or needs enrichment
        if (fetchedList.isEmpty()) {
            try {
                val url = URL("$BASE_URL/api/feeds?limit=100")
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
                    if (responseText.startsWith("{")) {
                        val root = JSONObject(responseText)
                        val feed = root.optJSONArray("feed")
                        if (feed != null) {
                            for (i in 0 until feed.length()) {
                                val item = feed.optJSONObject(i) ?: continue
                                val isProduct = item.optString("source") == "product" || 
                                                item.optString("item_type") == "product" ||
                                                item.has("product_id") || item.has("product_id2")
                                if (isProduct) {
                                    parseProductJson(item)?.let { fetchedList.add(it) }
                                }
                            }
                        }
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fetchedList
    }

    suspend fun publishProduct(product: Product): Product? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/api/products")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("x-user-id", product.seller.id.toString())
            }

            val json = JSONObject().apply {
                put("title", product.title)
                put("description", product.description)
                put("category", product.category)
                put("main_price", product.price)
                if (product.discountPrice != null && product.discountPrice > 0) {
                    put("discount_price", product.discountPrice)
                } else {
                    put("discount_price", JSONObject.NULL)
                }
                put("address", product.location)
                put("country", product.country)
                put("phone_number", product.phoneNumber)
                put("quantity", product.quantity)
                val imagesArray = JSONArray()
                if (product.images.isNotEmpty()) {
                    product.images.forEach { imagesArray.put(it) }
                } else {
                    imagesArray.put(product.imageUrl)
                }
                put("images", imagesArray)
                put("image_variants", JSONArray())
                put("status", "active")
                put("seller_id", product.seller.id)
                put("seller_name", product.seller.name)
                put("seller_avatar", product.seller.profileImageUrl)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(json.toString())
                it.flush()
            }

            if (conn.responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseText = reader.use { it.readText() }.trim()
                conn.disconnect()
                if (responseText.startsWith("{")) {
                    val root = JSONObject(responseText)
                    val prodObj = root.optJSONObject("product") ?: root
                    return@withContext parseProductJson(prodObj) ?: product
                }
                return@withContext product
            }
            conn.disconnect()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseProductJson(obj: JSONObject): Product? {
        val id = obj.optLong("id", obj.optLong("product_id", 0L))
        val title = obj.optString("title", "").ifBlank { return null }
        val mainPrice = obj.optDouble("main_price", obj.optDouble("price", 0.0))
        val discountPrice = if (obj.has("discount_price") && !obj.isNull("discount_price")) {
            obj.optDouble("discount_price", 0.0).takeIf { it > 0 }
        } else null

        val category = obj.optString("category", "General")
        val address = obj.optString("address", obj.optString("location", "Tanzania"))
        val country = obj.optString("country", "TZ")
        val description = obj.optString("description", "")
        val phone = obj.optString("phone_number", obj.optString("phone", ""))
        val isSponsored = obj.optBoolean("is_sponsored", false) || obj.optBoolean("sponsored", false)

        val imagesList = mutableListOf<String>()
        val imagesJson = obj.optJSONArray("images")
        if (imagesJson != null) {
            for (j in 0 until imagesJson.length()) {
                val img = imagesJson.optString(j, "").trim()
                if (img.isNotBlank()) imagesList.add(img)
            }
        }
        val imageUrl = obj.optString("imageUrl", obj.optString("image_url", obj.optString("media_url", "")))
        if (imageUrl.isNotBlank() && !imagesList.contains(imageUrl)) {
            imagesList.add(0, imageUrl)
        }

        val primaryImage = imagesList.firstOrNull() ?: "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=800"

        val sellerId = obj.optLong("seller_id", obj.optLong("user_id", 101L))
        val sellerName = obj.optString("seller_name", obj.optString("seller_username", obj.optString("username", obj.optString("name", "Seller"))))
        val sellerAvatar = obj.optString("seller_avatar", obj.optString("profile_image_url", obj.optString("avatar_url", "")))
            .ifBlank { "https://ui-avatars.com/api/?name=${sellerName.replace(" ", "+")}&background=1877F2&color=FFFFFF" }

        val seller = User(
            id = sellerId,
            name = sellerName,
            username = obj.optString("seller_username", sellerName.lowercase().replace(" ", "_")),
            profileImageUrl = sellerAvatar,
            location = address,
            isVerified = obj.optBoolean("seller_is_verified", obj.optBoolean("is_verified", false)) || obj.optInt("seller_is_verified", 0) == 1
        )

        return Product(
            id = if (id != 0L) id else System.currentTimeMillis() + (Math.random() * 1000).toLong(),
            title = title,
            price = mainPrice,
            discountPrice = discountPrice,
            currency = if (country.equals("TZ", ignoreCase = true)) "TZS" else if (country.equals("KE", ignoreCase = true)) "KES" else "USD",
            category = category,
            location = address,
            country = country,
            imageUrl = primaryImage,
            images = if (imagesList.isNotEmpty()) imagesList else listOf(primaryImage),
            description = description,
            seller = seller,
            phoneNumber = phone,
            isSponsored = isSponsored,
            isFresh = true,
            createdAt = obj.optString("created_at", "")
        )
    }
}
