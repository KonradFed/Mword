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

    /* Lista do tabeli – bez job_id, z nazwą działu (departments.name) */
    @Query(value = """
            SELECT
              e.employee_id   AS employeeId,
              e.first_name    AS firstName,
              e.last_name     AS lastName,
              e.email         AS email,
              e.phone         AS phone,
              e.hire_date     AS hireDate,
              e.department_id AS departmentId,
              d.name          AS departmentName
            FROM employees e
            LEFT JOIN departments d ON d.department_id = e.department_id
            ORDER BY e.employee_id
            OFFSET :offset
            LIMIT :limit
            """, nativeQuery = true)
    List<PgEmployeeRow> page(@Param("offset") int offset, @Param("limit") int limit);

    /* Departamenty do selecta */
    @Query(value = """
            SELECT d.department_id AS departmentId,
                   d.name          AS departmentName
            FROM departments d
            ORDER BY d.name
            """, nativeQuery = true)
    List<PgDepartmentRow> listDepartments();

    /* Info o dziale (fallback do Neo4j, jeśli nie podasz nazwy/lokalizacji) */
    @Query(value = """
            SELECT d.name AS name, d.location AS location
            FROM departments d
            WHERE d.department_id = :deptId
            """, nativeQuery = true)
    EmployeeRepository.PgDeptInfo getDeptInfo(@Param("deptId") Long deptId);

    /* === JOBS: tworzenie stanowiska z auto-ID (tylko gdy coś podasz) === */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO jobs (title, min_salary, max_salary)
            VALUES (:title,
                    CAST(:minSalary AS numeric),
                    CAST(:maxSalary AS numeric))
            """, nativeQuery = true)
    int insertJobAutoId(
            @Param("title") String title,
            @Param("minSalary") Long minSalary,
            @Param("maxSalary") Long maxSalary
    );

    @Query(value = "SELECT currval('jobs_job_id_seq')", nativeQuery = true)
    Long getLastJobIdFromSequence();

    /* === EMPLOYEES: insert bez employee_id (auto) === */
    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO employees
            (first_name, last_name, email, phone, hire_date, job_id, department_id)
            VALUES (:firstName, :lastName, :email, :phone, :hireDate, :jobId, :departmentId)
            """, nativeQuery = true)
    int insertEmployeeAutoId(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("hireDate") LocalDate hireDate,
            @Param("jobId") Integer jobId,          // może być NULL
            @Param("departmentId") Long departmentId
    );

    @Query(value = "SELECT currval('employees_employee_id_seq')", nativeQuery = true)
    Long getLastEmployeeIdFromSequence();

    /* Pomocnicze projekcje */
    interface PgDeptInfo { String getName(); String getLocation(); }
}
