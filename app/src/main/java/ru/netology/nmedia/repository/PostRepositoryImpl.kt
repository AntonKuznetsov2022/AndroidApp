package ru.netology.nmedia.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import okio.IOException
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.model.MediaModel

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val data = postDao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override fun getNewCount(latestId: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            try {
                val postsResponse = Api.service.getNewer(latestId)
                if (!postsResponse.isSuccessful) {
                    throw ApiError(postsResponse.code(), postsResponse.message())
                }
                val body = postsResponse.body() ?: throw ApiError(
                    postsResponse.code(),
                    postsResponse.message()
                )
                postDao.insert(body.toEntity().map {
                    it.copy(newPostHidden = true)
                })
                emit(postDao.newerCount())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
        .flowOn(Dispatchers.Default)

    override suspend fun showNewPosts() {
        try {
            postDao.readAll()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun get() {
        try {
            val postsResponse = Api.service.getAll()
            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            postDao.getPostOnServer().forEach { save(it.toDto()) }

            postDao.removeAll()

            postDao.insert(body.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        postDao.save(PostEntity.fromDto(post).copy(postStatusOnSever = true))
        try {
            val postsResponse = Api.service.save(post)
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

    override suspend fun saveWithAttachment(post: Post, mediaModel: MediaModel) {
        try {
            val media = upload(mediaModel)
            val postsResponse = Api.service.save(
                post.copy(
                    attachment = Attachment(media.id, null, AttachmentType.IMAGE)
                )
            )
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

    private suspend fun upload(media: MediaModel): Media {
        val part = MultipartBody.Part.createFormData(
            "file", media.file.name, media.file.asRequestBody()
        )

        val postsResponse = Api.service.uploadMedia(part)
        if (!postsResponse.isSuccessful) {
            throw ApiError(postsResponse.code(), postsResponse.message())
        }

        return requireNotNull(postsResponse.body())
    }

    override suspend fun removeById(id: Long) {
        try {
            val postsResponse = Api.service.removeById(id)
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
            val postsResponse = Api.service.likeById(id)
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
            val postsResponse = Api.service.unlikeById(id)
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
