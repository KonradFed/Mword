package com.example.demo.pg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Integer> {

    @Query(value = """
        SELECT 
            e.employee_id        AS employeeId,
            e.first_name         AS firstName,
            e.last_name          AS lastName,
            e.email              AS email,
            e.phone              AS phone,
            d.name               AS department,
            j.title              AS jobTitle,
            s.amount             AS salary
        FROM employees e
        LEFT JOIN departments d ON d.department_id = e.department_id
        LEFT JOIN jobs        j ON j.job_id        = e.job_id
        LEFT JOIN LATERAL (
            SELECT amount 
            FROM salaries s 
            WHERE s.employee_id = e.employee_id
            ORDER BY s.from_date DESC NULLS LAST 
            LIMIT 1
        ) s ON TRUE
        ORDER BY e.employee_id
        """, nativeQuery = true)
    List<PgEmployeeRow> findEmployeesTable();
}
