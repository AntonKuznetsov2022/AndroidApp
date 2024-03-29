package ru.netology.nmedia.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.MediaModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    fun getNewCount(latestId: Long): Flow<Int>
    suspend fun showNewPosts()
    suspend fun get()
    suspend fun likeById(id: Long): Post
    suspend fun unlikeById(id: Long): Post
    suspend fun shareById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, media: MediaModel)
    suspend fun removeById(id: Long)
}