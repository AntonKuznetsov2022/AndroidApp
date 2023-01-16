package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import java.lang.Exception

class PostRepositoryImpl : PostRepository {

    override fun get(callback: PostRepository.Callback<List<Post>>) {
        PostApi.service.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun likeById(id: Long, callback: PostRepository.Callback<Post>) {
        PostApi.service.likeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun unlikeById(id: Long, callback: PostRepository.Callback<Post>) {
        PostApi.service.unlikeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Unit>) {
        PostApi.service.save(post)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    val posts = response.body()
                    if (posts == null) {
                        callback.onError(RuntimeException("Body is null"))
                        return
                    }
                    callback.onSuccess(posts)
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        PostApi.service.deletePostById(id)
            .enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException(response.message()))
                        return
                    }
                    callback.onSuccess(Unit)
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }
            })
    }

    override fun shareById(id: Long) {
        // TODO
    }
}
