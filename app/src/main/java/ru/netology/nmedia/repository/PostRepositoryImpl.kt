package ru.netology.nmedia.repository

import androidx.lifecycle.map
import retrofit2.HttpException
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import okio.IOException

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data = postDao.getAll().map(List<PostEntity>::toDto)

    override suspend fun get() {
        try {
            val postsResponse = PostApi.service.getAll()
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            postDao.insert(body.toEntity())

            postDao.getPostOnServer().forEach { save(it.toDto()) }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun savePost(post: Post) {
        try {
            postDao.insert(PostEntity.fromDto(post))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun save(post: Post) {
        postDao.save(PostEntity.fromDto(post).copy(postStatusOnSever = true))
        try {
            val postsResponse = PostApi.service.save(post)
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            postDao.insert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            val postsResponse = PostApi.service.removeById(id)
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            postDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long): Post {
        try {
            val postsResponse = PostApi.service.likeById(id)
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }
            postDao.likedById(id)
            return postsResponse.body() ?: throw HttpException(postsResponse)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unlikeById(id: Long): Post {
        try {
            val postsResponse = PostApi.service.unlikeById(id)
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }
            postDao.unlikedById(id)
            return postsResponse.body() ?: throw HttpException(postsResponse)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        TODO("Not yet implemented")
    }
}
