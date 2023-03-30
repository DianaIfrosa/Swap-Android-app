package com.diana.bachelorthesis.repository

import android.net.Uri
import android.util.Log
import com.diana.bachelorthesis.OneParamCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import java.util.*

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PhotoRepository {
    private val TAG: String = PhotoRepository::class.java.name

    // for cloud
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference: StorageReference = firebaseStorage.reference

    companion object {
        @Volatile
        private var instance: PhotoRepository? = null
        fun getInstance() = instance ?: PhotoRepository().also { instance = it }

    }

    fun uploadPhotoToCloud(
        owner: String,
        itemId: String,
        imageUri: Uri,
        callback: OneParamCallback<String>
    ) {

        val itemPhotosReference = storageReference.child(owner).child(itemId)
        imageUri.let {
            val imageName = UUID.randomUUID().toString()

            itemPhotosReference.child(imageName).putFile(it).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    itemPhotosReference.child(imageName).downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d(
                            TAG,
                            "Successfully uploaded image with url $imageUrl"
                        )
                        callback.onComplete(imageUrl)

                    }.addOnFailureListener {
                        Log.w(
                            TAG,
                            "\"Couldn't get url for photo with uri $imageUri"
                        )
                        callback.onComplete(null)
                    }

                } else {
                    Log.w(
                        TAG,
                        "Photo with uri $imageUri failed to upload!"
                    )
                    callback.onComplete(null)
                }
            }
        }
    }

    suspend fun uploadPhotosToCloud(
        owner: String,
        itemId: String, imagesUri: List<Uri>
    ): List<String> {

        val result: ArrayList<String?> = arrayListOf()


        for (imageUri in imagesUri) {
            runBlocking {
                val url = async(Dispatchers.Default) {
                    uploadPhotoToCloudAndWait(owner, itemId, imageUri)
                }
                result.add(url.await())
            }
        }

        return result.filterNotNull()
    }

    suspend fun uploadPhotoToCloudAndWait(owner: String, itemId: String, imageUri: Uri): String? =
        suspendCoroutine { cont ->
            uploadPhotoToCloud(
                owner,
                itemId,
                imageUri,
                object : OneParamCallback<String> {
                    override fun onComplete(url: String?) {
                        cont.resume(url)
                    }
                })
        }
}