package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.Optional;

public interface DepartmentNeoRepository extends Neo4jRepository<DepartmentNode, Integer> {
  Optional<DepartmentNode> findByName(String name);
}
