package com.example.demo.graph;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    @Query("""
        MATCH (p:Pracownik)-[:NA_STANOWISKU]->(j:Stanowisko)
        OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
        OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
        RETURN
          p.`employee_id`       AS employeeId,
          p.`imię`              AS firstName,
          p.`nazwisko`          AS lastName,
          p.`email`             AS email,
          p.`telefon`           AS phone,
          p.`data_zatrudnienia` AS hireDate,
          j.`tytuł`             AS title,
          j.`min_pensja`        AS minSalary,
          j.`max_pensja`        AS maxSalary,
          d.`nazwa`             AS departmentName,
          d.`lokalizacja`       AS location,
          s.`kwota`             AS amount,
          s.`od`                AS fromDate
        ORDER BY employeeId
        """)
    List<NeoEmployeeRow> findEmployeesTable();
}
