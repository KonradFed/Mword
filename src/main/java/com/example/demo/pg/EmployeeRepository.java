package com.example.demo.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Repozytorium JPA + zapytania natywne pod dashboard i dual-write.
 */
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    @Query(value = "SELECT COUNT(*) FROM employees", nativeQuery = true)
    long countAll();

    @Query(value = """
            SELECT
              e.employee_id   AS employeeId,
              e.first_name    AS firstName,
              e.last_name     AS lastName,
              e.email         AS email,
              e.phone         AS phone,
              e.hire_date     AS hireDate,
              e.job_id        AS jobId,
              e.department_id AS departmentId
            FROM employees e
            ORDER BY e.employee_id
            OFFSET :offset
            LIMIT :limit
            """, nativeQuery = true)
    List<PgEmployeeRow> page(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * INSERT + RETURNING employee_id.
     * Jeśli :employeeId = null i kolumna ma domyślną sekwencję, Postgres wygeneruje ID.
     * Jeśli :employeeId jest podane i istnieje konflikt, zwróci NULL (ON CONFLICT DO NOTHING).
     */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO employees
            (employee_id, first_name, last_name, email, phone, hire_date, job_id, department_id)
            VALUES (:employeeId, :firstName, :lastName, :email, :phone, :hireDate, :jobId, :departmentId)
            ON CONFLICT (employee_id) DO NOTHING
            RETURNING employee_id
            """, nativeQuery = true)
    Long insertEmployeeReturningId(
            @Param("employeeId") Long employeeId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("hireDate") LocalDate hireDate,
            @Param("jobId") String jobId,
            @Param("departmentId") Long departmentId
    );
}
