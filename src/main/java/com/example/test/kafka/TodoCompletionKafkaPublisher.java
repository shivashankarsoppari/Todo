package com.example.test.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TodoCompletionKafkaPublisher {

    private static final Logger logger = LoggerFactory.getLogger(TodoCompletionKafkaPublisher.class);

    private final KafkaTemplate<String, TodoCompletedEvent> kafkaTemplate;
    private final String topic;

    public TodoCompletionKafkaPublisher(
            KafkaTemplate<String, TodoCompletedEvent> kafkaTemplate,
            @Value("${app.kafka.todo-completed-topic:todo-completed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TodoCompletedEvent event) {
        kafkaTemplate.send(topic, event.todoId().toString(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        logger.error("Failed to publish completion event for todo {}", event.todoId(), failure);
                    } else {
                        logger.info("Published completion event for todo {} to topic {}", event.todoId(), topic);
                    }
                });
    }
}
