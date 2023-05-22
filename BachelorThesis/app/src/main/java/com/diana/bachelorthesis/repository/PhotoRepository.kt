package com.diana.bachelorthesis.repository

import android.net.Uri
import android.util.Log
import com.diana.bachelorthesis.model.User
import com.diana.bachelorthesis.utils.ListParamCallback
import com.diana.bachelorthesis.utils.NoParamCallback
import com.diana.bachelorthesis.utils.OneParamCallback
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*
import javax.inject.Singleton

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
@Singleton
class PhotoRepository {
    private val TAG: String = PhotoRepository::class.java.name

    var defaultProfilePhotoUri: Uri =
        Uri.parse("android.resource://com.diana.bachelorthesis/drawable/default_profile_picture")
    val unavailablePhotoUrl =
        "https://firebasestorage.googleapis.com/v0/b/bachelorthesis-3092d.appspot.com/o/unavailable.jpg?alt=media&token=bbf854a6-162d-47e4-9006-7d8fe5ef083e"

    // for cloud
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageReference: StorageReference = firebaseStorage.reference

    companion object {
        @Volatile
        private var instance: PhotoRepository? = null
        fun getInstance() = instance ?: PhotoRepository().also { instance = it }
    }

    private fun uploadItemPhoto(
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
                        callback.onError(task.exception)
                    }

                } else {
                    Log.w(
                        TAG,
                        "Photo with uri $imageUri failed to upload!"
                    )
                    callback.onError(task.exception)
                }
            }
        }
    }
     fun uploadItemPhotos(
        owner: String,
        itemId: String,
        imagesUri: List<Uri>,
        currentImageNo: Int,
        imagesUrl: ArrayList<String>,
        callback: ListParamCallback<String>
    ) {
        // recursively add photos
        val itemPhotosReference = storageReference.child(owner).child(itemId)
        val currentImageUri = imagesUri[currentImageNo]

        currentImageUri.let {
            val imageName = UUID.randomUUID().toString()

            itemPhotosReference.child(imageName).putFile(it).addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    itemPhotosReference.child(imageName).downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d(
                            TAG,
                            "Successfully uploaded image with url $imageUrl"
                        )
                        imagesUrl.add(imageUrl)
                        if (currentImageNo == imagesUri.size - 1) {
                            callback.onComplete(imagesUrl)
                        } else {
                            uploadItemPhotos(owner, itemId, imagesUri, currentImageNo + 1, imagesUrl, callback)
                        }

                    }.addOnFailureListener {
                        Log.w(
                            TAG,
                            "\"Couldn't get url for photo with uri $currentImageUri"
                        )
                        callback.onError(task.exception)
                    }

                } else {
                    Log.w(
                        TAG,
                        "Photo with uri $currentImageUri failed to upload!"
                    )
                    callback.onError(task.exception)
                }
            }
        }
    }
    suspend fun uploadPhotos(
        owner: String,
        itemId: String, imagesUri: List<Uri>
    ): List<String> {

        val result: ArrayList<String?> = arrayListOf()

        for (imageUri in imagesUri) {
            runBlocking {
                val url = async(Dispatchers.Default) {
                    uploadPhotoAndWait(owner, itemId, imageUri)
                }
                result.add(url.await())
            }
        }

        return result.filterNotNull()
    }

    fun uploadMessagePhoto(chatId: String, photo:Uri, callback: OneParamCallback<String>) {
        val imageReference = storageReference
            .child(chatId)
            .child(UUID.randomUUID().toString())

       imageReference
            .putFile(photo)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageUrl = url.toString()
                    Log.d(
                        TAG,
                        "Successfully uploaded photo for for chat $chatId"
                    )
                    callback.onComplete(imageUrl)

                }.addOnFailureListener {
                    Log.w(
                        TAG,
                        "\"Couldn't get url for photo in chat $chatId"
                    )
                    callback.onError(task.exception)
                }
            } else {
                Log.w(
                    TAG,
                    "Photo for chat $chatId failed to upload!"
                )
                callback.onError(task.exception)
            }
        }
    }

    private suspend fun uploadPhotoAndWait(owner: String, itemId: String, imageUri: Uri): String? =
        suspendCoroutine { cont ->
            uploadItemPhoto(
                owner,
                itemId,
                imageUri,
                object : OneParamCallback<String> {
                    override fun onComplete(value: String?) {
                        cont.resume(value)
                    }

                    override fun onError(e: Exception?) {
                        Log.w(TAG, "Upload photo failed")
                        if (e != null) {
                            Log.w(TAG, "${e.message}")
                        }
                        cont.resume(null)
                    }
                })
        }


    fun uploadProfilePhoto(
        userEmail: String,
        photoUri: Uri?,
        callback: OneParamCallback<String>,
        retry: Boolean = true
    ) {
        // FIXME not working when uploading Google's user's photo after sign up
        val choseDefaultPhoto = (photoUri == null)
        val uploadPhoto = photoUri ?: defaultProfilePhotoUri
        val imageReference = storageReference
            .child(userEmail)
            .child("profile_photo.png")

        imageReference
            .putFile(uploadPhoto)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageReference.downloadUrl.addOnSuccessListener { url ->
                        val imageUrl = url.toString()
                        Log.d(
                            TAG,
                            "Successfully uploaded profile photo for user $userEmail"
                        )
                        callback.onComplete(imageUrl)

                    }.addOnFailureListener {
                        Log.w(
                            TAG,
                            "\"Couldn't get url for profile photo for user $userEmail"
                        )
                        if (!choseDefaultPhoto && retry) {
                            Log.d(TAG, "Trying to upload the default picture for user $userEmail")
                            uploadProfilePhoto(userEmail, null, callback)
                        } else {
                            callback.onError(task.exception)
                        }
                    }

                } else {
                    Log.w(
                        TAG,
                        "Profile photo for user $userEmail failed to upload!"
                    )
                    if (!choseDefaultPhoto) {
                        Log.d(TAG, "Trying to upload the default picture for user $userEmail")
                        uploadProfilePhoto(userEmail, null, callback)
                    } else {
                        callback.onError(task.exception)
                    }
                }
            }
    }

    fun deleteItemPhotos(ownerEmail: String, itemId: String) {
        val itemPhotosReference = storageReference.child(ownerEmail).child(itemId)
        itemPhotosReference.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                item.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Deleted image for item id: $itemId")
                    } else {
                        Log.d(TAG, "Failed to delete image for item id: $itemId")
                    }
                }
            }
        }

//            delete().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    Log.d(TAG, "Deleted item's photos with id $itemId.")
//                    callback.onComplete()
//                } else {
//                    Log.w(
//                        TAG,
//                        "Error while deleting item's photos with id $itemId, see message below"
//                    )
//                    if (task.exception != null) {
//                        Log.w(TAG, task.exception!!.message.toString())
//                    }
//                    callback.onError(task.exception)
//                }
//            }
//        }
    }
}