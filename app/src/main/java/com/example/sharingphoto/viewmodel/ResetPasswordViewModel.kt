package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class ResetPasswordViewModel(application: Application) : AndroidViewModel(application) {

    val resetPasswordLiveData = MutableLiveData<Any>()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun resetPassword(oldPassword : String,newPassword : String)
    {
        val user = auth.currentUser
        val user_id = user?.uid
        if(user_id != null)
        {
            val users = firestore.collection("Users").document(user_id)
            users.get().addOnSuccessListener { result ->
                val password = result.getString("password")
                val individual = result.getString("individual")

                val oldHashedPassword = hashPassword(oldPassword + individual)
                val hashedNewPassword = hashPassword(newPassword + individual)

                if (oldHashedPassword == hashedNewPassword)
                {
                    resetPasswordLiveData.postValue(-1)
                }
                else
                {

                    if (password == oldHashedPassword)
                    {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    users.update("password", hashedNewPassword).addOnSuccessListener {
                                        resetPasswordLiveData.postValue(1)

                                    }
                                        .addOnFailureListener {
                                            resetPasswordLiveData.postValue(0)
                                        }
                                } else {
                                    resetPasswordLiveData.postValue(0)
                                }
                            }
                    }
            }

            }
        }
    }



    private fun hashPassword(password : String) : String
    {
        val byte = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val result = md.digest(byte)

        return result.joinToString("") {"%02x".format(it)}
    }

}