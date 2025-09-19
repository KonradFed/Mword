package com.example.demo.web;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.hrms.AddEmployeeForm;
import com.example.demo.hrms.DualWriteService;
import com.example.demo.hrms.EditEmployeeForm;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.pg.PgDepartmentRow;
import com.example.demo.pg.PgEmployeeRow;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
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
                            @RequestParam(name = "refresh", defaultValue = "0") int refresh,
                            Model model) {

        // Paginacja PG
        long pgCount = pgRepo.countAll();
        int pgPages = (int) Math.max(1, Math.ceil(pgCount / (double) size));
        int pgOffset = Math.max(0, pgPage) * size;

        // Paginacja Neo
        long neoCount = neoRepo.countPersons();
        int neoPages = (int) Math.max(1, Math.ceil(neoCount / (double) size));
        int neoSkip = Math.max(0, neoPage) * size;

        // Dane + pomiary czasu, jeśli refresh=1
        List<PgEmployeeRow> pgRows;
        double pgMs = Double.NaN;
        var neoRows = java.util.Collections.<Map<String,Object>>emptyList();
        double neoMs = Double.NaN;

        if (refresh == 1) {
            long t1 = System.nanoTime();
            pgRows = pgRepo.page(pgOffset, size);
            long t2 = System.nanoTime();
            pgMs = (t2 - t1) / 1_000_000.0;

            long t3 = System.nanoTime();
            neoRows = neoRepo.pageAsRowMap(neoSkip, size);
            long t4 = System.nanoTime();
            neoMs = (t4 - t3) / 1_000_000.0;
        } else {
            // bez pomiaru (zwykłe wejście na dashboard)
            pgRows = pgRepo.page(pgOffset, size);
            neoRows = neoRepo.pageAsRowMap(neoSkip, size);
        }

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

        // jeśli refresh=1 – pokaż wyniki czasu; w innym wypadku niech będą NaN i ukryte w UI
        model.addAttribute("pgTimeMs", pgMs);
        model.addAttribute("neoTimeMs", neoMs);

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
        return "redirect:/dashboard?refresh=1";
    }

    /** ZAWSZE 200 – zwraca cokolwiek się uda (PG i/lub Neo), brak = null. */
    @GetMapping("/api/employee/{id}")
    @ResponseBody
    public ResponseEntity<?> getEmployee(@PathVariable("id") Long id) {
        Map<String,Object> out = new HashMap<>();

        try {
            PgEmployeeRow pg = pgRepo.findRowById(id);
            if (pg != null) {
                out.put("pg", Map.of(
                    "employeeId", pg.getEmployeeId(),
                    "firstName", pg.getFirstName(),
                    "lastName",  pg.getLastName(),
                    "email",     pg.getEmail(),
                    "phone",     pg.getPhone(),
                    "hireDate",  pg.getHireDate(),
                    "departmentId",   pg.getDepartmentId(),
                    "departmentName", pg.getDepartmentName()
                ));
            } else {
                out.put("pg", null);
            }
        } catch (Exception e) { out.put("pg", null); }

        try {
            Map<String,Object> neoRaw = neoRepo.findOneRowMapByEmployeeId(id);
            Object neo = null;
            if (neoRaw != null) {
                neo = neoRaw.containsKey("row") ? neoRaw.get("row") : neoRaw;
            }
            out.put("neo", neo);
        } catch (Exception e) { out.put("neo", null); }

        return ResponseEntity.ok(out);
    }

    @PostMapping("/edit-both")
    public String editBoth(@ModelAttribute EditEmployeeForm form, RedirectAttributes ra) {
        try {
            var res = dualWrite.editBoth(form);
            ra.addFlashAttribute("pgTimeMs", res.pgMs);
            ra.addFlashAttribute("neoTimeMs", res.neoMs);
            ra.addFlashAttribute("lastEmployeeId", res.effectiveEmployeeId);
        } catch (Exception ex) {
            ra.addFlashAttribute("lastError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return "redirect:/dashboard?refresh=1";
    }

    @PostMapping("/delete-both")
    public String deleteBoth(@RequestParam("employeeId") Long employeeId, RedirectAttributes ra) {
        try {
            var res = dualWrite.deleteBoth(employeeId);
            ra.addFlashAttribute("pgTimeMs", res.pgMs);
            ra.addFlashAttribute("neoTimeMs", res.neoMs);
            ra.addFlashAttribute("lastEmployeeId", res.effectiveEmployeeId);
        } catch (Exception ex) {
            ra.addFlashAttribute("lastError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return "redirect:/dashboard?refresh=1";
    }
}
