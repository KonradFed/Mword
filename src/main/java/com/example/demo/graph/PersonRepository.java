package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;

public interface PersonRepository extends Neo4jRepository<PersonNode, Integer> {

  @Query("""
    MATCH (p:Pracownik)
    OPTIONAL MATCH (p)-[]->(d:`Dział`)
    RETURN  p.employee_id AS id,
            p.`imię`      AS firstName,
            p.nazwisko    AS lastName,
            p.email       AS email,
            [x IN collect(d) | coalesce(x.nazwa, x.name)] AS departments
    ORDER BY id
  """)
  List<PersonProjection> findPeopleWithDepartments();
}
