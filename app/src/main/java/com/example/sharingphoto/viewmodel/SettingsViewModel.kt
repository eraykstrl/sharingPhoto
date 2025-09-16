package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application)  : AndroidViewModel(application){

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val logOutLiveData = MutableLiveData<Any>()
    val deleteLiveData = MutableLiveData<Boolean>()


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

    suspend fun deleteAllInfo(currentUserId : String)
    {

        try
        {
            firestore.collection("Users").document(currentUserId).delete().await()
            val posts = firestore.collection("Posts").whereEqualTo("user_id",currentUserId).get().await()
            val comments = firestore.collection("Comments").whereEqualTo("user_id",currentUserId).get().await()
            val problems = firestore.collection("Problems").whereEqualTo("user_id",currentUserId).get().await()
            for(doc in posts.documents)
            {
                doc.reference.delete().await()
            }

            for(doc in comments.documents)
            {
                doc.reference.delete().await()
            }

            for(doc in problems.documents)
            {
                doc.reference.delete().await()
            }
            withContext(Dispatchers.Main) {
                deleteLiveData.value = true
            }
        }

        catch (e : Exception)
        {
            deleteLiveData.value = false
        }

    }


}