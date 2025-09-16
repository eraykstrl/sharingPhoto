package com.example.sharingphoto.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.model.User
import com.example.sharingphoto.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FriendRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val userLiveData = MutableLiveData<List<User>>()
    val errorLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()

    fun getFollowerRequests(userId : String)
    {
        println("get follower cagrildi")
        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                userRepository.getFollowerRequests(currentUserId = userId).collect {
                    userList ->
                    Log.d("iceri girildi repository cagrildi","cagrildi")
                    if(userList.isEmpty())
                    {
                        Log.d("userlist bos","bos")
                    }
                    else
                    {
                        Log.d("userList dolu","dolu")
                    }
                    withContext(Dispatchers.Main) {
                        userLiveData.value = userList
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


    suspend fun acceptFollowerRequest(currentUserId : String,requestId : String)
    {
        val currentUser = firestore.collection("Users").document(currentUserId)
        currentUser.update("followerRequest", FieldValue.arrayRemove(requestId)).await()
        currentUser.update("follower", FieldValue.arrayUnion(requestId)).await()
        val requestedUser = firestore.collection("Users").document(requestId)
        requestedUser.update("followingRequest", FieldValue.arrayRemove(currentUserId))
        requestedUser.update("following", FieldValue.arrayUnion(currentUserId))
        getFollowerRequests(currentUserId)
    }

    suspend fun rejectFollowerRequest(currentUserId : String,requestId : String)
    {
        val currentUser = firestore.collection("Users").document(currentUserId)
        currentUser.update("followerRequest", FieldValue.arrayRemove(requestId)).await()
        val requestedUser = firestore.collection("Users").document(requestId)
        requestedUser.update("followingRequest", FieldValue.arrayRemove(currentUserId))
        getFollowerRequests(currentUserId)
    }


}