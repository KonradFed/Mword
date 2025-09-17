package com.example.demo.web;

import com.example.demo.pg.EmployeeEntity;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.graph.NeoEmployeeRow;
import com.example.demo.graph.NeoEmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
        List<EmployeeEntity> employees = pgRepo.findAll();
        List<NeoEmployeeRow> graphRows = neoRepo.findEmployeesTable();

        model.addAttribute("employees", employees);
        model.addAttribute("graphRows", graphRows);
        return "dashboard";
    }
}
