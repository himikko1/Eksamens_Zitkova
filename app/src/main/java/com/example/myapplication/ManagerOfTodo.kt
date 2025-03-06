//package com.example.myapplication
//
//import com.example.myapplication.pages.Todo
//import java.time.Instant
//import java.util.Date
//
//object ManagerOfTodo {
//    private val todoList = mutableListOf<Todo>()
//
//
//    // Function to get all todos
//    fun allTodo(): List<Todo> {
//        return todoList
//    }
//
//    // funkcija lai delete uzdevumu
//    fun deleteTodo() {
//        todoList.removeIf{
//            it.id==id
//        }
//    }
//
//    // Function to add a new todo
//    fun addTodo() {
//        todoList.add(Todo(System.currentTimeMillis().toInt(), title, Date.form(Instant.now())))
//    }
//}