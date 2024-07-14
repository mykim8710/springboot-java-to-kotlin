package com.javatokotlinspring.todo.api.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.javatokotlinspring.todo.domain.Todo
import java.time.LocalDateTime

data class TodoListResponse(
    val items: List<TodoResponse>  // immutable
) {

    val size: Int
        @JsonIgnore // 프로퍼티로 만들었기 때문에
        get() = items.size

    fun get(index: Int): TodoResponse = items[index]


    // public static이 코틀린에는 존재 X -> companion object(동반객체)
    companion object {
        fun of(todoList: List<Todo>): TodoListResponse {
            return TodoListResponse(todoList.map { TodoResponse.of(it) })
        }
    }
}

data class TodoResponse(
    val id: Long,
    val title: String,
    val description: String,
    val done: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {

    companion object {
        fun of(todo: Todo?): TodoResponse {
            checkNotNull(todo) {
                "Todo is null"
            }

            return TodoResponse(
                id = todo.id,
                title = todo.title,
                description = todo.description,
                done = todo.done,
                createdAt = todo.createdAt,
                updatedAt = todo.updatedAt
            )
        }
    }
}

data class TodoRequest(
    val title: String,
    val description: String,
    val done: Boolean =  false,
)