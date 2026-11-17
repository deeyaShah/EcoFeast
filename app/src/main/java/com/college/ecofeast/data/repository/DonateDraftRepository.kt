package com.college.ecofeast.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.college.ecofeast.data.model.DonateDraft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DonateDraftRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("ecofeast_donate_draft", Context.MODE_PRIVATE)
    private val draftPhotoDir = File(context.filesDir, "draft_photos")

    init {
        if (!draftPhotoDir.exists()) {
            draftPhotoDir.mkdirs()
        }
    }

    suspend fun saveDraft(draft: DonateDraft) = withContext(Dispatchers.IO) {
        val json = JSONObject().apply {
            put("wizardStep", draft.wizardStep)
            put("isWizardActive", draft.isWizardActive)
            put("foodName", draft.foodName)
            put("category", draft.category)
            put("description", draft.description)
            put("quantityText", draft.quantityText)
            put("quantityUnit", draft.quantityUnit)
            put("localPhotoPaths", JSONArray(draft.localPhotoPaths))
            put("preparationTimestamp", draft.preparationTimestamp)
            if (draft.expiryTimestamp != null) {
                put("expiryTimestamp", draft.expiryTimestamp)
            }
            put("foodCondition", draft.foodCondition)
            put("allergenNotes", draft.allergenNotes)
            put("pickupAddress", draft.pickupAddress)
            if (draft.latitude != null) put("latitude", draft.latitude)
            if (draft.longitude != null) put("longitude", draft.longitude)
            put("pickupInstructions", draft.pickupInstructions)
            put("lastUpdated", draft.lastUpdated)
        }

        prefs.edit()
            .putString("draft_json", json.toString())
            .putBoolean("has_draft", true)
            .apply()
    }

    fun getDraftSync(): DonateDraft? {
        if (!prefs.getBoolean("has_draft", false)) return null
        val jsonStr = prefs.getString("draft_json", null) ?: return null

        return try {
            val json = JSONObject(jsonStr)
            val photoPathsArray = json.optJSONArray("localPhotoPaths")
            val photoPaths = mutableListOf<String>()
            if (photoPathsArray != null) {
                for (i in 0 until photoPathsArray.length()) {
                    val path = photoPathsArray.getString(i)
                    if (File(path).exists()) {
                        photoPaths.add(path)
                    }
                }
            }

            DonateDraft(
                wizardStep = json.optInt("wizardStep", 1),
                isWizardActive = json.optBoolean("isWizardActive", true),
                foodName = json.optString("foodName", ""),
                category = json.optString("category", ""),
                description = json.optString("description", ""),
                quantityText = json.optString("quantityText", ""),
                quantityUnit = json.optString("quantityUnit", "Portions"),
                localPhotoPaths = photoPaths,
                preparationTimestamp = json.optLong("preparationTimestamp", System.currentTimeMillis()),
                expiryTimestamp = if (json.has("expiryTimestamp")) json.getLong("expiryTimestamp") else null,
                foodCondition = json.optString("foodCondition", "Freshly Prepared"),
                allergenNotes = json.optString("allergenNotes", ""),
                pickupAddress = json.optString("pickupAddress", ""),
                latitude = if (json.has("latitude")) json.getDouble("latitude") else null,
                longitude = if (json.has("longitude")) json.getDouble("longitude") else null,
                pickupInstructions = json.optString("pickupInstructions", ""),
                lastUpdated = json.optLong("lastUpdated", System.currentTimeMillis())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getDraft(): DonateDraft? = withContext(Dispatchers.IO) {
        getDraftSync()
    }

    suspend fun clearDraft() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        // Clean up draft photos
        try {
            val files = draftPhotoDir.listFiles()
            files?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasDraft(): Boolean {
        return prefs.getBoolean("has_draft", false)
    }

    /**
     * Copies a Content URI (from Photo Picker or Camera) to app-private draft storage
     * so it persists cleanly across app restarts.
     */
    suspend fun copyUriToDraftStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(draftPhotoDir, "photo_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (file.exists() && file.length() > 0) {
                file.absolutePath
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
