package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.EMPTY_REQUEST
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun get(callback: PostRepository.Callback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                        return
                    }
                    val data: List<Post>? = response.body?.string()?.let {
                        gson.fromJson(it, typeToken.type)
                    }

                    data ?: run {
                        callback.onError(Exception("Body is null"))
                        return
                    }
                    callback.onSuccess(data)
                }
            })
    }

    override fun likeById(id: Long, callback: PostRepository.Callback<Post>) {

        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts/${id}/likes")
            .post(EMPTY_REQUEST)
            .build()
        newCallLikes(request, callback)
    }

    override fun unlikeById(id: Long, callback: PostRepository.Callback<Post>) {

        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts/${id}/likes")
            .delete()
            .build()

        newCallLikes(request, callback)
    }



    override fun save(post: Post, callback: PostRepository.Callback<Unit>) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/posts")
            .build()

        newCall(request, callback)
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/posts/$id")
            .build()

        newCall(request, callback)
    }

    override fun shareById(id: Long) {
        // TODO
    }

    fun newCallLikes(request: Request, callback: PostRepository.Callback<Post>) {
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                        return
                    }
                    val data: Post? = response.body?.string()?.let {
                        gson.fromJson(it, Post::class.java)
                    }

                    data ?: run {
                        callback.onError(Exception("Body is null"))
                        return
                    }
                    callback.onSuccess(data)
                }
            })
    }

    fun newCall(request: Request, callback: PostRepository.Callback<Unit>) {
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                        return
                    }
                    callback.onSuccess(Unit)
                }
            })
    }
}
