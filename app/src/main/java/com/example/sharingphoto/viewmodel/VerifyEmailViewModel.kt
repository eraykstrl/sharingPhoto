package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class VerifyEmailViewModel(application: Application) : AndroidViewModel(application) {

    val verifyLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()


    suspend fun sendEmailVerificationCode(currentUser : FirebaseUser) : Result<Unit>
    {
        val user_id = currentUser.uid

        return try {
            currentUser.sendEmailVerification().await()

            try {
                firestore.collection("Users").document(user_id).update("isVerify",true).await()

            }
            catch (e : Exception)
            {
                val errorMessage = when(e) {
                    is FirebaseFirestoreException -> when(e.code)
                    {
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Kullanıcı bulunamadı"
                        else -> "Hata oluştu"
                    }
                    is FirebaseAuthUserCollisionException -> "Doğrulama adresi zaten gönderildi"

                    else -> "Bilinmeyen hata oluştu"
                }
                verifyLiveData.postValue(errorMessage)

            }


            verifyLiveData.postValue(1)
            Result.success(Unit)
        }
        catch (e : Exception)
        {
            val errorMessage = when(e) {
                is FirebaseFirestoreException -> when(e.code)
                {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    FirebaseFirestoreException.Code.NOT_FOUND -> "Kullanıcı bulunamadı"
                    else -> "Hata oluştu"
                }

                is FirebaseAuthUserCollisionException -> "Doğrulama kodu zaten gönderildi"

                else -> "Beklenmeyen hata oluştu"
            }

            verifyLiveData.postValue(errorMessage)
            Result.failure(Exception(errorMessage))
        }
    }

}