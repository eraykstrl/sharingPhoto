package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class UserViewModel(application: Application) : AndroidViewModel(application)   {


    private val authRepository = AuthRepository()
    val signUpLiveData = MutableLiveData<FirebaseUser?>()
    val errorLiveData = MutableLiveData<Any>()


    fun signUp(name : String,surname :String,username : String,email : String,password : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val result = authRepository.signUpWithEmailAndPassword(
                    name,
                    surname,
                    username,
                    email,
                    password
                )
                withContext(Dispatchers.Main) {
                    signUpLiveData.value = result
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Böyle bir kullanıcı bulunamadı"
                    is FirebaseAuthInvalidCredentialsException -> "Email veya şifre hatalı"
                    is FirebaseAuthUserCollisionException -> "Bu email zaten kayıtlı"
                    is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf"
                    is FirebaseAuthException -> "Giriş hatası lütfen tekrar deneyiniz: ${e.errorCode}"
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucuya ulaşılamıyor lütfen daha sonra tekrar deneyiniz"
                        FirebaseFirestoreException.Code.ALREADY_EXISTS -> "Zaten böyle bir hesap var"
                        FirebaseFirestoreException.Code.INTERNAL -> "Sunucu içi bir hata oluştu lütfen tekrar deneyiniz"
                        else -> "Bilinmeyen bir sunucu hatası oluştu lütfen tekrar deneyiniz"
                    }

                    else -> "Bilinmeyen bir hata oluştu: ${e.localizedMessage}"
                }

                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage

                }
            }
        }

    }

}