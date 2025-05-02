package com.mikrochek.data

import com.mikrochek.server.database.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserState {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    fun setUser(user: User?) {
        _currentUser.value = user
        _isAuthenticated.value = user != null
        if (user != null) {
            _authToken.value = generateAuthToken()
        } else {
            _authToken.value = null
        }
    }

    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    fun setAuthToken(token: String?) {
        _authToken.value = token
    }

    fun getAuthToken(): String? {
        return _authToken.value
    }

    fun clearUserSession() {
        _currentUser.value = null
        _isAuthenticated.value = false
        _authToken.value = null
    }

    fun isUserValid(): Boolean {
        return _currentUser.value != null && 
               _currentUser.value?.isActive == true && 
               _authToken.value != null
    }

    private fun generateAuthToken(): String {
        return java.util.UUID.randomUUID().toString()
    }
} 