package com.example.sharingphoto.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.adapter.PostAdapter
import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.User
import com.example.sharingphoto.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.model.Document
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    val postLiveData = MutableLiveData<List<Post>>()
    val errorLiveData = MutableLiveData<Any>()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val userLiveData = MutableLiveData<User>()
    private val userRepository = UserRepository()
    val followingRequestLiveData = MutableLiveData<Pair<User,Int>>()
    val loadingLiveData = MutableLiveData<Boolean>()


    suspend fun getPostsFromInternet(userId : String)
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true

        }
        try
        {
            userRepository.getPostsByUserId(userId).collect {
                list ->
                withContext(Dispatchers.Main) {
                    postLiveData.value = list
                    loadingLiveData.value = false
                }
                getFollowingInfo(userId)
                getUser(userId)

            }

        }

        catch(e : Exception)
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
                loadingLiveData.value = false
                errorLiveData.value = errorMessage
            }
        }
    }

    suspend fun getUser(userId : String)
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true

        }
        val user = firestore.collection("Users").document(userId).get().await().toObject(User::class.java)

        withContext(Dispatchers.Main) {
            userLiveData.value = user!!
            loadingLiveData.value = false
        }
    }


    suspend fun setProfilePhoto(selectedImage : Uri?)
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true

        }
        try
        {
            val userId = auth.currentUser?.uid
            val reference = storage.reference
            val personalReference = reference.child(userId!!)

            if(selectedImage != null)
            {
                personalReference.putFile(selectedImage).await()
                val downloadUrl = personalReference.downloadUrl.await().toString()
                firestore.collection("Users").document(userId).update("profilePhoto",downloadUrl).await()

            }
            getUser(userId)
            withContext(Dispatchers.Main) {
                loadingLiveData.value = true

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
                loadingLiveData.value = false
                errorLiveData.value = errorMessage
            }
            null
        }

    }


    fun followingRequest(receiveId : String?)
    {
        val currentUserId = auth.uid
        if(currentUserId != null && receiveId != null)
        {
            viewModelScope.launch(Dispatchers.IO) {
                try
                {
                    userRepository.sendFollowingRequest(currentUserId,receiveId)
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
                    }
                    null
                }
            }
        }
        else
        {
            errorLiveData.value = "Kullanıcı bilgisi alınamıyor lütfen daha sonra tekrar deneyiniz"
        }
    }


    suspend fun getFollowingInfo(receiveId : String?)
    {
        withContext(Dispatchers.Main) {
            loadingLiveData.value = true

        }
        val currentUserId = auth.currentUser?.uid
        val userObject = firestore.collection("Users").document(receiveId!!).get().await().toObject(User::class.java)
        println(userObject?.username)
        println(userObject?.isPrivate)
        if(currentUserId != null)
        {
                viewModelScope.launch(Dispatchers.IO) {
                    try
                    {
                        val result = userRepository.getFollowingInfo(currentUserId,receiveId)
                        withContext(Dispatchers.Main) {
                            followingRequestLiveData.value = userObject!! to result
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
        else
        {
            withContext(Dispatchers.Main) {
                errorLiveData.value = "Kullanıcı bilgisi alınamıyor lütfen daha sonra tekrar deneyiniz"
            }
        }
    }


    fun removeFromFollowing(receiveId : String?)
    {
        val currentUserId = auth.currentUser?.uid
        if(currentUserId != null && receiveId != null)
        {
            firestore.collection("Users").document(currentUserId).update("following", FieldValue.arrayRemove(receiveId))
            firestore.collection("Users").document(receiveId).update("follower", FieldValue.arrayRemove(currentUserId))
        }
    }

    fun addToFollowRequest(receiveId : String?)
    {
        val currentUserId = auth.currentUser?.uid
        if(currentUserId != null && receiveId != null)
        {
            firestore.collection("Users").document(currentUserId).update("followingRequest", FieldValue.arrayUnion(receiveId))
            firestore.collection("Users").document(receiveId).update("followerRequest", FieldValue.arrayUnion(currentUserId))

        }

    }

    fun removeFromFollowRequest(receiveId: String?)
    {
        val currentUserId = auth.currentUser?.uid
        if(currentUserId != null && receiveId != null)
        {
            firestore.collection("Users").document(currentUserId).update("followingRequest", FieldValue.arrayRemove(receiveId))
            firestore.collection("Users").document(receiveId).update("followerRequest", FieldValue.arrayRemove(currentUserId))
        }
    }


}