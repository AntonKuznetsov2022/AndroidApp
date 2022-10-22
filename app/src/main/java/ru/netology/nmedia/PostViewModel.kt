package ru.netology.nmedia

import androidx.lifecycle.ViewModel

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.get()
    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id:Long) = repository.shareById(id)
}