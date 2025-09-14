package com.example.demo.graph;
import org.springframework.data.neo4j.repository.Neo4jRepository;
public interface PersonRepository extends Neo4jRepository<PersonNode, Long> {}
