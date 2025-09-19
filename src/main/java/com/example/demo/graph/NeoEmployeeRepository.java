package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository
public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    /* Liczba pracowników (do paginacji w dashboardzie) */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("MATCH (p:Pracownik) RETURN count(p)")
    long countPersons();

    /* Strona wyników – ZWRACAJ MAPĘ 'row' (zgodnie z tym, co czyta Thymeleaf) */
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
        ORDER BY row.employeeId
        SKIP $skip
        LIMIT $limit
        """)
    List<Map<String,Object>> pageAsRowMap(@Param("skip") long skip,
                                          @Param("limit") long limit);

    /* Jeden rekord do edycji (mapa) */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik {`employee_id`:$id})
        OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
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
    Map<String,Object> findOneRowMapByEmployeeId(@Param("id") long id);

    /* UPSERT pełnego zestawu właściwości/relacji */
    @Transactional(value = "neo4jTransactionManager")
    @Query("""
        MERGE (p:Pracownik {`employee_id`: $id})
        SET p += $pProps
        WITH p, $jProps AS jProps, $dProps AS dProps, $sProps AS sProps
        FOREACH(_ IN CASE WHEN jProps IS NULL THEN [] ELSE [1] END |
          MERGE (j:Stanowisko {`tytuł`: coalesce(jProps.`tytuł`,'Unknown')})
          SET j.`min_pensja` = jProps.`min_pensja`, j.`max_pensja` = jProps.`max_pensja`
          MERGE (p)-[:NA_STANOWISKU]->(j)
        )
        FOREACH(_ IN CASE WHEN dProps IS NULL THEN [] ELSE [1] END |
          MERGE (d:`Dział` {`nazwa`: coalesce(dProps.`nazwa`,'Unknown')})
          SET d.`lokalizacja` = dProps.`lokalizacja`
          MERGE (p)-[:PRACUJE_W_DZIALE]->(d)
        )
        FOREACH(_ IN CASE WHEN sProps IS NULL THEN [] ELSE [1] END |
          MERGE (s:Wynagrodzenie {`kwota`: sProps.`kwota`, `od`: sProps.`od`})
          MERGE (p)-[:MA_WYNAGRODZENIE]->(s)
        )
        """)
    void upsertFull(@Param("id") long id,
                    @Param("pProps") Map<String,Object> pProps,
                    @Param("jProps") Map<String,Object> jProps,
                    @Param("dProps") Map<String,Object> dProps,
                    @Param("sProps") Map<String,Object> sProps);

    /* UPDATE (różna nazwa, ale logika jak upsert – przydaje się do edycji) */
    @Transactional(value = "neo4jTransactionManager")
    @Query("""
        MATCH (p:Pracownik {`employee_id`:$id})
        SET p += $pProps
        WITH p, $jProps AS jProps, $dProps AS dProps, $sProps AS sProps
        FOREACH(_ IN CASE WHEN jProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:NA_STANOWISKU]->(j:Stanowisko {`tytuł`: coalesce(jProps.`tytuł`,'Unknown')})
          SET j.`min_pensja` = jProps.`min_pensja`, j.`max_pensja` = jProps.`max_pensja`
        )
        FOREACH(_ IN CASE WHEN dProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:PRACUJE_W_DZIALE]->(d:`Dział` {`nazwa`: coalesce(dProps.`nazwa`,'Unknown')})
          SET d.`lokalizacja` = dProps.`lokalizacja`
        )
        FOREACH(_ IN CASE WHEN sProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie {`kwota`: sProps.`kwota`, `od`: sProps.`od`})
        )
        """)
    void updateFull(@Param("id") long id,
                    @Param("pProps") Map<String,Object> pProps,
                    @Param("jProps") Map<String,Object> jProps,
                    @Param("dProps") Map<String,Object> dProps,
                    @Param("sProps") Map<String,Object> sProps);

    /* DELETE po employee_id */
    @Transactional(value = "neo4jTransactionManager")
    @Query("MATCH (p:Pracownik {`employee_id`:$id}) DETACH DELETE p")
    void deleteByEmployeeId(@Param("id") long id);
}
