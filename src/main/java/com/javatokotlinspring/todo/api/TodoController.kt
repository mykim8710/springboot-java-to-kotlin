package com.javatokotlinspring.todo.api

import com.javatokotlinspring.todo.api.model.TodoListResponse
import com.javatokotlinspring.todo.api.model.TodoRequest
import com.javatokotlinspring.todo.api.model.TodoResponse
import com.javatokotlinspring.todo.service.TodoService
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(
    // 생성자 주입 방식
    private val todoService: TodoService
) {

    @GetMapping
    fun findAll() =
        ok(TodoListResponse.of(todoService.findAll()))

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long) =
        ok(TodoResponse.of(todoService.findById(id)))

    @PostMapping
    fun create(@RequestBody request: TodoRequest) =
        ok(TodoResponse.of(todoService.create(request)))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: TodoRequest
    ) = ok(TodoResponse.of(todoService.update(id, request)))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long):ResponseEntity<Unit> {
        todoService.delete(id)
        return noContent().build();
    }

}