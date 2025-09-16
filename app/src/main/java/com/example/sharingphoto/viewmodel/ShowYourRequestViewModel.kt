package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.User
import com.example.sharingphoto.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowYourRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val userLiveData = MutableLiveData<List<User>>()
    val errorLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getFollowingRequest(currentUserId : String)
    {
        try
        {
            userRepository.getFollowingRequest(currentUserId).collect {
                user ->
                withContext(Dispatchers.Main) {
                    userLiveData.value = user
                }
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

    suspend fun removeUserFromFollowingRequest(currentUserId : String,receiveId : String)
    {
        firestore.collection("Users").document(currentUserId).update("followingRequest", FieldValue.arrayRemove(receiveId)).await()
        firestore.collection("Users").document(receiveId).update("followerRequest", FieldValue.arrayRemove(currentUserId)).await()
        getFollowingRequest(currentUserId)
    }
}