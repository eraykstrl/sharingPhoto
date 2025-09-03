package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var today = Date()



    val information = MutableLiveData<Any>()


    suspend fun getUserByEmail(email : String) : String?
    {
        val query = firestore.collection("Users")
            .whereEqualTo("email",email)
            .get().await()

        if(query.isEmpty) return null

        val doc = query.documents[0]

        return doc.getString("user_id")
    }


    suspend fun setPosts(userComment : String,downloadUrl : String) : Result<Unit>
    {
        return try {
            val email = auth.currentUser?.email

            val user_id = getUserByEmail(email!!)
            val post_id = UUID.randomUUID().toString()
            val commentNumber = 0
            val likeNumber = 0


            val likedUser = emptyList<String?>()

            val snapshot =firestore.collection("Users").whereEqualTo("user_id",user_id)
                .get().await()

            val userName = if (!snapshot.isEmpty) snapshot.documents[0].getString("username") else null

            val sendNumber = 0
            val commentList: MutableList<Comment> = mutableListOf()


            val post = Post(
                post_id,
                userComment,
                commentList,
                downloadUrl,
                commentNumber,
                likeNumber,
                sendNumber,
                user_id,
                userName,
                likedUser,
                postDate = today, isLiked = false,
            )

            firestore.collection("Posts").document(post_id).set(post)
            withContext(Dispatchers.Main) {
                    information.value = 1
            }

            Result.success(Unit)
        }
        catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Bunu yapmaya izniniz yok"
                    else -> "Bilinmeyen Firestore hatası"
                }
                else -> e.localizedMessage ?: "Bilinmeyen hata"
            }

            withContext(Dispatchers.Main) {
                information.value = e.toString()

            }

            Result.failure(Exception(errorMessage))
        }

    }

}