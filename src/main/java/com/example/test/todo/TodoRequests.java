package com.example.test.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class TodoRequests {

    private TodoRequests() {
    }

    public record SaveTodo(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 2000) String description) {
    }

    public record SetCompletion(@NotNull Boolean completed) {
    }
}
