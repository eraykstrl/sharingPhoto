package com.example.sharingphoto.model

import java.util.Date


data class Post(

    val postId :String ?= "",
    val userComment : String ?= "",
    var comment: MutableList<Comment> = mutableListOf(),
    val downloadUrl : String ?= "",
    var likeCounter : Int =0,
    var commentCounter: Int =0,
    val sendNumber : Int ?= 0,
    val user_id : String ?= "",
    val username : String ?= "",
    val likes : List<String?> = emptyList(),
    var isOwner : Boolean = false,
    val postDate : Date? = null,
    var isLiked : Boolean = false,
    var savedPostId : List<String> = emptyList<String>()

)
{

}