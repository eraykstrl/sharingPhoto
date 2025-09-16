package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.PostType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.serializerOrNull
import java.util.Date
import java.util.UUID

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var today = Date()

    val information = MutableLiveData<Any>()
    val loadingLiveData = MutableLiveData<Boolean>()
    val errorLiveData = MutableLiveData<Any>()

    suspend fun getName(userId : String?) : String?
    {
        withContext(Dispatchers.Main) {
            errorLiveData.value = false

        }
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        if(userId != null)
        {
            try
            {
                val user = firestore.collection("Users").document(userId).get().await()
                val name = user.getString("name")
                val last = name?.replaceFirstChar { it.uppercase() }

                withContext(Dispatchers.Main) {
                    loadingLiveData.value = false

                }
                return last

            }
            catch (e : Exception)
            {
                val errorMessage = when(e) {
                    is FirebaseFirestoreException -> when(e.code)
                    {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Bulunamadı"
                        else -> "Beklenmeyen hata oluştu"
                    }
                    else -> "Beklenmeyen hata"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                }
                return null
            }

        }
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = false

            }
            return null
        }
    }

    suspend fun setPostsByPhoto(user_id : String,userComment : String,downloadUrl : String) : Result<Unit>
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        return try {
            val email = auth.currentUser?.email

            val post_id = UUID.randomUUID().toString()
            val commentNumber = 0
            val likeNumber = 0
            val user = firestore.collection("Users").document(user_id!!).get().await()
            val userName = user.getString("username")


            val sendNumber = 0
            val commentList: MutableList<Comment> = mutableListOf()

            val post = Post(
                postId = post_id,
                userComment = userComment,
                comment = commentList,
                downloadUrl = downloadUrl,
                videoUrl = null,
                gifUrl = null,
                fileUrl = null,
                voiceUrl = null,
                likeCounter = likeNumber,
                commentCounter = commentNumber,
                sendNumber = sendNumber,
                user_id = user_id,
                username = userName,
                likes = emptyList(),
                postDate = today,
                isLiked = false,
                postType = PostType.IMAGE
            )


            firestore.collection("Posts").document(post_id).set(post)
            withContext(Dispatchers.Main) {
                    information.value = 1
                    loadingLiveData.value = false
            }

            Result.success(Unit)
        }
        catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    else -> "Bilinmeyen Firestore hatası"
                }
                else -> e.localizedMessage ?: "Bilinmeyen hata"
            }

            withContext(Dispatchers.Main) {
                errorLiveData.value = e.toString()

            }

            Result.failure(Exception(errorMessage))
        }

    }


    suspend fun setPostsByVideo(user_id : String,userComment : String,videoUrl : String) : Result<Unit>
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        return try {
            val email = auth.currentUser?.email

            val post_id = UUID.randomUUID().toString()
            val commentNumber = 0
            val likeNumber = 0
            val user = firestore.collection("Users").document(user_id!!).get().await()
            val userName = user.getString("username")


            val sendNumber = 0
            val commentList: MutableList<Comment> = mutableListOf()

            val post = Post(
                postId = post_id,
                userComment = userComment,
                comment = commentList,
                downloadUrl = null,
                videoUrl = videoUrl,
                gifUrl = null,
                fileUrl = null,
                voiceUrl = null,
                likeCounter = likeNumber,
                commentCounter = commentNumber,
                sendNumber = sendNumber,
                user_id = user_id,
                username = userName,
                likes = emptyList(),
                postDate = today,
                isLiked = false,
                postType = PostType.VIDEO
            )

            println("post olusturdu video url ${videoUrl}")
            firestore.collection("Posts").document(post_id).set(post)
            println("set edildi")
            withContext(Dispatchers.Main) {
                information.value = 1
                loadingLiveData.value = false
            }

            Result.success(Unit)
        }
        catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    else -> "Bilinmeyen Firestore hatası"
                }
                else -> e.localizedMessage ?: "Bilinmeyen hata"
            }

            withContext(Dispatchers.Main) {
                errorLiveData.value = e.toString()

            }

            Result.failure(Exception(errorMessage))
        }

    }

    suspend fun setPostsByFile(user_id : String,userComment : String,fileUrl : String,fileName : String) : Result<Unit>
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        return try {

            val post_id = UUID.randomUUID().toString()
            val commentNumber = 0
            val likeNumber = 0
            val user = firestore.collection("Users").document(user_id!!).get().await()
            val userName = user.getString("username")


            val sendNumber = 0
            val commentList: MutableList<Comment> = mutableListOf()

            val post = Post(
                postId = post_id,
                userComment = userComment,
                comment = commentList,
                downloadUrl = null,
                videoUrl = null,
                gifUrl = null,
                fileUrl = fileUrl,
                voiceUrl = null,
                likeCounter = likeNumber,
                commentCounter = commentNumber,
                sendNumber = sendNumber,
                user_id = user_id,
                username = userName,
                likes = emptyList(),
                postDate = today,
                isLiked = false,
                postType = PostType.FILE,
                postName = fileName,
            )


            firestore.collection("Posts").document(post_id).set(post)
            withContext(Dispatchers.Main) {
                information.value = 1
                loadingLiveData.value = false
            }

            Result.success(Unit)
        }
        catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    else -> "Bilinmeyen Firestore hatası"
                }
                else -> e.localizedMessage ?: "Bilinmeyen hata"
            }

            withContext(Dispatchers.Main) {
                errorLiveData.value = e.toString()

            }

            Result.failure(Exception(errorMessage))
        }

    }

    suspend fun setPosts(user_id : String,userComment : String) : Result<Unit>
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        return try {
            val email = auth.currentUser?.email

            val post_id = UUID.randomUUID().toString()
            val commentNumber = 0
            val likeNumber = 0
            val user = firestore.collection("Users").document(user_id!!).get().await()
            val userName = user.getString("username")


            val sendNumber = 0
            val commentList: MutableList<Comment> = mutableListOf()

            val post = Post(
                postId = post_id,
                userComment = userComment,
                comment = commentList,
                downloadUrl = null,
                videoUrl = null,
                gifUrl = null,
                fileUrl = null,
                voiceUrl = null,
                likeCounter = likeNumber,
                commentCounter = commentNumber,
                sendNumber = sendNumber,
                user_id = user_id,
                username = userName,
                likes = emptyList(),
                postDate = today,
                isLiked = false,
                postType = PostType.TEXT
            )


            firestore.collection("Posts").document(post_id).set(post)
            withContext(Dispatchers.Main) {
                information.value = 1
                loadingLiveData.value = false
            }

            Result.success(Unit)
        }
        catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    else -> "Bilinmeyen Firestore hatası"
                }
                else -> e.localizedMessage ?: "Bilinmeyen hata"
            }

            withContext(Dispatchers.Main) {
                errorLiveData.value = e.toString()

            }

            Result.failure(Exception(errorMessage))
        }

    }

}