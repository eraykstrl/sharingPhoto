package com.example.sharingphoto.repository

import androidx.lifecycle.viewModelScope
import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.io.path.fileVisitor
import kotlin.math.sign

class FeedRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var postList : MutableList<Post> = mutableListOf()



    fun getPosts(currentUserId : String) : Flow<List<Post>> = flow {

        var flag = true
        var lastDocument : DocumentSnapshot ?= null
        var batchSize : Long = 50L
        val currentUser = firestore.collection("Users").document(currentUserId)
        val friendList = (currentUser.get().await().get("following") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val currentUsername = firestore.collection("Users").document(currentUserId).get().await().getString("username")
        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").orderBy("postDate").limit(batchSize)
            }
            else
            {
                firestore.collection("Posts").orderBy("postDate").limit(batchSize).startAfter(lastDocument)
            }

            var snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                var posts = snapshot.toObjects(Post::class.java)
                posts = posts.filter {
                    it.user_id == currentUserId || friendList.contains(it.user_id)
                }
                posts.forEach {
                    if(it.likes.contains(currentUsername))
                    {
                        it.isLiked = true
                    }
                    it.commentCounter = it.comment.size
                    it.likeCounter = it.likes.size
                }
                emit(posts)
                lastDocument = snapshot.documents.last()

            }
        }

    }

    fun getAllPosts(currentUserId : String) : Flow<List<Post>> = flow {

        var flag = true
        var lastDocument : DocumentSnapshot ?= null
        var batchSize : Long = 50L
        val currentUsername = firestore.collection("Users").document(currentUserId).get().await().getString("username")

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").orderBy("postDate").whereEqualTo("isPrivate",false).limit(batchSize)
            }
            else
            {
                firestore.collection("Posts").orderBy("postDate").whereEqualTo("isPrivate",false).limit(batchSize).startAfter(lastDocument)
            }

            var snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                var posts = snapshot.toObjects(Post::class.java)
                posts.forEach {
                    if(it.likes.contains(currentUsername))
                    {
                        it.isLiked = true
                    }
                    it.commentCounter = it.comment.size
                    it.likeCounter = it.likes.size
                }
                emit(posts)
                lastDocument = snapshot.documents.last()

            }
        }

    }

    fun getAllPostsById(currentUserId : String,receiveId : String) : Flow<List<Post>> = flow {

        var flag = true
        var lastDocument : DocumentSnapshot ?= null
        var batchSize : Long = 50L
        val currentUsername = firestore.collection("Users").document(currentUserId).get().await().getString("username")

        while(flag)
        {
            val query = if(lastDocument == null)
            {
                firestore.collection("Posts").orderBy("postDate").whereEqualTo("user_id",receiveId).limit(batchSize)
            }
            else
            {
                firestore.collection("Posts").orderBy("postDate").whereEqualTo("user_id",receiveId).limit(batchSize).startAfter(lastDocument)
            }

            var snapshot = query.get().await()
            if(snapshot.isEmpty)
            {
                flag = false
            }
            else
            {
                var posts = snapshot.toObjects(Post::class.java)
                posts.forEach {
                    if(it.likes.contains(currentUsername))
                    {
                        it.isLiked = true
                    }
                    it.commentCounter = it.comment.size
                    it.likeCounter = it.likes.size
                }
                emit(posts)
                lastDocument = snapshot.documents.last()

            }
        }

    }

    fun getSavedPost(currentUserId: String): Flow<List<Post>> = flow {
        val userDoc = firestore.collection("Users").document(currentUserId).get().await()
        val currentUsername = userDoc.getString("username") ?: ""
        val followingUsers = (userDoc.get("following") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        val savedPostIds = (userDoc.get("savedPosts") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

        val batchSize = 10
        var start = 0
        println("get saved post girdi")

        println("size ${savedPostIds.size}")
        while (start < savedPostIds.size) {
            val batchIds = savedPostIds.subList(start, minOf(start + batchSize, savedPostIds.size))

            val snapshot = firestore.collection("Posts")
                .whereIn("postId", batchIds)
                .orderBy("postDate")
                .get()
                .await()

            val posts = snapshot.toObjects(Post::class.java)
                .filter { post ->
                    val postUserDoc = firestore.collection("Users").document(post.user_id!!).get().await()
                    val isPrivate = postUserDoc.getBoolean("isPrivate") ?: false

                    !isPrivate || post.user_id in followingUsers || post.user_id == currentUsername
                }
                .map { post ->
                    post.isLiked = post.likes.contains(currentUsername)
                    post.commentCounter = post.comment.size
                    post.likeCounter = post.likes.size
                    post
                }


            posts.forEach {
                println("id ${it.postId}")
            }
            emit(posts)
            start += batchSize
        }
    }



    suspend fun updatePost(postId : String) : Post?
    {
        val post = firestore.collection("Posts").document(postId).get().await().toObject(Post::class.java)
        post?.commentCounter = post.comment.size
        post?.likeCounter = post.likes.size

        return post
    }



    suspend fun updateComments(postId : String,comment : Comment) : MutableList<Comment>
    {
        val post = firestore.collection("Posts").document(postId)
        val commentList = post.get().await().get("comment") as? List<Map<String,Any>> ?: emptyList()
        val comments= commentList.map {
            map ->
            Comment(
                commentId = map["commentId"] as? String,
                user_id = map["user_id"] as? String,
                username = map["username"] as? String,
                comment = map["comment"] as? String,
                postId = map["postId"] as? String,
                isCommentOwner = map["isCommentOwner"] as? Boolean,
                commentDate = map["commentDate"] as? Date
            )
        }.toMutableList()
        return comments
    }


    suspend fun updateLike(post : Post,info : Int,userId : String) : Post
    {

        val user = firestore.collection("Users").document(userId).get().await()
        val username = user.getString("username")
        val postId = post.postId
        println("update like girdi ve info $info")
        if(postId != null)
        {
            val postInstance = firestore.collection("Posts").document(postId)
            if(info == 0)
            {
                postInstance.update("likes", FieldValue.arrayUnion(username)).await()
                post.isLiked = true

            }
            else if(info == 1)
            {
                postInstance.update("likes", FieldValue.arrayRemove(username)).await()
                post.isLiked = false

            }
        }

        return post
    }


    suspend fun deleteComment(comment : Comment)
    {
        val postId = comment.postId
        if (postId != null) {
            val postRef = firestore.collection("Posts").document(postId)
            val list = postRef.get().await().get("comment") as? List<Map<String,Any>> ?: emptyList()
            val updatedList = list.filterNot { it["commentId"] ==comment.commentId }
            postRef.update("comment",updatedList).await()

        }
    }


    suspend fun modifyComment(newComment : String,comment: Comment)
    {
        val postId = comment.postId
        if(postId != null)
        {
            val postRef = firestore.collection("Posts").document(postId)
            val list = postRef.get().await().get("comment") as? List<Map<String, Any >>
            if(list != null)
            {
                val modifiedComment = list.find { it["commentId"] ==comment.commentId }
                if(modifiedComment != null)
                {
                    postRef.update("comment", FieldValue.arrayRemove(modifiedComment)).await()
                    comment.comment = newComment
                    postRef.update("comment", FieldValue.arrayUnion(comment)).await()
                }
            }
        }
    }


    suspend fun savePost(post : Post, userId : String)
    {
        val postId = post.postId
        if(postId != null)
        {
            firestore.collection("Users").document(userId).update("savedPosts", FieldValue.arrayUnion(postId)).await()
        }
    }

    suspend fun deletePost(post : Post)
    {
        val postId = post.postId
        if(postId != null)
        {
            firestore.collection("Posts").document(postId).delete().await()
        }
    }

    suspend fun updateOwnerComment(comment : String,postId : String)
    {
            val modifiedPost = firestore.collection("Posts").document(postId)
            modifiedPost.update("userComment", comment).await()
    }




}