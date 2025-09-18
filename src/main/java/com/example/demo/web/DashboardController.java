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

        // Postgres — pełna lista (bez limitu), posortowana po ID dla stabilnego widoku
        List<EmployeeEntity> employees = pgRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(EmployeeEntity::getEmployeeId))
                .toList();

        // Neo4j — pełna tabela (bez limitu) zgodnie z projekcją NeoEmployeeRow
        List<NeoEmployeeRow> graphRows = neoRepo.findEmployeesTable();

        model.addAttribute("employees", employees);
        model.addAttribute("pgCount", employees.size());

        model.addAttribute("graphRows", graphRows);
        model.addAttribute("neoCount", graphRows.size());

        return "dashboard";
    }

    // --- Przyciski akcji w środku (wspólne miejsce) ---
    // Na razie proste odświeżenie; Add/Edit/Delete najlepiej obsłużyć POST-em z formularzy.
    @GetMapping("/actions/refresh")
    public String refresh() {
        return "redirect:/dashboard";
    }
}
