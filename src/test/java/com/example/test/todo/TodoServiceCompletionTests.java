package com.example.test.todo;

import com.example.test.kafka.TodoCompletedEvent;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.test.security.User;
import com.example.test.security.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class TodoServiceCompletionTests {

    @Test
    void publishesOnlyWhenTodoFirstBecomesComplete() {
        TodoRepository todos = mock(TodoRepository.class);
        UserRepository users = mock(UserRepository.class);
        ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
        User owner = new User("alice", "hash", "alice@example.com", null);
        Todo todo = new Todo("Buy milk", "Oat milk", owner);
        when(todos.findByIdAndOwner_Username(7L, "alice")).thenReturn(Optional.of(todo));
        when(todos.save(todo)).thenReturn(todo);
        TodoService service = new TodoService(todos, users, events);

        service.setCompletion("alice", 7L, true);
        service.setCompletion("alice", 7L, true);
        service.setCompletion("alice", 7L, false);

        verify(events).publishEvent(new TodoCompletedEvent(7L, "Buy milk", "Oat milk", "alice"));
    }
}
