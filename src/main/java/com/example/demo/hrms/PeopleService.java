package com.example.demo.hrms;

import com.example.demo.employee.Employee;
import com.example.demo.employee.EmployeeRepository;
import com.example.demo.graph.PersonProjection;
import com.example.demo.graph.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PeopleService {

  private final EmployeeRepository employeeRepo;
  private final PersonRepository personNeoRepo;

  public PeopleService(EmployeeRepository employeeRepo, PersonRepository personNeoRepo) {
    this.employeeRepo = employeeRepo;
    this.personNeoRepo = personNeoRepo;
  }

  public List<Employee> employeesFromPostgres() {
    // dociąga department i job – działa w widoku bez LazyInitializationException
    return employeeRepo.findAllWithDepartmentAndJob();
  }

  public List<PersonProjection> peopleFromNeo4j() {
    return personNeoRepo.findPeopleWithDepartments();
  }
}
