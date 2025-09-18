package com.example.demo.graph;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Repozytorium Neo4j – etykiety i właściwości po polsku:
 * :Pracownik, :Stanowisko, :Dział, :Wynagrodzenie
 * employee_id, imię, nazwisko, email, telefon, data_zatrudnienia, ...
 */
@Repository
public interface NeoEmployeeRepository extends Neo4jRepository<PersonNode, Long> {

    /* ===== licznik do badge/paginacji ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("MATCH (p:Pracownik) RETURN count(p)")
    long countPersons();

    /* ===== strona wyników – mapy pod kluczem 'row' (spójne z dashboard.html) ===== */
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

    /* alias zgodności, gdyby ktoś wołał page(...) */
    default List<Map<String, Object>> page(int skip, int limit) {
        return pageAsRowMap(skip, limit);
    }

    /* ===== pojedynczy rekord (płaska mapa 'item' – dla ewentualnych potrzeb) ===== */
    @Transactional(value = "neo4jTransactionManager", readOnly = true)
    @Query("""
        MATCH (p:Pracownik { `employee_id`: $employeeId })
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
        } AS item
    """)
    Map<String, Object> findFlatByEmployeeId(@Param("employeeId") Long employeeId);

    /* ===== upsert węzła :Pracownik po employee_id (ustawia tylko przekazane pola) ===== */
    @Transactional("neo4jTransactionManager")
    @Query("""
        MERGE (p:Pracownik { `employee_id`: $employeeId })
        SET p += $props
        RETURN p
    """)
    PersonNode upsertByEmployeeId(@Param("employeeId") Long employeeId,
                                  @Param("props") Map<String, Object> props);

    /* ===== delete po employee_id ===== */
    @Transactional("neo4jTransactionManager")
    @Query("""
        MATCH (p:Pracownik { `employee_id`: $employeeId })
        DETACH DELETE p
        RETURN count(*) as deleted
    """)
    long deleteByEmployeeId(@Param("employeeId") Long employeeId);
}
