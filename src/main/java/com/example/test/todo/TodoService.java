package com.example.test.todo;

import com.example.test.security.User;
import com.example.test.security.UserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TodoService {

    private final TodoRepository repository;
    private final UserRepository userRepository;

    public TodoService(TodoRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<Todo> listTodos(String username) {
        return repository.findAllByOwner_Username(username, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Todo getTodo(String username, Long id) {
        return findTodo(username, id);
    }

    public Todo createTodo(String username, String title, String description) {
        validate(title, description);
        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return repository.save(new Todo(title.trim(), normalizeDescription(description), owner));
    }

    public Todo updateTodo(String username, Long id, String title, String description) {
        validate(title, description);
        Todo todo = findTodo(username, id);
        todo.setTitle(title.trim());
        todo.setDescription(normalizeDescription(description));
        return repository.save(todo);
    }

    public Todo setCompletion(String username, Long id, boolean completed) {
        Todo todo = findTodo(username, id);
        todo.setCompleted(completed);
        return repository.save(todo);
    }

    public void deleteTodo(String username, Long id) {
        repository.delete(findTodo(username, id));
    }

    private Todo findTodo(String username, Long id) {
        return repository.findByIdAndOwner_Username(id, username)
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
