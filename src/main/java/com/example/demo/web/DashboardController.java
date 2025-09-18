package com.example.demo.web;

import com.example.demo.pg.EmployeeEntity;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.graph.NeoEmployeeRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
                .sorted(Comparator.comparingInt(e -> e.getEmployeeId()))
                .toList();
        model.addAttribute("pgRows", pgRows);
        model.addAttribute("pgCount", pgRows.size());

        // --- Neo4j --- use MAPs in the view to mirror /api/neo exactly
        List<Map<String, Object>> neoRows = neoRepo.findEmployeesTableAsMap();
        model.addAttribute("neoRows", neoRows);
        model.addAttribute("neoCount", neoRows.size());

        return "dashboard";
    }

    // JSON: return raw maps to avoid Jackson issues with SDN projection proxies
    @GetMapping("/api/neo")
    @ResponseBody
    public List<Map<String, Object>> apiNeo() {
        return neoRepo.findEmployeesTableAsMap();
    }

    // Optional: projection JSON for comparison (may fail to serialize)
    @GetMapping("/api/neo/proj")
    @ResponseBody
    public List<NeoEmployeeRow> apiNeoProjection() {
        return neoRepo.findEmployeesTable();
    }
}
