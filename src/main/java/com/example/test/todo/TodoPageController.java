package com.example.test.todo;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TodoPageController {

    private final Environment environment;

    public TodoPageController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/")
    public String home() {
        return "forward:/todos.html";
    }

    @GetMapping("/api/environment")
    @ResponseBody
    public EnvironmentInfo environmentInfo() {
        String[] activeProfiles = environment.getActiveProfiles();
        String name = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : String.join(", ", environment.getDefaultProfiles());
        return new EnvironmentInfo(name);
    }

    public record EnvironmentInfo(String name) {
    }
}
