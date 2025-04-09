package com.example.myapplication.pages
import java.time.Instant
import java.util.Date

data class Todo (
    var id: Int,
    var title: String,
    var createdAt : Date
)

fun getFakeTodos() : List<Todo>{
    return listOf(
        Todo(1, "1 Todo", Date.from(Instant.now())),
        Todo(2, "2 Todo", Date.from(Instant.now())),
        Todo(3, "3 Todo", Date.from(Instant.now())),
        Todo(4, "4 Todo", Date.from(Instant.now()))
    )
}
