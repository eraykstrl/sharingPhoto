package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class SettingsViewModel(application: Application)  : AndroidViewModel(application){

    val photoLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val logOutLiveData = MutableLiveData<Any>()
    val profilePhotoLiveData = MutableLiveData<Any>()
    val userFirstCharacterLiveData = MutableLiveData<Any>()
    val getDownloadUrlLiveData = MutableLiveData<Any>()



    suspend fun setProfilePhoto(authId : String,downloadUrl : String) : Result<Unit>
    {
        return try {
            firestore.collection("Users").document(authId).update("profilePhoto",downloadUrl).await()
            photoLiveData.postValue("success")

            Result.success(Unit)
        }

        catch(e : Exception)
        {

            val errorMessage = when(e)
            {
                is FirebaseFirestoreException -> "Yüklenirken bir hata oluştu"
                else -> "Bilinmeyen hata oluştu"
            }

            photoLiveData.postValue(errorMessage)

            Result.failure(Exception(errorMessage))
        }


    }

    suspend fun getDownloadUrl(user_id : String) : String
    {
        try
        {
            val user = firestore.collection("Users").document(user_id).get().await()
            val downloadUrl = user.getString("profilePhoto") ?: ""

            getDownloadUrlLiveData.postValue(1)
            return downloadUrl
        }

        catch (e : Exception)
        {
            val errorMessage = when(e) {
                is FirebaseFirestoreException -> "Bir hata oluştu"
                is FirebaseAuthException -> "Doğrulama hatası oluştu"
                else -> "Bilinmeyen hata oluştu"
            }

            getDownloadUrlLiveData.postValue(errorMessage)
            return ""
        }

    }

    suspend fun getCurrentFirstCharacter(user_id: String) : String
    {
        try
        {
            val user = firestore.collection("Users").document(user_id).get().await()
            val name = user.getString("name")
            val surname = user.getString("surname")
            val firstCharacterName = name?.firstOrNull() ?: ""
            val firstCharacterSurname = surname?.firstOrNull() ?: ""

            userFirstCharacterLiveData.postValue(1)
            return "$firstCharacterName $firstCharacterSurname"
        }
        catch (e : Exception)
        {
            val errorMessage = when(e) {

                is FirebaseFirestoreException -> "Bir hata oluştu"
                is FirebaseAuthException -> "Doğrulama hatası oluştu"
                else -> "Bilinmeyen hata oluştu"
            }

            userFirstCharacterLiveData.postValue(errorMessage)

            return ""
        }


    }

    suspend fun isThereProfilePhoto(user_id : String) : Boolean
    {

        try
        {
            val user = firestore.collection("Users").document(user_id).get().await()
            val downloadUrl = user.getString("profilePhoto")

            if(downloadUrl == "")
            {
                profilePhotoLiveData.postValue(false)
                return false
            }
            else
            {
                profilePhotoLiveData.postValue(true)
                return true
            }
        }

        catch (e : Exception)
        {
            val errorMessage = when(e) {
                is FirebaseFirestoreException -> "Bir hata oluştu"
                is FirebaseAuthException -> "Doğrulama hatası oluştu"
                else -> "Bilinmeyen hata oluştu"
            }

            profilePhotoLiveData.postValue(errorMessage)
            return false
        }

    }


    fun logOut()
    {
        auth.signOut()
        try {
            logOutLiveData.postValue(1)
        }
        catch (e : Exception)
        {
            val errorMessage = when(e) {

                is FirebaseAuthException -> "Kullanıcı alınırken hata oluştu"
                else ->  "Hata oluştu lütfen tekrar deneyiniz"
            }
            logOutLiveData.postValue(errorMessage)
        }
    }


}