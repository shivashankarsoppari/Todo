package com.example.test.todo;

import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TodoMcpTools {

    private final TodoService todoService;

    public TodoMcpTools(TodoService todoService) {
        this.todoService = todoService;
    }

    @Tool(description = "List all todo tasks, newest first.")
    public List<Todo> listTodos() {
        return todoService.listTodos(currentUsername());
    }

    @Tool(description = "Get one todo task by its ID.")
    public Todo getTodo(@ToolParam(description = "The todo ID") Long id) {
        return todoService.getTodo(currentUsername(), id);
    }

    @Tool(description = "Create a todo task.")
    public Todo createTodo(
            @ToolParam(description = "Task title, up to 200 characters") String title,
            @ToolParam(required = false, description = "Optional task description, up to 2000 characters")
            String description) {
        return todoService.createTodo(currentUsername(), title, description);
    }

    @Tool(description = "Update a todo task's title and description.")
    public Todo updateTodo(
            @ToolParam(description = "The todo ID") Long id,
            @ToolParam(description = "New task title, up to 200 characters") String title,
            @ToolParam(required = false, description = "New optional description, up to 2000 characters")
            String description) {
        return todoService.updateTodo(currentUsername(), id, title, description);
    }

    @Tool(description = "Mark a todo task completed or active.")
    public Todo setTodoCompletion(
            @ToolParam(description = "The todo ID") Long id,
            @ToolParam(description = "True to complete the task, false to reactivate it") boolean completed) {
        return todoService.setCompletion(currentUsername(), id, completed);
    }

    @Tool(description = "Delete a todo task by its ID.")
    public String deleteTodo(@ToolParam(description = "The todo ID") Long id) {
        todoService.deleteTodo(currentUsername(), id);
        return "Todo " + id + " deleted";
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return authentication.getName();
    }
}
