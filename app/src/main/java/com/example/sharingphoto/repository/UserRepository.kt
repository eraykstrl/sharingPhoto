package com.example.sharingphoto.repository

import com.example.sharingphoto.model.Post
import com.example.sharingphoto.model.User
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllUser(batchSize : Int = 50,userId : String) : Flow<List<User>> = flow {

        val followingRequest = (firestore.collection("Users").document(userId).get().await()
            .get("followingRequest") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        var lastDocument : DocumentSnapshot ?= null
        var flag : Boolean = true

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                println()
                firestore.collection("Users").orderBy("username").limit(batchSize.toLong())

            }
            else
            {
                firestore.collection("Users").orderBy("username").startAfter(lastDocument).limit(batchSize.toLong())
            }

            val snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                var users = snapshot.toObjects(User::class.java)
                users = users.filter {
                    it ->
                    it.user_id !in followingRequest && it.user_id != userId
                }
                emit(users)
                lastDocument = snapshot.documents.last()
            }
        }
    }


    fun getFollowerRequests(currentUserId : String,batchSize: Int = 50) : Flow<List<User>> = flow {

        val user = firestore.collection("Users").document(currentUserId)
        val idList = (user.get().await().get("followerRequest") as? List<*>)?.filterIsInstance<String>() ?: emptyList<String>()
        val userList = mutableListOf<User>()


        idList.chunked(10).forEach {
            chunk ->
            val querySnapshot = firestore.collection("Users").whereIn("user_id",chunk).get().await()

            querySnapshot.documents.mapNotNull {
                it.toObject(User::class.java)
            }.also {
                users ->
                userList.addAll(users)
            }
        }
        emit(userList)


    }

    suspend fun sendFollowingRequest(currentUserId : String?,receiveId : String?)
    {
        if(currentUserId != null && receiveId != null)
        {
            firestore.collection("Users").document(receiveId).update("followerRequest", FieldValue.arrayUnion(currentUserId))
                .await()
            firestore.collection("Users").document(currentUserId).update("followingRequest",
                FieldValue.arrayUnion(receiveId)).await()

        }

    }

    suspend fun getFollowingInfo(currentUserId: String,receiveId: String?) : Int
    {
        if(currentUserId == receiveId)
        {
            return 4
        }
        if(receiveId != null)
        {
            val query = firestore.collection("Users").whereEqualTo("user_id",currentUserId)
                .whereArrayContains("followingRequest",receiveId).get().await()

            val secondQuery = firestore.collection("Users").whereEqualTo("user_id",currentUserId)
                .whereArrayContains("following",receiveId).get().await()

            if(!query.isEmpty)
            {
                return 2
            }
            else if(!secondQuery.isEmpty)
            {
                return 3
            }
            else
            {
                return 1
            }
        }
        else
        {
            return 1
        }

    }



    fun getPostsByUserId(userId : String) : Flow<List<Post>> = flow {
        var lastDocument : DocumentSnapshot ?=null
        var flag = true
        var batchSize = 50

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereEqualTo("postType",
                    "IMAGE").limit(batchSize.toLong())
            }
            else
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereEqualTo("postType","IMAGE").limit(batchSize.toLong()).startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                val posts = snapshot.toObjects(Post::class.java)
                lastDocument = snapshot.documents.last()
                emit(posts)
            }

        }
    }

    fun getPostsByUserIdVideo(userId : String) : Flow<List<Post>> = flow {
        var lastDocument : DocumentSnapshot ?=null
        var flag = true
        var batchSize = 50

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereEqualTo("postType",
                    "VIDEO").limit(batchSize.toLong())
            }
            else
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereEqualTo("postType","VIDEO").limit(batchSize.toLong()).startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                val posts = snapshot.toObjects(Post::class.java)
                lastDocument = snapshot.documents.last()
                emit(posts)
            }

        }
    }

    fun getPostsByUserIdTextOrFile(userId : String) : Flow<List<Post>> = flow {
        var lastDocument : DocumentSnapshot ?=null
        var flag = true
        var batchSize = 50

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereIn("postType",
                    listOf("TEXT","FILE")).limit(batchSize.toLong())
            }
            else
            {
                firestore.collection("Posts").whereEqualTo("user_id",userId).whereIn("postType",listOf("FILE","TEXT")).limit(batchSize.toLong()).startAfter(lastDocument)
            }

            val snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                val posts = snapshot.toObjects(Post::class.java)
                lastDocument = snapshot.documents.last()
                posts.forEach {
                    println("tipi")
                    println(it.postType)
                }
                emit(posts)
            }

        }
    }

    fun getFollowingRequest(currentUserId : String?) : Flow<List<User>> = flow {

        val returnList = mutableListOf<User>()
        if(currentUserId != null)
        {
            val user = firestore.collection("Users").document(currentUserId).get().await()
            val idList = (user.get("followingRequest") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            idList.chunked(10).forEach {
                chunk ->
                val newUser = firestore.collection("Users").whereIn("user_id",chunk).get().await().toObjects(User::class.java)
                returnList.addAll(newUser)
            }

            emit(returnList)
        }

    }

    fun getUserFollowersById(currentUserId : String,profileId : String) : Flow<List<Pair<User, Int>>> = flow {

        var user = firestore.collection("Users").document(profileId).get().await()
        var idList = (user.get("follower") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        var currentUser = firestore.collection("Users").document(currentUserId).get().await()
        var currentIdList = (currentUser.get("following") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        var currentFollowingRequest = (currentUser.get("followingRequest") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val userList = mutableListOf<Pair<User, Int>>()



        idList.chunked(10).forEach {
            chunk ->
            val snapshot = firestore.collection("Users").whereIn("user_id",chunk).get().await().toObjects(User::class.java)
            snapshot.forEach {
                it->
                if(it.user_id == currentUserId)
                {
                    userList.add(it to 4)
                }
                else if(currentIdList.contains(it.user_id))
                {
                    userList.add(it to 3)
                }
                else if(currentFollowingRequest.contains(it.user_id))
                {
                    userList.add(it to 2)
                }
                else
                {
                    userList.add(it to 1)
                }
            }

            emit(userList)

        }
    }

    fun getUserFollowingById(currentUserId : String,profileId : String) : Flow<List<Pair<User, Int>>> = flow {

        var user = firestore.collection("Users").document(profileId).get().await()
        var idList = (user.get("following") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        var currentUser = firestore.collection("Users").document(currentUserId).get().await()
        var currentIdList = (currentUser.get("following") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        var currentFollowingRequest = (currentUser.get("followingRequest") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val userList = mutableListOf<Pair<User, Int>>()


        idList.chunked(10).forEach {
                chunk ->
            val snapshot = firestore.collection("Users").whereIn("user_id",chunk).get().await().toObjects(User::class.java)
            snapshot.forEach {
                    it->
                if(it.user_id == currentUserId)
                {
                    userList.add(it to 4)
                }
                else if(currentIdList.contains(it.user_id))
                {
                    userList.add(it to 3)
                }
                else if(currentFollowingRequest.contains(it.user_id))
                {
                    userList.add(it to 2)
                }
                else
                {
                    userList.add(it to 1)
                }
            }

            emit(userList)

        }
    }



}