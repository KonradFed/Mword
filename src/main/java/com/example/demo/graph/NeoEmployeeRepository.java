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
    """)
    List<Map<String, Object>> findEmployeesTable();
}
