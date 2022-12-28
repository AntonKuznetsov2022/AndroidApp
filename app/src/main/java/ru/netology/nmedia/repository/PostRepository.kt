package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun get(callback: Callback<List<Post>>)
    fun likeById(id: Long, callback: Callback<Post>)
    fun unlikeById(id: Long, callback: Callback<Post>)
    fun shareById(id: Long)
    fun save(post: Post, callback: Callback<Unit>)
    fun removeById(id: Long, callback: Callback<Unit>)

    interface Callback<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }
}