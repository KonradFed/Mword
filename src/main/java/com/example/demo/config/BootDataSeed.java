// src/main/java/com/example/demo/config/BootDataSeed.java
package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.employee.EmployeeRepository;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class BootDataSeed {

  @Bean
  CommandLineRunner seed(EmployeeRepository employees) {
    return args -> {
      // tu był seeding – zostaw puste albo usuń
    };
  }
}
