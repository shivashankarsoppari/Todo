package com.example.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestApplication {

	public static void main(String[] args) {
//		SpringApplication.run(TestApplication.class, args);
		SpringApplication application =
				new SpringApplication(TestApplication.class);

		application.addInitializers(new ConfigDebugInitializer());

		application.run(args);
	}

}
