package ru.netology.nmedia.dto

import ru.netology.nmedia.enumeration.AttachmentType

sealed class FeedItem {
    abstract val id: Long
}

data class Ad(
    override val id: Long,
    val image: String,
) : FeedItem()

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int,
    val shares: Int,
    val views: Int,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
) : FeedItem()

data class TimeSeparator(
    override val id: Long,
    val time: Time
) : FeedItem()

enum class Time {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
}

data class Attachment(
    val url: String,
    val description: String? = null,
    val type: AttachmentType,
)
