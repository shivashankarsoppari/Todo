package com.example.test.todo;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TodoService {

    private final TodoRepository repository;

    public TodoService(TodoRepository repository) {
        this.repository = repository;
    }

    public List<Todo> listTodos() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Todo getTodo(Long id) {
        return findTodo(id);
    }

    public Todo createTodo(String title, String description) {
        validate(title, description);
        return repository.save(new Todo(title.trim(), normalizeDescription(description)));
    }

    public Todo updateTodo(Long id, String title, String description) {
        validate(title, description);
        Todo todo = findTodo(id);
        todo.setTitle(title.trim());
        todo.setDescription(normalizeDescription(description));
        return repository.save(todo);
    }

    public Todo setCompletion(Long id, boolean completed) {
        Todo todo = findTodo(id);
        todo.setCompleted(completed);
        return repository.save(todo);
    }

    public void deleteTodo(Long id) {
        repository.delete(findTodo(id));
    }

    private Todo findTodo(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo not found"));
    }

    private void validate(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Todo title must not be blank");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("Todo title must be at most 200 characters");
        }
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Todo description must be at most 2000 characters");
        }
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
