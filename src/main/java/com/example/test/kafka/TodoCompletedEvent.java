package com.example.test.kafka;

public record TodoCompletedEvent(Long todoId, String title, String description, String completedBy) {
}
