package com.example.demo.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

  @Query("""
         select e
         from Employee e
         left join fetch e.department
         left join fetch e.job
         order by e.id
         """)
  List<Employee> findAllWithDepartmentAndJob();
}
