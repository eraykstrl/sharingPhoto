package com.example.sharingphoto.repository

import com.example.sharingphoto.model.Comment
import com.example.sharingphoto.model.Post
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.io.path.fileVisitor
import kotlin.math.sign

class FeedRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var postList : MutableList<Post> = mutableListOf()

    suspend fun getPosts() : List<Post>
    {
        val posts = firestore.collection("Posts").orderBy("postDate", Query.Direction.ASCENDING).get().await()
            .toObjects(Post::class.java)
        val userId = auth.currentUser?.uid
        val username = getCurrentUserById(userId!!)
        for(post in posts)
        {
            post.likes.forEach {
                if(it == username)
                {
                    post.isLiked = true
                }
                else
                {
                    post.isLiked = false
                }
            }
            post.likeCounter = post.likes.size
            post.commentCounter = post.comment.size
        }

        postList.clear()
        postList.addAll(posts)
        return postList
    }

    suspend fun getCurrentUserById(userId : String) : String
    {
        val user = firestore.collection("Users").document(userId).get().await()
        val username = user.getString("username")

        return username!!
    }


    suspend fun updatePost(postId : String) : Post?
    {
        val post = firestore.collection("Posts").document(postId).get().await().toObject(Post::class.java)
        post?.commentCounter = post.comment.size
        post?.likeCounter = post.likes.size

        return post
    }

    fun updateComment(comment : Comment) : Comment?
    {
       return comment
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
            println("save post repository içindeyim simdi save edicez")
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