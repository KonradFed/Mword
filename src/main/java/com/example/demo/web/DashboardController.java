package com.example.demo.web;

import com.example.demo.hrms.PeopleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

  private final PeopleService peopleService;

  public DashboardController(PeopleService peopleService) {
    this.peopleService = peopleService;
  }

  @GetMapping("/")
  public String dashboard(Model model) {
    model.addAttribute("employees", peopleService.employeesFromPostgres());
    model.addAttribute("peopleGraph", peopleService.peopleFromNeo4j());
    return "dashboard";
  }
}
