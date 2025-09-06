package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.repository.FeedRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class PersonalPostViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val postList : MutableList<Post> = mutableListOf()
    val personalProfileLiveData = MutableLiveData<List<Post>>()
    val updatedPostLiveData = MutableLiveData<Post>()
    val errorLiveData = MutableLiveData<Any>()
    private val feedRepository = FeedRepository()
    val commentLiveData = MutableLiveData<Pair<String,List<Comment>>>()
    private var today = Date()


    suspend fun getPostByUser(userId : String?)
    {
        if(userId != null)
        {
            postList.clear()
            val posts = firestore.collection("Posts").whereEqualTo("user_id",userId).get().await().toObjects(
                Post::class.java)
            postList.addAll(posts)

            withContext(Dispatchers.Main) {
                personalProfileLiveData.value = postList
            }

        }
    }


    fun updatePost(postId : String)  {
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                val post = feedRepository.updatePost(postId)
                if (post != null)
                {
                    updatedPostLiveData.postValue(post)
                }
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                }
            }
        }

    }

    fun deletePost(post : Post)
    {
        viewModelScope.launch(Dispatchers.Main) {
            try
            {
                val postId = post.postId
                if (postId != null)
                {
                    feedRepository.deletePost(post)
                }
                else
                {
                    errorLiveData.postValue("Post bulunamıyor")
                }
                getPostByUser(post.user_id)
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {

                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Beklenmeyen bir hata oluştu ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                }
            }
        }
    }

    fun updateLike(post : Post,info : Int,userId : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                feedRepository.updateLike(post,info,userId)
                val postId = post.postId
                if(postId != null)
                {
                    updatePost(postId)
                }
            }
            catch (e : Exception)
            {
                val errorMessage = when(e)
                {
                    is FirebaseFirestoreException -> when(e.code)
                    {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }
                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage

                }
            }
        }


    }

    fun savePost(post : Post, userId : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                println("try içindeyiz")
                val postId = post.postId
                if (postId != null) {
                    feedRepository.savePost(post, userId)
                }
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                }
            }
        }

    }

    fun setComment(post: Post, comment: String)
    {
        val commentId = UUID.randomUUID().toString()
        val userId = post.user_id
        val username = post.username
        val postId = post.postId

        if (username != null && postId != null && userId != null) {
            val commentObject =
                Comment(commentId, userId, username, comment, postId, commentDate = today)
            viewModelScope.launch(Dispatchers.Main) {


                try {
                    val postRef = firestore.collection("Posts").document(postId)
                    postRef.update("comment", FieldValue.arrayUnion(commentObject)).await()
                    val result = feedRepository.updateComments(postId, commentObject)
                    withContext(Dispatchers.Main) {
                        commentLiveData.value = postId to result
                        updatePost(postId)
                    }

                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is FirebaseFirestoreException -> when (e.code) {
                            FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                            FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                            FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                            else -> "Bir hata oluştu: ${e.message}"
                        }

                        is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                        is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                        else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                    }
                    withContext(Dispatchers.Main) {
                        errorLiveData.value = errorMessage
                    }
                }
            }
        }
    }


        fun updateOwnerComment(comment : String,post : Post)
        {
            viewModelScope.launch(Dispatchers.Main) {
                try
                {
                    val postId = post.postId
                    if(postId != null)
                    {
                        feedRepository.updateOwnerComment(comment,postId)
                    }
                    else
                    {
                        withContext(Dispatchers.Main) {
                            errorLiveData.value = "Post bulunamıyor"
                        }
                    }
                }
                catch (e: Exception)
                {
                    val errorMessage = when (e) {
                        is FirebaseFirestoreException -> when (e.code) {
                            FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                            FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                            FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                            else -> "Bir hata oluştu: ${e.message}"
                        }

                        is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                        is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                        else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                    }
                    withContext(Dispatchers.Main) {
                        errorLiveData.value = errorMessage
                    }
                }
            }

        }


        fun modifyComment(newComment : String,comment: Comment)
        {
            viewModelScope.launch(Dispatchers.IO) {
                val postId = comment.postId
                try
                {
                    if (postId != null) {
                        feedRepository.modifyComment(newComment, comment)
                    }
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is FirebaseFirestoreException -> when (e.code) {
                            FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                            FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                            FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                            else -> "Bir hata oluştu: ${e.message}"
                        }

                        is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                        is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                        else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                    }
                    withContext(Dispatchers.Main) {
                        errorLiveData.value = errorMessage
                    }
                }
            }
        }

        fun deleteComment(comment : Comment)
        {
            viewModelScope.launch(Dispatchers.IO) {
                try
                {
                    val postId = comment.postId
                    if (postId != null) {
                        feedRepository.deleteComment(comment)
                        val result = feedRepository.updateComments(postId, comment)
                        withContext(Dispatchers.Main) {
                            commentLiveData.value = postId to result
                            updatePost(postId)
                        }
                    }
                }
                catch (e: Exception) {
                    val errorMessage = when (e) {
                        is FirebaseFirestoreException -> when (e.code) {
                            FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                            FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                            FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                            FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                            else -> "Bir hata oluştu: ${e.message}"
                        }

                        is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                        is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                        else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                    }
                    errorLiveData.postValue(errorMessage)
                }
            }

        }

    }

