package com.example.sharingphoto.model

import java.util.Date

data class Comment(

    val commentId : String ?= "",
    val user_id : String ?= "",
    val username : String ?= "",
    var comment : String ?= "",
    val postId : String ?= "",
    var isCommentOwner : Boolean ?= false,
    val commentDate : Date?= null

)
