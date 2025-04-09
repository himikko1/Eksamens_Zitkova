package com.example.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.RecipeData.Comment
import com.example.myapplication.RecipeData.FirestoreRepository
import com.example.myapplication.RecipeData.Recipe
import com.google.firebase.auth.FirebaseAuth

class RecipeViewModel : ViewModel() {
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes

    private val _currentRecipe = MutableLiveData<Recipe?>()
    val currentRecipe: LiveData<Recipe?> get() = _currentRecipe

    private val _comments = MutableLiveData<Map<String, List<Comment>>>()
    val comments: LiveData<Map<String, List<Comment>>> get() = _comments

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val commentsCache = mutableMapOf<String, List<Comment>>()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        _isLoading.value = true
        FirestoreRepository.getRecipes { recipesList ->
            _recipes.value = recipesList
            _isLoading.value = false
        }
    }

    fun getRecipeById(recipeId: String) {
        _isLoading.value = true
        FirestoreRepository.getRecipeById(recipeId) { recipe ->
            _currentRecipe.value = recipe
            _isLoading.value = false
            if (recipe != null) {
                loadComments(recipeId)
            }
        }
    }

    fun addRecipe(recipe: Recipe) {
        _isLoading.value = true
        FirestoreRepository.addRecipe(recipe) { success, _ ->
            _isLoading.value = false
            if (success) {
                loadRecipes()
            } else {
                _error.value = "Neizdevās pievienot recepti"
            }
        }
    }

    fun likeRecipe(recipeId: String) {
        FirestoreRepository.likeRecipe(recipeId)


        val currentList = _recipes.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == recipeId }
        if (index >= 0) {
            val updatedRecipe = currentList[index].copy(likes = currentList[index].likes + 1)
            currentList[index] = updatedRecipe
            _recipes.value = currentList


            if (_currentRecipe.value?.id == recipeId) {
                _currentRecipe.value = updatedRecipe
            }
        }
    }

    fun addComment(recipeId: String, text: String) {
        if (text.isBlank()) return

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Lietotājs"

        val comment = Comment(userId = userId, text = text, userName = userName)

        FirestoreRepository.addComment(recipeId, comment) { success ->
            if (success) {
                loadComments(recipeId)
            } else {
                _error.value = "Neizdevās pievienot komentāru"
            }
        }
    }

    fun loadComments(recipeId: String) {
        FirestoreRepository.getComments(recipeId) { commentsList ->
            commentsCache[recipeId] = commentsList
            _comments.value = commentsCache
        }
    }

    fun getCommentsForRecipe(recipeId: String): LiveData<List<Comment>> {
        val commentLiveData = MutableLiveData<List<Comment>>()


        commentsCache[recipeId]?.let {
            commentLiveData.value = it
        }


        loadComments(recipeId)


        comments.observeForever { commentsMap ->
            commentLiveData.value = commentsMap[recipeId] ?: emptyList()
        }

        return commentLiveData
    }

    fun clearError() {
        _error.value = null
    }
}