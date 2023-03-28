package ru.netology.nmedia.repository

import androidx.paging.*
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
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.model.MediaModel
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService,
    postRemoteKeyDao: PostRemoteKeyDao,
    appDb: AppDb,
) : PostRepository {

/*    override val data = postDao.getAll().map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)*/

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        remoteMediator = PostRemoteMediator(
            apiService = apiService,
            postDao = postDao,
            postRemoteKeyDao = postRemoteKeyDao,
            appDb = appDb,
            ),
        pagingSourceFactory = { postDao.allPostPaging() })
        .flow.map { pagingData ->
            pagingData.map(PostEntity::toDto)
        }

    override fun getNewCount(latestId: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000)
            try {
                val postsResponse = apiService.getNewer(latestId)
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
            val postsResponse = apiService.getAll()
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
            val postsResponse = apiService.save(post)
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
            val postsResponse = apiService.save(
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

        val postsResponse = apiService.uploadMedia(part)
        if (!postsResponse.isSuccessful) {
            throw ApiError(postsResponse.code(), postsResponse.message())
        }

        return requireNotNull(postsResponse.body())
    }

    override suspend fun removeById(id: Long) {
        try {
            val postsResponse = apiService.removeById(id)
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
            val postsResponse = apiService.likeById(id)
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
            val postsResponse = apiService.unlikeById(id)
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
