package com.example.ble_jetpackcompose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetEmailSent : AuthState() // New state for password reset
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Current user state
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    init {
        // Initialize current user and listen for changes
        updateCurrentUser()
        auth.addAuthStateListener { firebaseAuth ->
            updateCurrentUser()
        }
    }

    private fun updateCurrentUser() {
        _currentUser.value = auth.currentUser
        auth.currentUser?.let { user ->
            _authState.value = AuthState.Success(user)  // Changes state again
        } ?: run {
            _authState.value = AuthState.Idle  // Changes state again
        }
    }

    // Get current user
    fun checkCurrentUser(): FirebaseUser? = auth.currentUser

    // Check if user is anonymous
    fun isAnonymousUser(): Boolean = auth.currentUser?.isAnonymous ?: false

    // Get user email
    fun getUserEmail(): String = auth.currentUser?.email ?: ""

    // Check if user is authenticated
    fun isUserAuthenticated(): Boolean = auth.currentUser != null

    // New function for forgot password
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.PasswordResetEmailSent
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    else -> e.message ?: "Failed to send password reset email."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun signInAsGuest() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInAnonymously().await()
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    updateCurrentUser()
                } ?: throw Exception("Anonymous sign-in failed")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    updateCurrentUser()
                } ?: throw Exception("Registration failed. No user created.")
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthWeakPasswordException -> "Weak password: ${e.reason}"
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                    is FirebaseAuthUserCollisionException -> "This email is already registered."
                    else -> e.message ?: "Registration failed due to an unknown error."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.let {
                    _authState.value = AuthState.Success(it)
                    updateCurrentUser()
                } ?: throw Exception("Login failed. No user found.")
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    else -> e.message ?: "Login failed due to an unknown error."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
        updateCurrentUser()
    }
}