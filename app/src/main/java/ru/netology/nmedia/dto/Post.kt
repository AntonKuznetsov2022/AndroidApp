package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

data class Post(
    var id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val shares: Int,
    val views: Int,
    val attachment: Attachment? = null,
)

data class Attachment(
    val url: String,
    val description: String?,
    val type: AttachmentType,
)
