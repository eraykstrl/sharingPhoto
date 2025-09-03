package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    val notificationLiveData = MutableLiveData<Any>()



    suspend fun changeNotificationSettings(user : FirebaseUser ,notificationMap : MutableMap<String, Boolean>) : Result<Unit>
    {
        return  try
        {

            val userId = user.uid

            val user = firestore.collection("Users").document(userId)

            notificationMap.forEach {
                user.update(it.key,it.value).await()
            }
            notificationLiveData.postValue(1)
            Result.success(Unit)
        }

        catch (e : FirebaseFirestoreException)
        {
            val errorMessage = when(e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    "Bu kullanıcı, bildirim ayarlarını güncellemek için yetkili değil."
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    "Güncellenmeye çalışılan kullanıcı verisi Firestore’da mevcut değil."
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    "Firestore hizmetine şu anda ulaşılamıyor. Lütfen internet bağlantınızı kontrol edin."
                FirebaseFirestoreException.Code.ABORTED ->
                    "İşlem Firestore tarafından iptal edildi. Lütfen tekrar deneyin."
                else ->
                    "Firestore güncelleme sırasında bilinmeyen bir hata oluştu: ${e.message}"
            }

            notificationLiveData.postValue(errorMessage)

            Result.failure(Exception(errorMessage))
        }

    }

}