package com.example.myapplication.RecipeData

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    fun addRecipe(recipe: Recipe, onComplete: (Boolean, String?) -> Unit) {

        val recipeRef = db.collection("recipes").document()

        // Update the recipe with the generated ID
        val recipeWithId = recipe.copy(id = recipeRef.id)

        recipeRef.set(recipeWithId)
            .addOnSuccessListener { onComplete(true, recipeRef.id) }
            .addOnFailureListener { onComplete(false, null) }
    }

    fun getRecipes(onRecipesLoaded: (List<Recipe>) -> Unit) {
        db.collection("recipes")
            .orderBy("likes", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val recipes = result.map { it.toObject(Recipe::class.java) }
                onRecipesLoaded(recipes)
            }
            .addOnFailureListener {
                onRecipesLoaded(emptyList())
            }
    }

    fun getRecipeById(recipeId: String, onRecipeLoaded: (Recipe?) -> Unit) {
        db.collection("recipes").document(recipeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onRecipeLoaded(document.toObject(Recipe::class.java))
                } else {
                    onRecipeLoaded(null)
                }
            }
            .addOnFailureListener {
                onRecipeLoaded(null)
            }
    }

    fun likeRecipe(recipeId: String) {
        val recipeRef = db.collection("recipes").document(recipeId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val newLikes = snapshot.getLong("likes")?.plus(1) ?: 1
            transaction.update(recipeRef, "likes", newLikes)
        }
    }

    fun addComment(recipeId: String, comment: Comment, onComplete: (Boolean) -> Unit) {
        val commentWithTimestamp = comment.copy(timestamp = System.currentTimeMillis())

        db.collection("recipes").document(recipeId)
            .collection("comments").add(commentWithTimestamp)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getComments(recipeId: String, onCommentsLoaded: (List<Comment>) -> Unit) {
        db.collection("recipes").document(recipeId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.map { it.toObject(Comment::class.java) }
                onCommentsLoaded(comments)
            }
            .addOnFailureListener {
                onCommentsLoaded(emptyList())
            }
    }
}