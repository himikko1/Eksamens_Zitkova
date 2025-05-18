package com.example.myapplication.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.ui.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first // Import first()
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val themePreferences = ThemePreferences(application)

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            // Pārbauda, vai lietotāja dokuments pastāv lietotnes palaišanas brīdī
            auth.currentUser?.uid?.let { userId ->
                viewModelScope.launch(Dispatchers.IO) {
                    checkAndCreateUserDocument(userId, auth.currentUser?.email)
                    loadUserProperties(userId)
                }
            }
            _authState.value = AuthState.Authenticated
        }
    }


    //funkcija ļauj ielogoties sistēmān ar e-pastu un paroli
    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.uid?.let { userId ->
                        viewModelScope.launch(Dispatchers.IO) {
                            checkAndCreateUserDocument(userId, email)
                            loadUserProperties(userId)
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
                        viewModelScope.launch(Dispatchers.IO) {
                            createUserDocument(userId, email) // Create on signup
                            // No need to load properties immediately after signup,
                            // as they will be default.
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
        viewModelScope.launch {
            // Save user properties before signing out
            auth.currentUser?.uid?.let { userId ->
                saveUserProperties(userId)
            }
            auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Function to save user properties to Firestore
    private suspend fun saveUserProperties(userId: String) {
        try {
            val isDarkTheme = themePreferences.isDarkTheme.first() // Get current theme from DataStore
            val userProperties = hashMapOf<String, Any>(
                "isDarkTheme" to isDarkTheme
                // Add other user properties here
            )
            firestore.collection("users").document(userId).update(userProperties).await()
            println("User properties saved successfully for $userId")
        } catch (e: Exception) {
            println("Error saving user properties: ${e.message}")
        }
    }

    // Function to load user properties from Firestore
    private suspend fun loadUserProperties(userId: String) {
        try {
            val document = firestore.collection("users").document(userId).get().await()
            if (document.exists()) {
                val isDarkTheme = document.getBoolean("isDarkTheme") ?: false // Default to false if not found
                themePreferences.setDarkTheme(isDarkTheme) // Update DataStore with loaded theme
                println("User properties loaded successfully for $userId")
            } else {
                println("User document not found for loading properties: $userId")
            }
        } catch (e: Exception) {
            println("Error loading user properties: ${e.message}")
        }
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