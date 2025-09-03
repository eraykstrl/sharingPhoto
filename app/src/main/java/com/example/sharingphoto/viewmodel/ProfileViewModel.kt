package com.example.sharingphoto.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val postList : ArrayList<Post> = arrayListOf()
    val postLiveData = MutableLiveData<List<Post>>()
    val errorLiveData = MutableLiveData<Any>()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val userLiveData = MutableLiveData<User>()

    suspend fun getPosts(userId : String)
    {
        try
        {
            val posts = firestore.collection("Posts").whereEqualTo("user_id",userId).get().await().toObjects(
                Post::class.java)
            postList.clear()
            postList.addAll(posts)
            withContext(Dispatchers.Main) {
                postLiveData.value = postList
            }
        }

        catch(e : Exception)
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


    suspend fun getUser(userId : String)
    {
        val user = firestore.collection("Users").document(userId).get().await().toObject(User::class.java)

        withContext(Dispatchers.Main) {
            userLiveData.value = user!!
        }
    }


    suspend fun setProfilePhoto(selectedImage : Uri?)
    {
        try
        {
            val userId = auth.currentUser?.uid
            val reference = storage.reference
            val personalReference = reference.child(userId!!)

            if(selectedImage != null)
            {
                personalReference.putFile(selectedImage).await()
                val downloadUrl = personalReference.downloadUrl.await().toString()
                firestore.collection("Users").document(userId).update("profilePhoto",downloadUrl).await()

            }
            getUser(userId)
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
            null
        }

    }

}