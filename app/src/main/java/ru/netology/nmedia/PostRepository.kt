package ru.netology.nmedia

import androidx.lifecycle.LiveData

interface PostRepository {
    fun get(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
}