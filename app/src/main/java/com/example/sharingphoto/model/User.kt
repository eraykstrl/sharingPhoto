package com.example.sharingphoto.model



data class User(
    val user_id: String = "",
    val name: String? = null,
    val surname: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val profilePhoto: String? = null,
    val posts: List<Post>? = null,
    val individual: String? = null,
    val isVerify: Boolean? = null,
    val appNotification: Boolean? = null,
    val messageNotification: Boolean? = null,
    val likeNotification: Boolean? = null,
    val commentNotification: Boolean? = null,
    val securityNotification: Boolean? = null,
    val followingNotification: Boolean? = null,
    val friends: List<String>? = null,
    val follower: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val followerRequest: List<String> = emptyList(),
    val followingRequest : List<String> = emptyList(),
    val savedPost: List<String>? = null,
    val aboutUser : String ?= null,
    val isPrivate : Boolean ?= null
)


