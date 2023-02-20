package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.SignInModelState
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException

class SignInViewModel : ViewModel() {

    private val _stateSignIn = MutableLiveData<SignInModelState>()
    val stateSignIn: LiveData<SignInModelState>
        get() = _stateSignIn

    private val _signInApp = SingleLiveEvent<AuthModel>()
    val signInApp: LiveData<AuthModel>
        get() = _signInApp

    fun signIn(login: String, password: String) = viewModelScope.launch {
        try {
            val postsResponse = Api.service.updateUser(login, password)
            if (!postsResponse.isSuccessful) {
                _stateSignIn.value = SignInModelState(signInError = true)
            }

            if (postsResponse.code() == 400 || postsResponse.code() == 404) {
                _stateSignIn.value = SignInModelState(signInWrong = true)
            }

            val body = postsResponse.body() ?: throw ApiError(
                postsResponse.code(),
                postsResponse.message()
            )
            _signInApp.postValue(body)
            _stateSignIn.value = SignInModelState()
        } catch (e: IOException) {
            _stateSignIn.value = SignInModelState(signInError = true)
        } catch (e: Exception) {
            _stateSignIn.value = SignInModelState(signInWrong = true)
        }
    }
}
