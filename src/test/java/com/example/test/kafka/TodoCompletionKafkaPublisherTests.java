package com.example.test.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class TodoCompletionKafkaPublisherTests {

    @Test
    void publishesTodoCompletionEventToConfiguredTopic() {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, TodoCompletedEvent> kafkaTemplate = mock(KafkaTemplate.class);
        TodoCompletedEvent event = new TodoCompletedEvent(42L, "Buy milk", "Oat milk", "alice");
        when(kafkaTemplate.send(eq("todo-completed"), eq("42"), eq(event)))
                .thenReturn(CompletableFuture.completedFuture(null));
        TodoCompletionKafkaPublisher publisher =
                new TodoCompletionKafkaPublisher(kafkaTemplate, "todo-completed");

        publisher.publish(event);

        verify(kafkaTemplate).send("todo-completed", "42", event);
    }
}
