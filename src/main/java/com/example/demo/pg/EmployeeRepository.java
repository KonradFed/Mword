package com.example.demo.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

    @Query(value = "SELECT COUNT(*) FROM employees", nativeQuery = true)
    long countAll();

    /* Strona do tabeli (PG) */
    @Query(value = """
            SELECT
              e.employee_id   AS employeeId,
              e.first_name    AS firstName,
              e.last_name     AS lastName,
              e.email         AS email,
              e.phone         AS phone,
              e.hire_date     AS hireDate,
              e.department_id AS departmentId,
              d.name          AS departmentName,
              d.location      AS location,
              e.job_id        AS jobId,
              j.title         AS jobTitle,
              j.min_salary    AS jobMinSalary,
              j.max_salary    AS jobMaxSalary
            FROM employees e
            LEFT JOIN departments d ON d.department_id = e.department_id
            LEFT JOIN jobs j        ON j.job_id        = e.job_id
            ORDER BY e.employee_id
            OFFSET :offset
            LIMIT :limit
            """, nativeQuery = true)
    List<PgEmployeeRow> page(@Param("offset") int offset, @Param("limit") int limit);

    /* Jeden rekord do edycji */
    @Query(value = """
            SELECT
              e.employee_id   AS employeeId,
              e.first_name    AS firstName,
              e.last_name     AS lastName,
              e.email         AS email,
              e.phone         AS phone,
              e.hire_date     AS hireDate,
              e.department_id AS departmentId,
              d.name          AS departmentName,
              d.location      AS location,
              e.job_id        AS jobId,
              j.title         AS jobTitle,
              j.min_salary    AS jobMinSalary,
              j.max_salary    AS jobMaxSalary
            FROM employees e
            LEFT JOIN departments d ON d.department_id = e.department_id
            LEFT JOIN jobs j        ON j.job_id        = e.job_id
            WHERE e.employee_id = :id
            """, nativeQuery = true)
    PgEmployeeRow findRowById(@Param("id") Long id);

    /* Departamenty */
    @Query(value = """
            SELECT d.department_id AS departmentId,
                   d.name          AS departmentName,
                   d.location      AS location
            FROM departments d
            ORDER BY d.name
            """, nativeQuery = true)
    List<PgDepartmentRow> listDepartments();

    /* Jobs + lista użytych departamentów (do filtrowania w UI) */
    @Query(value = """
            SELECT
              j.job_id AS jobId,
              j.title  AS title,
              COALESCE(STRING_AGG(DISTINCT e.department_id::text, ','), '') AS deptIds
            FROM jobs j
            LEFT JOIN employees e ON e.job_id = j.job_id
            GROUP BY j.job_id, j.title
            ORDER BY j.title
            """, nativeQuery = true)
    List<PgJobWithDeptsRow> listJobsWithDeptsAgg();

    /* Pojedynczy job (dla Neo4j tytuł/min/max) */
    @Query(value = """
            SELECT
              j.job_id      AS jobId,
              j.title       AS title,
              j.min_salary  AS minSalary,
              j.max_salary  AS maxSalary
            FROM jobs j
            WHERE j.job_id = :id
            """, nativeQuery = true)
    PgJobRow getJobById(@Param("id") Integer id);

    /* === INSERT/UPDATE/DELETE === */
    @Transactional @Modifying
    @Query(value = """
            INSERT INTO jobs (title, min_salary, max_salary)
            VALUES (:title,
                    CAST(:minSalary AS numeric),
                    CAST(:maxSalary AS numeric))
            """, nativeQuery = true)
    int insertJobAutoId(@Param("title") String title,
                        @Param("minSalary") Long minSalary,
                        @Param("maxSalary") Long maxSalary);

    @Query(value = "SELECT currval('jobs_job_id_seq')", nativeQuery = true)
    Long getLastJobIdFromSequence();

    @Transactional @Modifying
    @Query(value = """
            INSERT INTO employees
            (first_name, last_name, email, phone, hire_date, job_id, department_id)
            VALUES (:firstName, :lastName, :email, :phone, :hireDate, :jobId, :departmentId)
            """, nativeQuery = true)
    int insertEmployeeAutoId(@Param("firstName") String firstName,
                             @Param("lastName") String lastName,
                             @Param("email") String email,
                             @Param("phone") String phone,
                             @Param("hireDate") LocalDate hireDate,
                             @Param("jobId") Integer jobId,
                             @Param("departmentId") Long departmentId);

    @Query(value = "SELECT currval('employees_employee_id_seq')", nativeQuery = true)
    Long getLastEmployeeIdFromSequence();

    @Transactional @Modifying
    @Query(value = """
            UPDATE employees
            SET first_name = :firstName,
                last_name  = :lastName,
                email      = :email,
                phone      = :phone,
                hire_date  = :hireDate,
                department_id = :departmentId
            WHERE employee_id = :id
            """, nativeQuery = true)
    int updateEmployee(@Param("id") Long id,
                       @Param("firstName") String firstName,
                       @Param("lastName") String lastName,
                       @Param("email") String email,
                       @Param("phone") String phone,
                       @Param("hireDate") LocalDate hireDate,
                       @Param("departmentId") Long departmentId);

    @Transactional @Modifying
    @Query(value = "UPDATE employees SET job_id = :jobId WHERE employee_id = :id", nativeQuery = true)
    int setEmployeeJob(@Param("id") Long id, @Param("jobId") Integer jobId);

    @Transactional @Modifying
    @Query(value = "DELETE FROM employees WHERE employee_id = :id", nativeQuery = true)
    int deleteEmployee(@Param("id") Long id);

    /* Projekcje */
    interface PgDeptInfo { String getName(); String getLocation(); }
    interface PgJobRow   { Integer getJobId(); String getTitle(); Long getMinSalary(); Long getMaxSalary(); }
}
