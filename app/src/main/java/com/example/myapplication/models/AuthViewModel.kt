package com.example.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            // Check if the user document exists on app start
            auth.currentUser?.uid?.let { userId ->
                CoroutineScope(Dispatchers.IO).launch {
                    checkAndCreateUserDocument(userId, auth.currentUser?.email)
                }
            }
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading // Indicate loading state
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { userId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            checkAndCreateUserDocument(userId, email) // Check/create on login
                            _authState.postValue(AuthState.Authenticated)
                        }
                    } ?: run {
                        _authState.value = AuthState.Error("Failed to get user ID after login")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    suspend fun createUserDocument(userId: String, email: String? = null) {
        try {
            val userMap = hashMapOf<String, Any>(
                "email" to (email ?: "")
                // Add any other initial user data you want
            )
            firestore.collection("users").document(userId).set(userMap).await()
            println("User document created successfully for $userId")
        } catch (e: Exception) {
            println("Error creating user document: ${e.message}")
        }
    }

    private suspend fun checkAndCreateUserDocument(userId: String, email: String?) {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            if (!document.exists()) {
                createUserDocument(userId, email)
            } else {
                println("User document already exists for $userId")
            }
        } catch (e: Exception) {
            println("Error checking user document: ${e.message}")
        }
    }

    fun signup(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading // Indicate loading state
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { userId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            createUserDocument(userId, email) // Create on signup
                            _authState.postValue(AuthState.Authenticated)
                        }
                    } ?: run {
                        _authState.value = AuthState.Error("Failed to get user ID after signup")
                    }
                } else {
                    _authState.value =
                        AuthState.Error(task.exception?.message ?: "Something went wrong")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

    sealed class AuthState {
        abstract val message: String?

        data object Authenticated : AuthState() {
            override val message: String? = null
        }

        data object Unauthenticated : AuthState() {
            override val message: String? = null
        }

        data object Loading : AuthState() {
            override val message: String? = null
        }

        data class Error(override val message: String) : AuthState()
    }
}