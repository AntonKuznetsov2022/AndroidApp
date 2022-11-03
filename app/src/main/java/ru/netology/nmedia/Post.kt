package ru.netology.nmedia

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 999,
    val shares: Int = 10,
    val views: Int = 1_000_000,
    val video: String? = null
)