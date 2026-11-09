package com.example.test;

//package com.example.todo.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

public class ConfigDebugInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {

        Environment env = context.getEnvironment();

        System.out.println("========== CONFIG DEBUG ==========");

        System.out.println("Active Profile: "
                + env.getProperty("spring.profiles.active"));

        System.out.println("DB URL: "
                + env.getProperty("DB_URL"));

        System.out.println("DB Username: "
                + env.getProperty("DB_USERNAME"));

        System.out.println("DB Password Present: "
                + (env.getProperty("DB_PASSWORD") != null));

        System.out.println("Oracle Password Present: "
                + (env.getProperty("ORACLE_PASSWORD") != null));

        System.out.println("==================================");
    }
}
