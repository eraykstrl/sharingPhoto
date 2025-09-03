package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReachUsModelView(application: Application) : AndroidViewModel(application) {

    val problemsLiveData = MutableLiveData<Any>()
    private val firestore = FirebaseFirestore.getInstance()


    suspend fun saveProblems(user : FirebaseUser,selectedItem : String,otherProblems : String) : Result<Unit>
    {
        return try {

            val documentId = UUID.randomUUID().toString()
            val user_id = user.uid
            val email = user.email
            val hashMap = hashMapOf<String,String>(
                "problemId" to documentId,
                "userId" to user_id,
                "problemTopic" to selectedItem,
                "fullProblem" to otherProblems
            )

            firestore.collection("Problems").document(documentId).set(hashMap).await()
            problemsLiveData.postValue(1)
            Result.success(Unit)
        }

        catch (e : Exception)
        {
            val error = when(e)
            {
                is FirebaseFirestoreException -> when(e.code)
                {
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzniniz yok"
                    else -> "Sunucu hatası oluştu lütfen tekrar deneyiniz"
                }

                else -> "Beklenmeyen bir hata oluştu lütfen tekrar deneyiniz"
            }
            problemsLiveData.postValue(error)
            Result.failure(Exception(error))
        }



    }

}