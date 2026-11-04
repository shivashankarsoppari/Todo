package com.example.test.todo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOwner_Username(String username, Sort sort);

    Optional<Todo> findByIdAndOwner_Username(Long id, String username);
}
