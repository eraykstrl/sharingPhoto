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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInViewModel(application: Application) : AndroidViewModel(application) {


    val signInLiveData = MutableLiveData<FirebaseUser?>()
    val errorLiveData = MutableLiveData<Any>()
    private val authRepository = AuthRepository()

    fun signIn(email : String,password : String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val user = authRepository.signInWithEmailAndPassword(email, password)
                withContext(Dispatchers.Main) {
                    signInLiveData.value = user
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "Böyle bir kullanıcı bulunamadı"
                    is FirebaseAuthInvalidCredentialsException -> "Email veya şifre hatalı"
                    is FirebaseAuthUserCollisionException -> "Bu email zaten kayıtlı"
                    is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf"
                    is FirebaseAuthException -> "Giriş hatası lütfen tekrar deneyiniz: ${e.errorCode}"
                    else -> "Bilinmeyen bir hata oluştu: ${e.localizedMessage}"
                }

                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                }
            }
        }

    }

}