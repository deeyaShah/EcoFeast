package com.college.ecofeast.data.repository

import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.college.ecofeast.data.model.FoodListing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class FoodRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Cloudinary Configuration
    private val cloudinaryCloudName = "do5nrhnkj"
    private val cloudinaryUploadPreset = "ecofeast_food_upload"
    private val cloudinaryFolder = "ecofeast/food_images"
    private val maxFileSizeBytes = 10 * 1024 * 1024L // 10 MB limit

    /**
     * Uploads local photo files directly to Cloudinary using unsigned multipart HTTP POST.
     * Validates image MIME types and 10MB size limit before uploading.
     * Reuses existing Cloudinary secure_url links if already uploaded in previous attempt.
     */
    suspend fun uploadPhotosToCloudinary(localPaths: List<String>): Result<List<String>> = withContext(Dispatchers.IO) {
        val downloadUrls = mutableListOf<String>()

        for (path in localPaths) {
            // 1. If path is already a Cloudinary secure_url from a previous partial upload, reuse it directly
            if (path.startsWith("http://") || path.startsWith("https://")) {
                downloadUrls.add(path)
                continue
            }

            val file = File(path)
            if (!file.exists() || !file.isFile) {
                return@withContext Result.failure(Exception("Image file not found: ${file.name}"))
            }

            // 2. Size Check (max 10 MB)
            if (file.length() <= 0) {
                return@withContext Result.failure(Exception("Image file '${file.name}' is empty."))
            }
            if (file.length() > maxFileSizeBytes) {
                return@withContext Result.failure(Exception("Image '${file.name}' exceeds the maximum size limit of 10 MB."))
            }

            // 3. MIME Type Check
            val ext = MimeTypeMap.getFileExtensionFromUrl(file.path)?.lowercase() ?: file.extension.lowercase()
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "image/jpeg"
            if (!mimeType.startsWith("image/") && ext !in listOf("jpg", "jpeg", "png", "webp", "heic")) {
                return@withContext Result.failure(Exception("File '${file.name}' is not a valid image format."))
            }

            // 4. Perform Cloudinary Unsigned Multipart Upload
            try {
                val boundary = "----EcoFeastCloudinary" + System.currentTimeMillis()
                val lineEnd = "\r\n"
                val twoHyphens = "--"

                val url = URL("https://api.cloudinary.com/v1_1/$cloudinaryCloudName/image/upload")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 15000 // 15 seconds
                conn.readTimeout = 30000    // 30 seconds
                conn.requestMethod = "POST"
                conn.doInput = true
                conn.doOutput = true
                conn.useCaches = false
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

                val dos = DataOutputStream(conn.outputStream)

                // Parameter: upload_preset
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"$lineEnd$lineEnd")
                dos.writeBytes(cloudinaryUploadPreset + lineEnd)

                // Parameter: folder
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"folder\"$lineEnd$lineEnd")
                dos.writeBytes(cloudinaryFolder + lineEnd)

                // File Parameter: file
                dos.writeBytes(twoHyphens + boundary + lineEnd)
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"$lineEnd")
                dos.writeBytes("Content-Type: $mimeType$lineEnd$lineEnd")

                // Write Image Binary Data
                file.inputStream().use { inputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        dos.write(buffer, 0, bytesRead)
                    }
                }

                dos.writeBytes(lineEnd)
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                dos.flush()
                dos.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val responseStr = reader.use { it.readText() }
                    conn.disconnect()

                    val json = JSONObject(responseStr)
                    val secureUrl = json.optString("secure_url")

                    if (secureUrl.isNotBlank()) {
                        downloadUrls.add(secureUrl)
                    } else {
                        return@withContext Result.failure(Exception("Cloudinary upload failed: Missing secure_url in response."))
                    }
                } else {
                    val errorStream = conn.errorStream ?: conn.inputStream
                    val errorStr = BufferedReader(InputStreamReader(errorStream)).use { it.readText() }
                    conn.disconnect()

                    val errorMsg = try {
                        JSONObject(errorStr).optJSONObject("error")?.optString("message") ?: errorStr
                    } catch (e: Exception) {
                        "HTTP $responseCode"
                    }
                    return@withContext Result.failure(Exception("Cloudinary upload error: $errorMsg"))
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Result.failure(Exception("Network error uploading '${file.name}': ${e.message ?: "Connection failed"}"))
            }
        }

        Result.success(downloadUrls)
    }

    /**
     * Uploads local photo files to Firebase Storage in `food_images/{donorId}/{uuid}.jpg`
     * (Preserved for temporary backward compatibility during testing).
     */
    suspend fun uploadPhotos(localPaths: List<String>, donorId: String): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()
            for (path in localPaths) {
                val file = File(path)
                if (!file.exists()) continue

                val fileUri = Uri.fromFile(file)
                val photoRef = storage.reference
                    .child("food_images")
                    .child(donorId)
                    .child("${UUID.randomUUID()}.jpg")

                // Upload file
                photoRef.putFile(fileUri).await()
                val url = photoRef.downloadUrl.await().toString()
                downloadUrls.add(url)
            }
            Result.success(downloadUrls)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Creates a new food listing document in Cloud Firestore `food_listings` collection.
     */
    suspend fun createListing(listing: FoodListing): Result<String> {
        return try {
            val collectionRef = firestore.collection("food_listings")
            val docRef = collectionRef.document()

            val finalListing = listing.copy(
                id = docRef.id,
                donorId = currentUserId ?: listing.donorId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            docRef.set(finalListing).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Retrieves real-time stream of food listings published by the specified donor.
     */
    fun getUserListings(donorId: String): Flow<List<FoodListing>> = callbackFlow {
        if (donorId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("food_listings")
            .whereEqualTo("donorId", donorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback to client query without orderBy if index is building
                    firestore.collection("food_listings")
                        .whereEqualTo("donorId", donorId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val list = querySnapshot.documents.mapNotNull { doc ->
                                doc.toObject(FoodListing::class.java)
                            }.sortedByDescending { it.createdAt }
                            trySend(list)
                        }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodListing::class.java)
                    }
                    trySend(listings)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves real-time stream of all available food listings published across the community.
     * Filters for status == "available" and sorts locally by createdAt descending.
     */
    fun getAvailableListings(): Flow<List<FoodListing>> = callbackFlow {
        val listenerRegistration = firestore.collection("food_listings")
            .whereEqualTo("status", "available")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FoodRepository", "Error listening to available food listings", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodListing::class.java)
                    }.sortedByDescending { it.createdAt }
                    trySend(listings)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves a single food listing by its document ID.
     */
    suspend fun getListingById(listingId: String): FoodListing? {
        return try {
            val doc = firestore.collection("food_listings").document(listingId).get().await()
            if (doc.exists()) doc.toObject(FoodListing::class.java) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Atomically claims an available food listing using a Firestore transaction.
     * Validates current user authentication, verifies status is "available",
     * prevents donor self-claiming, checks expiry, and updates status to "claimed"
     * with verified claimant metadata.
     */
    suspend fun claimListing(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in to claim food."))
        }

        try {
            // 1. Fetch verified profile fullName from users/{uid} document
            val userSnapshot = firestore.collection("users").document(currentUid).get().await()
            val claimantName = userSnapshot.getString("fullName")?.trim().orEmpty()
            if (claimantName.isBlank()) {
                return@withContext Result.failure(Exception("Profile full name is required to claim food. Please complete your profile."))
            }

            // 2. Execute atomic Firestore Transaction
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val status = snapshot.getString("status") ?: ""
                val donorId = snapshot.getString("donorId") ?: ""
                val expiryDateTime = snapshot.getLong("expiryDateTime")

                if (status != "available") {
                    throw Exception("This food listing is no longer available.")
                }
                if (donorId == currentUid) {
                    throw Exception("Donors cannot claim their own food listings.")
                }
                if (expiryDateTime != null && expiryDateTime <= System.currentTimeMillis()) {
                    throw Exception("This food listing has expired and can no longer be claimed.")
                }

                val updates = mapOf(
                    "status" to "claimed",
                    "claimantId" to currentUid,
                    "claimantName" to claimantName,
                    "claimedAt" to FieldValue.serverTimestamp()
                )
                transaction.update(docRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            val msg = e.message ?: "Failed to claim food listing."
            Result.failure(Exception(msg))
        }
    }

    /**
     * Updates listing status in Cloud Firestore (e.g. "available", "claimed", "completed", "cancelled").
     */
    suspend fun updateListingStatus(listingId: String, newStatus: String): Result<Unit> {
        return try {
            firestore.collection("food_listings")
                .document(listingId)
                .update("status", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Deletes a food listing from Cloud Firestore.
     */
    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            firestore.collection("food_listings")
                .document(listingId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
