package com.example.demo.hrms;

import com.example.demo.employee.Employee;
import com.example.demo.employee.EmployeeRepository;
import com.example.demo.graph.Department;
import com.example.demo.graph.DepartmentRepository;
import com.example.demo.graph.PersonNode;
import com.example.demo.graph.PersonRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class PeopleService {

  private final EmployeeRepository empRepo;
  private final DepartmentRepository depRepo;
  private final PersonRepository personRepo;

  public PeopleService(EmployeeRepository empRepo,
                       DepartmentRepository depRepo,
                       PersonRepository personRepo) {
    this.empRepo = empRepo;
    this.depRepo = depRepo;
    this.personRepo = personRepo;
  }

  @Transactional
  public void createPersonInBoth(String first, String last, String email, String deptName) {
    // --- Postgres ---
    Employee e = new Employee();
    e.setFirstName(first);
    e.setLastName(last);
    e.setEmail(email);
    e.setHireDate(LocalDate.now());
    empRepo.save(e);

    // --- Neo4j ---
    Department dept = depRepo.findByName(deptName)
        .orElseGet(() -> {
            Department newDept = new Department();
            newDept.setName(deptName);
            newDept.setLocation("Default Location");
            return depRepo.save(newDept);
        });

    PersonNode p = new PersonNode(first, last);
    p.addDepartment(dept);
    personRepo.save(p);

    System.out.println("✅ Saved " + first + " " + last + " in both Postgres and Neo4j.");
  }
}
