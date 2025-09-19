package com.example.demo.web;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.hrms.AddEmployeeForm;
import com.example.demo.hrms.DualWriteService;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.pg.PgDepartmentRow;
import com.example.demo.pg.PgEmployeeRow;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;
    private final DualWriteService dualWrite;

    public DashboardController(EmployeeRepository pgRepo,
                               NeoEmployeeRepository neoRepo,
                               DualWriteService dualWrite) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
        this.dualWrite = dualWrite;
    }

    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(name = "pgPage",  defaultValue = "0") int pgPage,
                            @RequestParam(name = "neoPage", defaultValue = "0") int neoPage,
                            @RequestParam(name = "size",    defaultValue = "20") int size,
                            Model model) {

        // PG
        long pgCount = pgRepo.countAll();
        int pgPages = (int) Math.max(1, Math.ceil(pgCount / (double) size));
        int pgOffset = Math.max(0, pgPage) * size;
        List<PgEmployeeRow> pgRows = pgRepo.page(pgOffset, size);

        // Neo4j
        long neoCount = neoRepo.countPersons();
        int neoPages = (int) Math.max(1, Math.ceil(neoCount / (double) size));
        int neoSkip = Math.max(0, neoPage) * size;
        List<Map<String,Object>> neoRows = neoRepo.pageAsRowMap(neoSkip, size);

        // Departamenty do selecta
        List<PgDepartmentRow> departments = pgRepo.listDepartments();

        model.addAttribute("size", size);

        model.addAttribute("pgPage", pgPage);
        model.addAttribute("pgPages", pgPages);
        model.addAttribute("pgCount", pgCount);
        model.addAttribute("pgRows", pgRows);

        model.addAttribute("neoPage", neoPage);
        model.addAttribute("neoPages", neoPages);
        model.addAttribute("neoCount", neoCount);
        model.addAttribute("neoRows", neoRows);

        model.addAttribute("departments", departments);

        return "dashboard";
    }

    @PostMapping("/add-both")
    public String addBoth(@ModelAttribute AddEmployeeForm form, RedirectAttributes ra) {
        try {
            var res = dualWrite.addToBoth(form);
            ra.addFlashAttribute("pgTimeMs", res.pgMs);
            ra.addFlashAttribute("neoTimeMs", res.neoMs);
            ra.addFlashAttribute("lastEmployeeId", res.effectiveEmployeeId);
        } catch (Exception ex) {
            ra.addFlashAttribute("lastError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return "redirect:/dashboard";
    }
}
