package com.example.sharingphoto.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.sharingphoto.model.User
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.checkerframework.checker.units.qual.Current

class AccountSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    val loadingLiveData = MutableLiveData<Boolean>()
    val errorLiveData = MutableLiveData<Any>()
    val userLiveData = MutableLiveData<User>()


    suspend fun getUserInfo(currentUserId : String?)
    {
        withContext(Dispatchers.Main) {
            errorLiveData.value = false
            loadingLiveData.value = true
        }

        if(currentUserId != null)
        {
            try
            {
                val user = firestore.collection("Users").document(currentUserId).get().await()
                val name = user.getString("name")
                val surname = user.getString("surname")
                val username = user.getString("username")
                val aboutUser = user.getString("aboutUser")
                val isPrivate = user.getBoolean("isPrivate")
                val userObject = User(name = name,surname = surname, username = username, aboutUser = aboutUser, isPrivate = isPrivate!!)
                withContext(Dispatchers.Main) {
                    userLiveData.value = userObject
                    loadingLiveData.value = false
                }
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                    loadingLiveData.value = false
                }
                null
            }
        }
    }

    suspend fun changeNameOrSurname(currentUserId : String?,name : String?,surname : String?)
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true
        }
        if(currentUserId != null)
        {
            try
            {
                if(name != null && surname != null && !name.isEmpty() && !surname.isEmpty())
                {
                    firestore.collection("Users").document(currentUserId).update("name",name).await()
                    firestore.collection("Users").document(currentUserId).update("surname",surname).await()
                    withContext(Dispatchers.Main) {
                        loadingLiveData.value = false
                    }
                    getUserInfo(currentUserId)
                }
                else if(name != null && !name.isEmpty())
                {
                    firestore.collection("Users").document(currentUserId).update("name",name).await()
                    withContext(Dispatchers.Main) {
                        loadingLiveData.value = false
                    }
                    getUserInfo(currentUserId)

                }
                else if(surname != null && !surname.isEmpty())
                {
                    firestore.collection("Users").document(currentUserId).update("surname",surname).await()
                    withContext(Dispatchers.Main) {
                        loadingLiveData.value = false
                    }
                    getUserInfo(currentUserId)
                }
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                    loadingLiveData.value = false
                }
            }

        }
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = "Kullanıcı alınamıyor lütfen tekrar giriş yapınız"
                loadingLiveData.value = false
            }
        }
    }

    suspend fun changeUsername(currentUserId: String?,username : String)
    {
        withContext(Dispatchers.Main) {
            errorLiveData.value = false
            loadingLiveData.value = true
        }

        if(currentUserId != null)
        {
            try
            {
                val batch = firestore.batch()
                val posts = firestore.collection("Posts").whereEqualTo("user_id",currentUserId).get().await()
                val comments = firestore.collection("Comments").whereEqualTo("user_id",currentUserId).get().await()
                firestore.collection("Users").document(currentUserId).update("username",username).await()
                for(doc in posts.documents)
                {
                    doc.reference.update("username",username).await()
                    val comments = doc.get("comment") as? List<Map<String?, Any?>> ?: emptyList()
                    val updatedComments = comments.map {
                        comment ->
                        if(comment["user_id"] == currentUserId)
                        {
                            comment.toMutableMap().apply { put("username",username) }
                        }
                        else comment
                    }

                    batch.update(doc.reference,"comment",updatedComments)
                }
                batch.commit().await()

                for(doc in comments.documents)
                {
                    doc.reference.update("username",username).await()
                }
                println("geçti ikisini de")
                withContext(Dispatchers.Main) {
                    errorLiveData.value = false
                    loadingLiveData.value = false
                }
                getUserInfo(currentUserId)
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                    loadingLiveData.value = false
                }
            }
        }
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = "Kullanıcı alınamıyor lütfen tekrar giriş yapınız"
                loadingLiveData.value = false
            }
        }
    }

    suspend fun changeAboutUser(currentUserId: String?,aboutUser : String?)
    {
        withContext(Dispatchers.Main) {
            errorLiveData.value = false
            loadingLiveData.value = true
        }

        if(currentUserId != null)
        {
            try
            {
                firestore.collection("Users").document(currentUserId).update("aboutUser",aboutUser).await()
                withContext(Dispatchers.Main) {
                    errorLiveData.value = false
                    loadingLiveData.value = false
                }
                getUserInfo(currentUserId)
            }
            catch (e: Exception)
            {
                val errorMessage = when (e) {
                    is FirebaseFirestoreException -> when (e.code) {
                        FirebaseFirestoreException.Code.NOT_FOUND -> "Sayfa bulunamadı"
                        FirebaseFirestoreException.Code.CANCELLED -> "İşlem iptal edildi"
                        FirebaseFirestoreException.Code.UNKNOWN -> "Bilinmeyen hata oluştu"
                        FirebaseFirestoreException.Code.PERMISSION_DENIED -> "İzin yok"
                        FirebaseFirestoreException.Code.UNAVAILABLE -> "Sunucu kullanılamıyor"
                        else -> "Bir hata oluştu: ${e.message}"
                    }

                    is FirebaseNetworkException -> "Network hatası: Lütfen internet bağlantınızı kontrol edin"
                    is FirebaseException -> "Sunucuda bir problem oluştu, lütfen tekrar deneyiniz"
                    else -> "Bilinmeyen bir hata oluştu: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    errorLiveData.value = errorMessage
                    loadingLiveData.value = false
                }
            }
        }
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = "Kullanıcı alınamıyor lütfen tekrar giriş yapınız"
                loadingLiveData.value = false
            }
        }
    }


    suspend fun changePrivacy(currentUserId: String?,info : Int)
    {
        if(currentUserId != null)
        {
            if(info == 0)
            {

                firestore.collection("Users").document(currentUserId).update("isPrivate",false).await()
                val posts = firestore.collection("Posts").orderBy("user_id").whereEqualTo("user_id",currentUserId).get().await()
                for(doc in posts.documents)
                {
                    doc.reference.update("isPrivate",false)
                }
                getUserInfo(currentUserId)

            }
            else if(info == 1)
            {
                firestore.collection("Users").document(currentUserId).update("isPrivate",true).await()
                val posts = firestore.collection("Posts").orderBy("user_id").whereEqualTo("user_id",currentUserId).get().await()
                for(doc in posts)
                {
                    doc.reference.update("isPrivate",true)
                }
                getUserInfo(currentUserId)
            }
        }
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = "Kullanıcı bulunamadı lütfen tekrar giriş yapınız."
            }
        }

    }
}