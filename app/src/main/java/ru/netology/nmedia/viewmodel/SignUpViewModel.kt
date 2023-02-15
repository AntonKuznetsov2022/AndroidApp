package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.MediaModel
import ru.netology.nmedia.model.SignUpModelState
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.io.IOException

class SignUpViewModel : ViewModel() {

    private val _stateSignUp = MutableLiveData<SignUpModelState>()
    val stateSignUp: LiveData<SignUpModelState>
        get() = _stateSignUp

    private val _signUpApp = SingleLiveEvent<AuthModel>()
    val signUpApp: LiveData<AuthModel>
        get() = _signUpApp

    private val _mediaAvatar = MutableLiveData<MediaModel?>(null)
    val mediaAvatar: LiveData<MediaModel?>
        get() = _mediaAvatar

    fun signUp(
        login: String,
        password: String,
        returnPass: String,
        name: String,
    ) = viewModelScope.launch {
        try {
            if (password != returnPass) {
                _stateSignUp.value = SignUpModelState(passCheck = true)
            } else {
                when (val media = mediaAvatar.value) {
                    null -> registration(login, password, name)
                    else -> registrationWithAvatar(login, password, name, media)
                }
            }
        } catch (e: Exception) {
            _stateSignUp.value = SignUpModelState(signUpError = true)
        }
    }

    fun changeAvatar(file: File, uri: Uri) {
        _mediaAvatar.value = MediaModel(uri, file)
    }

    fun clearAvatar() {
        _mediaAvatar.value = null
    }

    private suspend fun registration(login: String, password: String, name: String) {
        try {
            val postsResponse = PostApi.service.registerUser(login, password, name)

            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            _signUpApp.postValue(body)
            _stateSignUp.value = SignUpModelState()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun registrationWithAvatar(
        login: String,
        password: String,
        name: String,
        media: MediaModel
    ) {
        try {
            val part = MultipartBody.Part.createFormData(
                "file", media.file.name, media.file.asRequestBody()
            )

            val postsResponse = PostApi.service.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                part
            )

            if (!postsResponse.isSuccessful) {
                throw ApiError(postsResponse.code(), postsResponse.message())
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            _signUpApp.postValue(body)
            _stateSignUp.value = SignUpModelState()
            clearAvatar()

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}