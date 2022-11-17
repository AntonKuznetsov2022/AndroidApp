package ru.netology.nmedia

interface PostDao {
    fun get(): List<Post>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun save(post: Post): Post
    fun removeById(id: Long)
}