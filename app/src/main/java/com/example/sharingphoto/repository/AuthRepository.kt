package com.example.sharingphoto.repository

import com.example.sharingphoto.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.UUID

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var error : String ?= null

    suspend fun signInWithEmailAndPassword(email : String,password : String) : FirebaseUser?
    {
        val authResult = auth.signInWithEmailAndPassword(email,password).await()
        return authResult.user
    }

    suspend fun signUpWithEmailAndPassword(name : String,surname :String,username : String,email : String,password : String) : FirebaseUser?
    {
        val authResult = auth.createUserWithEmailAndPassword(email,password).await()
        val userId = authResult.user?.uid
        val individualCode = UUID.randomUUID().toString()
        val hashedPassword = hashPassword(password+individualCode)
        val user = User(userId!!,name,surname,username,email,password = hashedPassword,individual = individualCode)
        firestore.collection("Users").document(userId).set(user).await()
        return authResult.user
    }

    private fun hashPassword(password : String) : String
    {
        val byteArray = password.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(byteArray)
        return digest.joinToString("") {"%02x" .format(it)}

    }
}