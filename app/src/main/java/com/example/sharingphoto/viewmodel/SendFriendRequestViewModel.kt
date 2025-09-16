package com.example.sharingphoto.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.model.User
import com.example.sharingphoto.repository.UserRepository
import com.google.firebase.Firebase
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

class SendFriendRequestViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val userLiveData = MutableLiveData<List<User>>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid
    val errorLiveData = MutableLiveData<Any>()

    fun getAllUser()
    {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getAllUser(userId = currentUserId!!).collect {
                user ->
                withContext(Dispatchers.Main)  {
                    userLiveData.value = user
                }
            }
        }
    }


    suspend fun setFollower(receiveId : String?)
    {
        try
        {
            if(currentUserId != null && receiveId != null)
            {
                userRepository.sendFollowingRequest(currentUserId,receiveId)
                getAllUser()
            }
            else
            {
                withContext(Dispatchers.Main) {
                    errorLiveData.value = "Kullanıcı bilgisi alınamıyor"
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

}