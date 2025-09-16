package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.User
import com.example.sharingphoto.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserFollowingViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository()
    val userListLiveData = MutableLiveData<List<Pair<User, Int>>>()
    val errorLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getFollowingById(currentUserId : String,profileId : String)
    {
        try
        {
            userRepository.getUserFollowingById(currentUserId,profileId).collect {
                list ->
                withContext(Dispatchers.Main) {
                    userListLiveData.value = list
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

    suspend fun userOperations(currentUserId: String?,profileId : String?,operationInfo : Int)
    {
        if(currentUserId != null && profileId != null)
        {
            if(operationInfo == 0)
            {
                firestore.collection("Users").document(currentUserId).update("followingRequest",
                    FieldValue.arrayUnion(profileId)).await()
                firestore.collection("Users").document(profileId).update("followerRequest", FieldValue.arrayUnion(currentUserId)).await()
                getFollowingById(currentUserId,profileId)
            }
            else if(operationInfo == 1)
            {

                firestore.collection("Users").document(currentUserId).update("followingRequest", FieldValue.arrayRemove(currentUserId)).await()
                firestore.collection("Users").document(profileId).update("followerRequest", FieldValue.arrayRemove(currentUserId)).await()
                getFollowingById(currentUserId,profileId)

            }

            else if(operationInfo == 2)
            {
                println("operation info 2 viewmodel")
                firestore.collection("Users").document(currentUserId).update("following",
                    FieldValue.arrayRemove(profileId)).await()
                firestore.collection("Users").document(profileId).update("follower", FieldValue.arrayRemove(currentUserId)).await()
                getFollowingById(currentUserId,profileId)

            }
        }
    }
}