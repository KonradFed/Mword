package com.example.demo.config;

import org.neo4j.driver.Driver;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager; // <-- poprawny import
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.example.demo.employee")
@EnableJpaRepositories(basePackages = "com.example.demo.employee")
@EnableNeo4jRepositories(basePackages = "com.example.demo.graph")
public class PersistenceConfig {

  // JPA (Postgres)
  @Bean(name = "transactionManager")
  public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }

  // Neo4j
  @Bean(name = "neo4jTransactionManager")
  public Neo4jTransactionManager neo4jTransactionManager(
      Driver driver, DatabaseSelectionProvider databaseSelectionProvider) {
    return new Neo4jTransactionManager(driver, databaseSelectionProvider);
  }
}
