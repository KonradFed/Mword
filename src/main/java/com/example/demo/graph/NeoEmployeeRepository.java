package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    /* ===== COUNT ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("MATCH (p:Pracownik) RETURN count(p)")
    long countPersons();

    /* ===== PAGE – zwracamy JEDNĄ wartość w rekordzie: mapę 'row' ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
      CALL {
        MATCH (p:Pracownik)
        WITH p
        ORDER BY p.`employee_id` ASC
        SKIP  coalesce($skip, 0)
        LIMIT coalesce($limit, 2147483647)
        RETURN collect(p) AS persons
      }
      UNWIND persons AS p
      OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
      OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
      OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
      WITH p, j, d, s
      RETURN {
        employeeId:       p.`employee_id`,
        firstName:        p.`imię`,
        lastName:         p.`nazwisko`,
        email:            p.`email`,
        phone:            p.`telefon`,
        hireDate:         p.`data_zatrudnienia`,
        title:            j.`tytuł`,
        minSalary:        j.`min_pensja`,
        maxSalary:        j.`max_pensja`,
        departmentName:   d.`nazwa`,
        location:         d.`lokalizacja`,
        amount:           s.`kwota`,
        fromDate:         s.`od`
      } AS row
      ORDER BY row.employeeId ASC
      """)
    List<Map<String,Object>> pageAsRowMap(@Param("skip") Integer skip,
                                          @Param("limit") Integer limit);

    /* ===== GET ONE – po employee_id (też jedna wartość: mapa 'row') ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik)
        WHERE toString(p.`employee_id`) = toString($id)
        OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
        OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
        OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
        RETURN {
          employeeId:       p.`employee_id`,
          firstName:        p.`imię`,
          lastName:         p.`nazwisko`,
          email:            p.`email`,
          phone:            p.`telefon`,
          hireDate:         p.`data_zatrudnienia`,
          title:            j.`tytuł`,
          minSalary:        j.`min_pensja`,
          maxSalary:        j.`max_pensja`,
          departmentName:   d.`nazwa`,
          location:         d.`lokalizacja`,
          amount:           s.`kwota`,
          fromDate:         s.`od`
        } AS row
        LIMIT 1
        """)
    Map<String,Object> findOneFlatByEmployeeId(@Param("id") Long id);

    /* ===== GET ONE – po email (też mapa 'row') ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik)
        WHERE toLower(p.`email`) = toLower($email)
        OPTIONAL MATCH (p)-[:NA_STANOWISKU]->(j:Stanowisko)
        OPTIONAL MATCH (p)-[:PRACUJE_W_DZIALE]->(d:`Dział`)
        OPTIONAL MATCH (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie)
        RETURN {
          employeeId:       p.`employee_id`,
          firstName:        p.`imię`,
          lastName:         p.`nazwisko`,
          email:            p.`email`,
          phone:            p.`telefon`,
          hireDate:         p.`data_zatrudnienia`,
          title:            j.`tytuł`,
          minSalary:        j.`min_pensja`,
          maxSalary:        j.`max_pensja`,
          departmentName:   d.`nazwa`,
          location:         d.`lokalizacja`,
          amount:           s.`kwota`,
          fromDate:         s.`od`
        } AS row
        LIMIT 1
        """)
    Map<String,Object> findOneFlatByEmail(@Param("email") String email);

    /* ===== UPSERT / UPDATE / DELETE (bez zmian) ===== */
    @Transactional(value = "neo4jTransactionManager")
    @Query("""
        MERGE (p:Pracownik {`employee_id`: $id})
        SET p += $pProps
        WITH p, $jProps AS jProps, $dProps AS dProps, $sProps AS sProps
        FOREACH(_ IN CASE WHEN jProps IS NULL THEN [] ELSE [1] END |
          MERGE (j:Stanowisko {`tytuł`: coalesce(jProps.`tytuł`, 'Unknown')})
          SET j.`min_pensja` = jProps.`min_pensja`,
              j.`max_pensja` = jProps.`max_pensja`
          MERGE (p)-[:NA_STANOWISKU]->(j)
        )
        FOREACH(_ IN CASE WHEN dProps IS NULL THEN [] ELSE [1] END |
          MERGE (d:`Dział` {`nazwa`: coalesce(dProps.`nazwa`, 'Unknown')})
          SET d.`lokalizacja` = dProps.`lokalizacja`
          MERGE (p)-[:PRACUJE_W_DZIALE]->(d)
        )
        FOREACH(_ IN CASE WHEN sProps IS NULL THEN [] ELSE [1] END |
          MERGE (s:Wynagrodzenie {`kwota`: sProps.`kwota`, `od`: sProps.`od`})
          MERGE (p)-[:MA_WYNAGRODZENIE]->(s)
        )
        """)
    void upsertFull(@Param("id") Long id,
                    @Param("pProps") Map<String,Object> pProps,
                    @Param("jProps") Map<String,Object> jProps,
                    @Param("dProps") Map<String,Object> dProps,
                    @Param("sProps") Map<String,Object> sProps);

    @Transactional(value = "neo4jTransactionManager")
    @Query("""
        MATCH (p:Pracownik {`employee_id`: $id})
        SET p += $pProps
        WITH p, $jProps AS jProps, $dProps AS dProps, $sProps AS sProps
        FOREACH(_ IN CASE WHEN jProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:NA_STANOWISKU]->(j:Stanowisko {`tytuł`: coalesce(jProps.`tytuł`, 'Unknown')})
          SET j.`min_pensja` = jProps.`min_pensja`,
              j.`max_pensja` = jProps.`max_pensja`
        )
        FOREACH(_ IN CASE WHEN dProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:PRACUJE_W_DZIALE]->(d:`Dział` {`nazwa`: coalesce(dProps.`nazwa`, 'Unknown')})
          SET d.`lokalizacja` = dProps.`lokalizacja`
        )
        FOREACH(_ IN CASE WHEN sProps IS NULL THEN [] ELSE [1] END |
          MERGE (p)-[:MA_WYNAGRODZENIE]->(s:Wynagrodzenie {`kwota`: sProps.`kwota`, `od`: sProps.`od`})
        )
        """)
    void updateFull(@Param("id") Long id,
                    @Param("pProps") Map<String,Object> pProps,
                    @Param("jProps") Map<String,Object> jProps,
                    @Param("dProps") Map<String,Object> dProps,
                    @Param("sProps") Map<String,Object> sProps);

    @Transactional(value = "neo4jTransactionManager")
    @Query("MATCH (p:Pracownik {`employee_id`: $id}) DETACH DELETE p")
    void deleteByEmployeeId(@Param("id") Long id);
}
