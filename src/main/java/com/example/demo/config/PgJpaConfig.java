package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.pg",
        transactionManagerRef = "transactionManager"   // <- korzysta z JPA TM z JpaTxConfig
)
public class PgJpaConfig {
    // UWAGA: tutaj NIE definiujemy żadnego @Bean transactionManager,
    // żeby nie dublować nazwy z JpaTxConfig.
}
