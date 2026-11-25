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
     * Uses an explicit Map to guarantee that all nullable/confirmation fields
     * (receiverPickupConfirmed, receiverPickupConfirmedAt, donorHandoverConfirmed,
     * donorHandoverConfirmedAt, claimantId, claimantName, claimedAt) are physically
     * and explicitly stored in Firestore.
     */
    suspend fun createListing(listing: FoodListing): Result<String> {
        val currentUid = currentUserId
        if (currentUid.isNullOrBlank()) {
            return Result.failure(Exception("You must be signed in to create a food donation."))
        }

        val now = System.currentTimeMillis()
        val expiry = listing.expiryDateTime
        if (expiry == null) {
            return Result.failure(Exception("Please select a best before / expiry date & time."))
        }
        if (expiry <= now) {
            return Result.failure(Exception("Expiry time must be in the future."))
        }
        if (expiry <= listing.preparationDateTime) {
            return Result.failure(Exception("Expiry date and time must be after preparation time."))
        }

        return try {
            val collectionRef = firestore.collection("food_listings")
            val docRef = collectionRef.document()

            val finalListing = listing.copy(
                id = docRef.id,
                donorId = currentUid,
                status = "available",
                claimantId = null,
                claimantName = null,
                claimedAt = null,
                receiverPickupConfirmed = false,
                receiverPickupConfirmedAt = null,
                donorHandoverConfirmed = false,
                donorHandoverConfirmedAt = null,
                createdAt = now,
                updatedAt = now
            )

            val dataMap = mapOf(
                "id" to finalListing.id,
                "donorId" to finalListing.donorId,
                "donorName" to finalListing.donorName,
                "foodName" to finalListing.foodName,
                "category" to finalListing.category,
                "description" to finalListing.description,
                "quantity" to finalListing.quantity,
                "quantityUnit" to finalListing.quantityUnit,
                "photoUrls" to finalListing.photoUrls,
                "preparationDateTime" to finalListing.preparationDateTime,
                "expiryDateTime" to finalListing.expiryDateTime,
                "foodCondition" to finalListing.foodCondition,
                "allergenNotes" to finalListing.allergenNotes,
                "pickupAddress" to finalListing.pickupAddress,
                "latitude" to finalListing.latitude,
                "longitude" to finalListing.longitude,
                "pickupInstructions" to finalListing.pickupInstructions,
                "status" to finalListing.status,
                "claimantId" to finalListing.claimantId,
                "claimantName" to finalListing.claimantName,
                "claimedAt" to finalListing.claimedAt,
                "receiverPickupConfirmed" to finalListing.receiverPickupConfirmed,
                "receiverPickupConfirmedAt" to finalListing.receiverPickupConfirmedAt,
                "donorHandoverConfirmed" to finalListing.donorHandoverConfirmed,
                "donorHandoverConfirmedAt" to finalListing.donorHandoverConfirmedAt,
                "createdAt" to finalListing.createdAt,
                "updatedAt" to finalListing.updatedAt
            )

            docRef.set(dataMap).await()
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
                                doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it }
                            }.sortedByDescending { it.createdAt }
                            trySend(list)
                        }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it }
                    }
                    trySend(listings)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves real-time stream of food listings claimed by the specified receiver (claimantId).
     */
    fun getUserClaims(claimantId: String): Flow<List<FoodListing>> = callbackFlow {
        if (claimantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("food_listings")
            .whereEqualTo("claimantId", claimantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback to client query without orderBy if index is building
                    firestore.collection("food_listings")
                        .whereEqualTo("claimantId", claimantId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            val list = querySnapshot.documents.mapNotNull { doc ->
                                doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it }
                            }.filter { it.status != "available" }
                             .sortedByDescending { it.claimedAt?.seconds ?: it.createdAt }
                            trySend(list)
                        }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val claims = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it }
                    }.filter { it.status != "available" }
                     .sortedByDescending { it.claimedAt?.seconds ?: it.createdAt }
                    trySend(claims)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves real-time stream of all available food listings published across the community.
     * Filters for status == "available" and valid future expiryDateTime, sorted locally by createdAt descending.
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
                    val now = System.currentTimeMillis()
                    val listings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it }
                    }.filter { listing ->
                        listing.expiryDateTime != null && listing.expiryDateTime > now
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
            if (doc.exists()) doc.toObject(FoodListing::class.java)?.let { if (it.id.isBlank()) it.copy(id = doc.id) else it } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Atomically claims an available food listing using a Firestore transaction.
     * Validates current user authentication, verifies status is "available",
     * prevents donor self-claiming, checks expiry, and updates status to "claimed"
     * with verified claimant metadata. Includes stage-specific diagnostic logging.
     */
    suspend fun claimListing(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        Log.d("FoodRepository", "CLAIM_STAGE_0: Starting claimListing for listingId=$listingId, currentUid=$currentUid")
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in to claim food."))
        }

        val claimantName: String
        try {
            // 1. Fetch verified profile fullName from users/{uid} document
            val userSnapshot = firestore.collection("users").document(currentUid).get().await()
            val exists = userSnapshot.exists()
            claimantName = userSnapshot.getString("fullName")?.trim().orEmpty()
            Log.d("FoodRepository", "CLAIM_STAGE_1_PROFILE: Profile read complete. exists=$exists, hasFullName=${claimantName.isNotBlank()}")
            if (claimantName.isBlank()) {
                return@withContext Result.failure(Exception("Profile full name is required to claim food. Please complete your profile."))
            }
        } catch (e: Exception) {
            Log.e("FoodRepository", "CLAIM_STAGE_1_ERROR: Failed to read users/$currentUid", e)
            return@withContext Result.failure(Exception("Failed to load user profile: ${e.message}", e))
        }

        try {
            // 2. Execute atomic Firestore Transaction
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                Log.d("FoodRepository", "CLAIM_STAGE_2_TX_START: Reading listing document $listingId")
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val status = snapshot.getString("status") ?: ""
                val donorId = snapshot.getString("donorId") ?: ""
                val rawExpiry = snapshot.get("expiryDateTime")
                val expiryLong = snapshot.getLong("expiryDateTime")
                val isDonorMatch = (donorId == currentUid)

                Log.d("FoodRepository", "CLAIM_STAGE_2_TX_DOC: status=$status, donorId=$donorId, currentUid=$currentUid, isDonorMatch=$isDonorMatch, rawExpiryType=${rawExpiry?.javaClass?.simpleName}, expiryVal=$expiryLong")

                if (status != "available") {
                    throw Exception("This food listing is no longer available.")
                }
                if (isDonorMatch) {
                    throw Exception("Donors cannot claim their own food listings.")
                }
                if (expiryLong == null) {
                    throw Exception("Listing lacks a valid expiry timestamp.")
                }
                if (expiryLong <= System.currentTimeMillis()) {
                    throw Exception("This food listing has expired and can no longer be claimed.")
                }

                val updates = mapOf(
                    "status" to "claimed",
                    "claimantId" to currentUid,
                    "claimantName" to claimantName,
                    "claimedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to System.currentTimeMillis()
                )
                Log.d("FoodRepository", "CLAIM_STAGE_2_TX_COMMIT: Applying transaction update payload=$updates")
                transaction.update(docRef, updates)
            }.await()

            Log.d("FoodRepository", "CLAIM_STAGE_3_SUCCESS: Claim transaction committed successfully for $listingId")
            Result.success(Unit)
        } catch (e: Exception) {
            val exClass = e.javaClass.simpleName
            val exMsg = e.message ?: "Unknown error"
            val cause = e.cause?.toString() ?: "None"
            Log.e("FoodRepository", "CLAIM_STAGE_3_ERROR: Transaction failed. class=$exClass, message=$exMsg, cause=$cause", e)
            Result.failure(e)
        }
    }

    /**
     * Confirms pickup collection by the authenticated receiver (claimant).
     * If donor has already confirmed handover, automatically completes the listing.
     */
    suspend fun confirmPickup(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in to confirm pickup."))
        }

        try {
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val claimantId = snapshot.getString("claimantId") ?: ""
                val status = snapshot.getString("status") ?: ""
                val alreadyConfirmed = snapshot.getBoolean("receiverPickupConfirmed") == true

                if (claimantId != currentUid) {
                    throw Exception("Only the assigned claimant can confirm pickup.")
                }

                if (alreadyConfirmed) {
                    // Calling party has already confirmed: successful no-op regardless of claimed or completed status
                    return@runTransaction
                }

                if (status != "claimed") {
                    throw Exception("Listing is not in a claimed state.")
                }

                val donorHandoverConfirmed = snapshot.getBoolean("donorHandoverConfirmed") == true
                val newStatus = if (donorHandoverConfirmed) "completed" else "claimed"

                val updates = mutableMapOf<String, Any>(
                    "receiverPickupConfirmed" to true,
                    "receiverPickupConfirmedAt" to FieldValue.serverTimestamp(),
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
                transaction.update(docRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Failed to confirm pickup."))
        }
    }

    /**
     * Confirms handover by the authenticated donor.
     * If receiver has already confirmed pickup, automatically completes the listing.
     */
    suspend fun confirmHandover(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in to confirm handover."))
        }

        try {
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val donorId = snapshot.getString("donorId") ?: ""
                val status = snapshot.getString("status") ?: ""
                val alreadyConfirmed = snapshot.getBoolean("donorHandoverConfirmed") == true

                if (donorId != currentUid) {
                    throw Exception("Only the original donor can confirm handover.")
                }

                if (alreadyConfirmed) {
                    // Calling party has already confirmed: successful no-op regardless of claimed or completed status
                    return@runTransaction
                }

                if (status != "claimed") {
                    throw Exception("Listing is not in a claimed state.")
                }

                val receiverPickupConfirmed = snapshot.getBoolean("receiverPickupConfirmed") == true
                val newStatus = if (receiverPickupConfirmed) "completed" else "claimed"

                val updates = mutableMapOf<String, Any>(
                    "donorHandoverConfirmed" to true,
                    "donorHandoverConfirmedAt" to FieldValue.serverTimestamp(),
                    "status" to newStatus,
                    "updatedAt" to System.currentTimeMillis()
                )
                transaction.update(docRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Failed to confirm handover."))
        }
    }

    /**
     * Allows receiver to release claim before confirmation, restoring listing availability if not expired.
     */
    suspend fun releaseClaim(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in."))
        }

        try {
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val claimantId = snapshot.getString("claimantId") ?: ""
                val status = snapshot.getString("status") ?: ""
                val receiverConfirmed = snapshot.getBoolean("receiverPickupConfirmed") == true
                val donorConfirmed = snapshot.getBoolean("donorHandoverConfirmed") == true

                if (claimantId != currentUid) {
                    throw Exception("Only the assigned claimant can release this claim.")
                }
                if (status != "claimed") {
                    throw Exception("Listing cannot be released in its current state.")
                }
                if (receiverConfirmed || donorConfirmed) {
                    throw Exception("Cannot release claim after confirmation has started.")
                }

                val expiryDateTime = snapshot.getLong("expiryDateTime")
                val currentTime = System.currentTimeMillis()
                val isExpired = expiryDateTime == null || expiryDateTime <= currentTime
                val targetStatus = if (isExpired) "cancelled" else "available"

                val updates = mutableMapOf<String, Any?>(
                    "status" to targetStatus,
                    "receiverPickupConfirmed" to false,
                    "donorHandoverConfirmed" to false,
                    "updatedAt" to currentTime
                )
                updates["claimantId"] = null
                updates["claimantName"] = null
                updates["claimedAt"] = null
                updates["receiverPickupConfirmedAt"] = null
                updates["donorHandoverConfirmedAt"] = null

                transaction.update(docRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Failed to release claim."))
        }
    }

    /**
     * Allows donor to withdraw a claimed or available donation.
     */
    suspend fun withdrawDonation(listingId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val currentUid = auth.currentUser?.uid
        if (currentUid.isNullOrBlank()) {
            return@withContext Result.failure(Exception("You must be signed in."))
        }

        try {
            val docRef = firestore.collection("food_listings").document(listingId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists()) {
                    throw Exception("Food listing no longer exists.")
                }

                val donorId = snapshot.getString("donorId") ?: ""
                val status = snapshot.getString("status") ?: ""
                val receiverConfirmed = snapshot.getBoolean("receiverPickupConfirmed") == true
                val donorConfirmed = snapshot.getBoolean("donorHandoverConfirmed") == true

                if (donorId != currentUid) {
                    throw Exception("Only the original donor can withdraw this donation.")
                }
                if (status != "available" && status != "claimed") {
                    throw Exception("Donation cannot be withdrawn in its current status ($status).")
                }
                if (receiverConfirmed || donorConfirmed) {
                    throw Exception("Cannot withdraw donation after confirmation has started.")
                }

                val updates = mapOf(
                    "status" to "cancelled",
                    "updatedAt" to System.currentTimeMillis()
                )
                transaction.update(docRef, updates)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception(e.message ?: "Failed to withdraw donation."))
        }
    }
}
