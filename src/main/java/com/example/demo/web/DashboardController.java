package com.example.demo.web;

import com.example.demo.pg.EmployeeRepository;
import com.example.demo.graph.NeoEmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;

    public DashboardController(EmployeeRepository pgRepo, NeoEmployeeRepository neoRepo) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        var employees = pgRepo.findAll();           // Postgres (lewa tabela)
        var graphRows = neoRepo.findEmployeesTable(); // Neo4j (prawa tabela, bez limitu)

        model.addAttribute("employees", employees);
        model.addAttribute("graphRows", graphRows);
        model.addAttribute("pgCount", employees.size());
        model.addAttribute("neoCount", graphRows.size());

        return "dashboard";
    }
}
