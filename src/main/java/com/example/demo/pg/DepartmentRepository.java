package com.example.demo.pg;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;

/** Repo tylko do pobrania listy działów do selecta. */
public interface DepartmentRepository extends Repository<EmployeeEntity, Long> {

    @Query(value = """
        SELECT d.department_id AS departmentId,
               d.name          AS departmentName
        FROM departments d
        ORDER BY d.name
        """, nativeQuery = true)
    List<PgDepartmentRow> listAllForSelect();
}
