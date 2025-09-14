package com.example.demo.web;

import com.example.demo.employee.EmployeeRepository;
import com.example.demo.graph.PersonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.demo.graph.DepartmentRepository;

@Controller
public class DashboardController {

  private final EmployeeRepository employees;
  private final DepartmentRepository departments;
  // (opcjonalnie Neo4j) private final PersonRepository people;

  public DashboardController(EmployeeRepository employees,
                             DepartmentRepository departments) {
    this.employees = employees;
    this.departments = departments;
  }

  @GetMapping({"/","/dashboard"})
  public String dashboard(Model model) {
    model.addAttribute("employees", employees.findAll());      // z Postgresa
    model.addAttribute("departments", departments.findAll());  // z Postgresa
    // model.addAttribute("people", people.findAll());          // z Neo4j, jeśli używasz
    return "dashboard";
  }
}
