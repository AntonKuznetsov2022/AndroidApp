package ru.netology.nmedia

import androidx.lifecycle.Transformations

class PostRepositoryImpl(
    private val dao: PostDao,
) : PostRepository {
    override fun get() = Transformations.map(dao.get()) { list ->
        list.map {
            it.toDto()
        }
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun save(post: Post) {
        dao.save(PostEntity.fromDto(post))
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }
}
