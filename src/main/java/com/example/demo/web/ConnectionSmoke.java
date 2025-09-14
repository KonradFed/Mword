package com.example.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;

@Configuration
public class ConnectionSmoke {

    @Bean
    CommandLineRunner testConnections(JdbcTemplate jdbc, Neo4jClient neo4j) {
        return args -> {
            try {
                Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
                System.out.println("✅ PostgreSQL connection OK, SELECT 1 -> " + one);
            } catch (Exception ex) {
                System.err.println("❌ PostgreSQL connection FAILED: " + ex.getMessage());
            }

            try {
                Integer neo = neo4j.query("RETURN 1 AS ok")
                                   .fetchAs(Integer.class)
                                   .one()
                                   .orElse(0);
                System.out.println("✅ Neo4j connection OK, RETURN 1 -> " + neo);
            } catch (Exception ex) {
                System.err.println("❌ Neo4j connection FAILED: " + ex.getMessage());
            }
        };
    }
}
