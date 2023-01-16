package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0,
    content = "",
    author = "Netology",
    likedByMe = false,
    likes = 0,
    published = "",
    shares = 0,
    views = 0,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.get(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.postValue(FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.Callback<Unit> {
                override fun onSuccess(data: Unit) {
                    _postCreated.postValue(Unit)
                }

                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun cancel() {
        edited.value = empty
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        repository.likeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty().map {
                        if (it.id == data.id) data else it
                    })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun unlikeById(id: Long) {
        repository.unlikeById(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty().map {
                        if (it.id == data.id) data else it
                    })
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun shareById(id: Long) {
        repository.shareById(id)
    }

    fun removeById(id: Long) {
        repository.removeById(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(data: Unit) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}
