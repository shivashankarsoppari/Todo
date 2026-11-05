package com.example.test.kafka;

import com.example.test.security.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TodoCompletionEmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TodoCompletionEmailConsumer.class);

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final String fromAddress;

    public TodoCompletionEmailConsumer(
            JavaMailSender mailSender,
            UserRepository userRepository,
            @Value("${app.mail.from:}") String fromAddress) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.fromAddress = fromAddress;
    }

    @KafkaListener(
            topics = "${app.kafka.todo-completed-topic:todo-completed}",
            groupId = "${spring.kafka.consumer.group-id:todo-completed-group}")
    public void sendCompletionEmail(
            TodoCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        if (!StringUtils.hasText(fromAddress)) {
            throw new IllegalStateException("Configure MAIL_FROM before sending email");
        }

        String recipientEmail = userRepository.findByUsername(event.completedBy())
                .map(user -> user.getEmail())
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalStateException(
                        "No email address found for user " + event.completedBy()));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Todo completed: " + event.title());
        message.setText("Hello " + event.completedBy() + ",\n\n"
                + "Todo #" + event.todoId() + " \"" + event.title() + "\" has been marked complete.\n"
                + (StringUtils.hasText(event.description())
                        ? "\nDescription: " + event.description() + "\n"
                        : ""));
        mailSender.send(message);

        logger.info("Sent completion notification for todo {} from Kafka topic {}",
                event.todoId(), topic);
    }
}
