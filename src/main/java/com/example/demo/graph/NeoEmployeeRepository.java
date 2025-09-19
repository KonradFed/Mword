package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Neo4j – etykiety/właściwości po polsku:
 * :Pracownik, :Stanowisko, :Dział, :Wynagrodzenie
 * employee_id, imię, nazwisko, email, telefon, data_zatrudnienia, ...
 */
@Repository
public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("MATCH (p:Pracownik) RETURN count(p)")
    long countPersons();

    /** Strona wyników do tabeli – zwracamy listę map {row:{...}} */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik)
        OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
        OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
        OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
        WITH {
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
        RETURN row
        ORDER BY row.employeeId ASC
        SKIP $skip LIMIT $limit
    """)
    List<Map<String, Object>> pageAsRowMap(@Param("skip") int skip, @Param("limit") int limit);

    /** alias zgodności – gdyby coś w projekcie wołało page(...) */
    default List<Map<String, Object>> page(int skip, int limit) {
        return pageAsRowMap(skip, limit);
    }

    /** Upsert węzła :Pracownik + warunkowe relacje (jeśli podano pola) */
    @Transactional("neo4jTransactionManager")
    @Query("""
        MERGE (p:Pracownik { `employee_id`: $employeeId })
        SET p += $p
        FOREACH (_ IN CASE WHEN $j IS NULL THEN [] ELSE [1] END |
          MERGE (job:Stanowisko { `tytuł`: $j.`tytuł` })
          SET job.`min_pensja` = $j.`min_pensja`,
              job.`max_pensja` = $j.`max_pensja`
          MERGE (p)-[:NA_STANOWISKU]->(job)
        )
        FOREACH (_ IN CASE WHEN $d IS NULL THEN [] ELSE [1] END |
          MERGE (dep:`Dział` { `nazwa`: $d.`nazwa` })
          SET dep.`lokalizacja` = $d.`lokalizacja`
          MERGE (p)-[:PRACUJE_W_DZIALE]->(dep)
        )
        FOREACH (_ IN CASE WHEN $s IS NULL THEN [] ELSE [1] END |
          MERGE (sal:Wynagrodzenie { `od`: $s.`od`, `kwota`: $s.`kwota` })
          MERGE (p)-[:MA_WYNAGRODZENIE]->(sal)
        )
        RETURN p
    """)
    PersonNode upsertFull(@Param("employeeId") Long employeeId,
                          @Param("p") Map<String,Object> personProps,
                          @Param("j") Map<String,Object> jobProps,
                          @Param("d") Map<String,Object> deptProps,
                          @Param("s") Map<String,Object> salaryProps);

    @Transactional("neo4jTransactionManager")
    @Query("""
        MATCH (p:Pracownik { `employee_id`: $employeeId })
        DETACH DELETE p
        RETURN count(*) as deleted
    """)
    long deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
