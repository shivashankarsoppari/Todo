package com.example.test.todo;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TodoMcpConfiguration {

    @Bean
    ToolCallbackProvider todoToolCallbackProvider(TodoMcpTools todoMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(todoMcpTools)
                .build();
    }
}
