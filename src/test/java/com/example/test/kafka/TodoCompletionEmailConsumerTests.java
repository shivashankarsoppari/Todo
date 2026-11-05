package com.example.test.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.test.security.User;
import com.example.test.security.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class TodoCompletionEmailConsumerTests {

    @Test
    void sendsNotificationToTheCompletingUsersEmailAddress() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User("alice", "password-hash", "alice@example.com", null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        TodoCompletionEmailConsumer consumer = new TodoCompletionEmailConsumer(
                mailSender, userRepository, "todos@example.com");

        consumer.sendCompletionEmail(
                new TodoCompletedEvent(42L, "Buy milk", "Oat milk", "alice"), "todo-completed");

        org.mockito.ArgumentCaptor<SimpleMailMessage> message =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        verify(userRepository).findByUsername("alice");
        assertThat(message.getValue().getFrom()).isEqualTo("todos@example.com");
        assertThat(message.getValue().getTo()).containsExactly("alice@example.com");
        assertThat(message.getValue().getSubject()).isEqualTo("Todo completed: Buy milk");
        assertThat(message.getValue().getText())
                .contains("Todo #42 \"Buy milk\" has been marked complete.")
                .contains("Description: Oat milk")
                .contains("Hello alice");
    }

    @Test
    void reportsMissingSenderAddressInsteadOfSendingEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        UserRepository userRepository = mock(UserRepository.class);
        TodoCompletionEmailConsumer consumer = new TodoCompletionEmailConsumer(mailSender, userRepository, "");

        assertThatThrownBy(() -> consumer.sendCompletionEmail(
                new TodoCompletedEvent(42L, "Buy milk", null, "alice"), "todo-completed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MAIL_FROM");
        verifyNoInteractions(mailSender);
        verifyNoInteractions(userRepository);
    }

    @Test
    void reportsWhenCompletingUserDoesNotHaveAnEmailAddress() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByUsername("alice"))
                .thenReturn(Optional.of(new User("alice", "password-hash", null, null)));
        TodoCompletionEmailConsumer consumer = new TodoCompletionEmailConsumer(
                mailSender, userRepository, "todos@example.com");

        assertThatThrownBy(() -> consumer.sendCompletionEmail(
                new TodoCompletedEvent(42L, "Buy milk", null, "alice"), "todo-completed"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No email address found for user alice");
        verify(userRepository).findByUsername("alice");
        verifyNoInteractions(mailSender);
    }
}
