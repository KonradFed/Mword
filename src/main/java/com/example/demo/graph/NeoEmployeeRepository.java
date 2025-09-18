package com.example.demo.graph;

import java.util.List;
import java.util.Map;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik)
        OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
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
        ORDER BY employeeId ASC
        LIMIT 20
    """)
    List<NeoEmployeeRow> findEmployeesTable();

    // Raw map for JSON and Thymeleaf: single literal map per record
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik)-[:NA_STANOWISKU]->(j:Stanowisko)
        OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
        OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
        RETURN {
          employeeId:        p.`employee_id`,
          firstName:         p.`imię`,
          lastName:          p.`nazwisko`,
          email:             p.`email`,
          phone:             p.`telefon`,
          hireDate:          p.`data_zatrudnienia`,
          title:             j.`tytuł`,
          minSalary:         j.`min_pensja`,
          maxSalary:         j.`max_pensja`,
          departmentName:    d.`nazwa`,
          location:          d.`lokalizacja`,
          amount:            s.`kwota`,
          fromDate:          s.`od`
        } AS row
        ORDER BY row.employeeId ASC
        LIMIT 20
    """)
    List<Map<String, Object>> findEmployeesTableAsMap();
}
