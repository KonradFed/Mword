package com.example.demo.web;

import com.example.demo.pg.EmployeeEntity;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.graph.NeoEmployeeRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;

    public DashboardController(EmployeeRepository pgRepo, NeoEmployeeRepository neoRepo) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        // --- PostgreSQL ---
        List<EmployeeEntity> pgRows = pgRepo.findAll()
                .stream()
                .sorted(Comparator.comparingInt(e -> e.getEmployeeId())) // porządek po ID
                .toList();
        model.addAttribute("pgRows", pgRows);
        model.addAttribute("pgCount", pgRows.size());

        // --- Neo4j ---
        // Oczekujemy projekcji NeoEmployeeRow z polami: employeeId, firstName, lastName, email, phone, hireDate,
        // title, minSalary, maxSalary, departmentName, location, amount, fromDate
        List<Map<String, Object>> neoRows = neoRepo.findEmployeesTable();
        model.addAttribute("neoRows", neoRows);
        model.addAttribute("neoCount", neoRows.size());

        return "dashboard";
    }
}
