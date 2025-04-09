package com.example.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

// Data class for Todo items
data class Todo(
    val id: String = "",
    val title: String = "",
    val createdAt: Date = Date(),
    val completed: Boolean = false,
    val userId: String = ""
)

class TodoViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _todoList = MutableLiveData<List<Todo>>()
    val todoList: LiveData<List<Todo>> = _todoList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // auth listener
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        // šī tiek aktivizēts , kad mainīsies auth state (log in/logout)
        loadTodos()
    }

    init {
        // auth listener reģistrācija
        auth.addAuthStateListener(authStateListener)

        loadTodos()
    }

    override fun onCleared() {
        super.onCleared()
        // dzēst auth klausītāju, kad ViewModelis tiek dzēsts
        auth.removeAuthStateListener(authStateListener)
    }

    // sarakstu ielādžešana no forestore
    fun loadTodos() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _todoList.value = emptyList()
            return
        }

        _isLoading.value = true

        // pārbauda vai ir izvedota kolekcijā todo
        firestore.collection("todos")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener {
                // izvedota , now set up real time listener
                setupRealtimeUpdates(currentUser.uid)
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = ":Neizdevās inicializēt todos ${exception.message}"
            }
    }

    private fun setupRealtimeUpdates(userId: String) {
        firestore.collection("todos")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, exception ->
                _isLoading.value = false

                if (exception != null) {
                    _error.value = "Error loading todos: ${exception.message}"
                    return@addSnapshotListener
                }

                val todos = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        // konvērtē Firestore dokumentu to Todo object
                        val id = doc.id
                        val title = doc.getString("title") ?: ""
                        val createdAt = doc.getTimestamp("createdAt")?.toDate() ?: Date()
                        val completed = doc.getBoolean("completed") ?: false
                        val userId = doc.getString("userId") ?: ""

                        Todo(id, title, createdAt, completed, userId)
                    } catch (e: Exception) {
                        _error.value = "Kļūdaina todo analizēšana: ${e.message}"
                        null
                    }
                } ?: emptyList()

                _todoList.value = todos
            }
    }

    // Add a new todo
    fun addTodo(title: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _error.value = "Jums ir jābūt pieteicies, lai pievienotu todos"
            return
        }

        if (title.isBlank()) {
            _error.value = "Todo virsraksts nevar būt tukšs"
            return
        }

        _isLoading.value = true

        // izveidot todo datu karti
        val todoData = hashMapOf(
            "title" to title,
            "createdAt" to Date(),
            "completed" to false,
            "userId" to currentUser.uid
        )

        firestore.collection("todos")
            .add(todoData)
            .addOnSuccessListener {
                _isLoading.value = false
                // nevajadzēs manuāli atjaunināt sarakstu , jo tas tiek atjaunināts ar listener
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Neizdevās pievienot todo: ${exception.message}"
            }
    }

    // update jau esošo todo
    fun updateTodo(todo: Todo) {
        _isLoading.value = true

        firestore.collection("todos")
            .document(todo.id)
            .set(todo)
            .addOnSuccessListener {
                _isLoading.value = false
                // update in ui
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Failed to update todo: ${exception.message}"
            }
    }

    // Toggle ja pabeidzās todo
    fun toggleTodoCompletion(todo: Todo) {
        _isLoading.value = true

        firestore.collection("todos")
            .document(todo.id)
            .update("completed", !todo.completed)
            .addOnSuccessListener {
                _isLoading.value = false
                // The snapshot listener will update the UI
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Failed to update todo status: ${exception.message}"
            }
    }

    // dzēst todo
    fun deleteTodo(todoId: String) {
        _isLoading.value = true

        firestore.collection("todos")
            .document(todoId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                // The snapshot listener will update the UI
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _error.value = "Failed to delete todo: ${exception.message}"
            }
    }

    // Kļūdas dzēšana
    fun clearError() {
        _error.value = null
    }
}