## build.grade(groovy)
```
plugins {
	id 'org.springframework.boot' version '3.2.6'
	id 'io.spring.dependency-management' version '1.1.5'
	id 'java'
}

group = 'com.javatokotlinspring'
version = '0.0.1-SNAPSHOT'

java {
	sourceCompatibility = '21'
}

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
	useJUnitPlatform()
}
```

## build.grade.kts(Gradle Kotlin DSL)
```
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.6"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "com.javatokotlinspring"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// 코틀린 프로젝트 시 default
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	runtimeOnly("com.h2database:h2")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.ninja-squad:springmockk:3.1.1")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "21"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
```

### setting.gradle
```
rootProject.name = 'todo'
```

### setting.gradle.kts
```
rootProject.name = "todo"
```


## Controller Layer Refactoring(Java -> Kotlin)
```
@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<TodoListResponse> getAll() {
        List<Todo> todos = todoService.findAll();
        return ResponseEntity.ok(TodoListResponse.of(todos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> get(@PathVariable Long id) {
        Todo todo = todoService.findById(id);
        return ResponseEntity.ok(TodoResponse.of(todo));
    }

    @PostMapping
    public ResponseEntity<TodoResponse> create(@RequestBody TodoRequest request) {
        Todo todo = todoService.create(request);
        return ResponseEntity.ok(TodoResponse.of(todo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id,
                                               @RequestBody TodoRequest request) {
        Todo todo = todoService.update(id, request);
        return ResponseEntity.ok(TodoResponse.of(todo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        todoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}



@RestController
@RequestMapping("/api/todos")
class TodoController(
    private val todoService: TodoService,
) {

    @GetMapping
    fun getAll() =
        ok(TodoListResponse.of(todoService.findAll()))

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long) =
        ok(TodoResponse.of(todoService.findById(id)))

    @PostMapping
    fun create(@RequestBody request: TodoRequest) =
        ok(TodoResponse.of(todoService.create(request)))

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TodoRequest) =
        ok(TodoResponse.of(todoService.update(id, request)))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Unit> {
        todoService.delete(id)
        return noContent().build()
    }
}
```

## dto Refactoring(Java -> Kotlin)
```
@Data
public class TodoListResponse {

    private final List<TodoResponse> items;

    private TodoListResponse(List<TodoResponse> items) {
        this.items = items;
    }

    public int size() {
        return items.size();
    }

    public TodoResponse get(int index) {
        return items.get(index);
    }

    public static TodoListResponse of(List<Todo> todoList) {
        List<TodoResponse> todoListResponse = todoList.stream()
            .map(TodoResponse::of)
            .collect(Collectors.toList());

        return new TodoListResponse(todoListResponse);
    }

}

@Data
@Builder
public class TodoResponse {

    private Long id;

    private String title;

    private String description;

    private Boolean done;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static TodoResponse of(Todo todo) {
        Assert.notNull(todo, "Todo is null");

        return TodoResponse.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .description(todo.getDescription())
            .done(todo.getDone())
            .createdAt(todo.getCreatedAt())
            .updatedAt(todo.getUpdatedAt())
            .build();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {
    private String title;
    private String description;
    private Boolean done = false;
}


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
```


## Service Layer Refactoring(Java -> Kotlin)
```
@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAll(Sort.by(Direction.DESC, "id"));
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        return todoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public Todo create(TodoRequest request) {
        Assert.notNull(request, "TodoRequest is null");

        Todo todo = Todo.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .done(false)
            .createdAt(LocalDateTime.now())
            .build();
        return todoRepository.save(todo);
    }

    @Transactional
    public Todo update(Long id, TodoRequest request) {
        Assert.notNull(request, "TodoRequest is null");

        Todo todo = findById(id);
        todo.update(request.getTitle(),
                    request.getDescription(),
                    request.getDone());
        return todoRepository.save(todo);
    }

    public void delete(Long id) {
        todoRepository.deleteById(id);
    }
}

@Service
class TodoService(
    private val todoRepository: TodoRepository
) {

    @Transactional(readOnly = true)
    fun findAll(): List<Todo> = todoRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))

    @Transactional(readOnly = true)
    fun findById(id: Long): Todo {
        return todoRepository.findByIdOrNull(id)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
    }

    @Transactional
    fun create(request: TodoRequest?): Todo {
        // null check
        checkNotNull(request) { "TodoRequest is null" }

        val todo = Todo(
            title = request.title,
            description = request.description,
            done = request.done,
            createdAt = LocalDateTime.now(),
        )

        return todoRepository.save(todo)
    }

    @Transactional
    fun update(id: Long, request: TodoRequest?): Todo {
        checkNotNull(request) { "TodoRequest is null" }

        return findById(id).let {
            it.update(request.title, request.description, request.done)
            todoRepository.save(it)
        }
    }

    @Transactional
    fun delete(id: Long) {
        todoRepository.deleteById(id)
    }
}
```

## Domain Layer(entity, repository) Refactoring(Java -> Kotlin)
```
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "done")
    private Boolean done;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(String title, String description, Boolean done) {
        this.title = title;
        this.description = description;
        this.done = done != null && done;
        this.updatedAt = LocalDateTime.now();
    }
}

package com.javatokotlinspring.todo.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "todos")
class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0,

    @Column(name = "title")
    var title: String,

    @Lob
    @Column(name = "description")
    var description: String,

    @Column(name = "done")
    var done: Boolean,

    @Column(name = "created_at")
    var createdAt: LocalDateTime,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,
) {
    fun update(title: String, description: String, done: Boolean) {
        this.title = title
        this.description = description
        this.done = done
    }
}


public interface TodoRepository extends JpaRepository<Todo, Long> {
    Optional<List<Todo>> findAllByDoneIsFalseOrderByIdDesc();
}

interface TodoRepository : JpaRepository<Todo, Long> {
    fun findAllByDoneIsFalseOrderByIdDesc(): List<Todo>?
}
```
