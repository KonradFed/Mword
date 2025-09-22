package com.example.demo.web;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.hrms.AddEmployeeForm;
import com.example.demo.hrms.DualWriteService;
import com.example.demo.hrms.EditEmployeeForm;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.pg.PgDepartmentRow;
import com.example.demo.pg.PgEmployeeRow;
import com.example.demo.pg.PgJobRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

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
    public String dashboard(@RequestParam(name = "pgPage",  required = false) Integer pgPageParam,
                            @RequestParam(name = "neoPage", required = false) Integer neoPageParam,
                            @RequestParam(name = "page",    required = false) Integer unifiedPage,
                            @RequestParam(name = "size",    defaultValue = "20") int size,
                            @RequestParam(name = "refresh", defaultValue = "0") int refresh,
                            Model model) {

        int pgPage  = (unifiedPage != null) ? Math.max(0, unifiedPage) : (pgPageParam  == null ? 0 : Math.max(0, pgPageParam));
        int neoPage = (unifiedPage != null) ? Math.max(0, unifiedPage) : (neoPageParam == null ? 0 : Math.max(0, neoPageParam));

        long pgCount = pgRepo.countAll();
        int pgPages = (int) Math.max(1, Math.ceil(pgCount / (double) size));
        int pgOffset = Math.min(pgPage, Math.max(0, pgPages - 1)) * size;

        long neoCount = neoRepo.countPersons();
        int neoPages = (int) Math.max(1, Math.ceil(neoCount / (double) size));
        int neoSkip = Math.min(neoPage, Math.max(0, neoPages - 1)) * size;

        List<PgEmployeeRow> pgRows;
        double pgMs = Double.NaN;
        List<Map<String,Object>> neoRows;
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
            pgRows = pgRepo.page(pgOffset, size);
            neoRows = neoRepo.pageAsRowMap(neoSkip, size);
        }

        List<PgDepartmentRow> departments = pgRepo.listDepartments();
        List<PgJobRow> jobs = pgRepo.listJobsForSelect();

        model.addAttribute("size", size);

        model.addAttribute("pgPage", pgPage);
        model.addAttribute("pgPages", pgPages);
        model.addAttribute("pgCount", pgCount);
        model.addAttribute("pgRows", pgRows);

        model.addAttribute("neoPage", neoPage);
        model.addAttribute("neoPages", neoPages);
        model.addAttribute("neoCount", neoCount);
        model.addAttribute("neoRows", neoRows);

        model.addAttribute("pgTimeMs", pgMs);
        model.addAttribute("neoTimeMs", neoMs);

        model.addAttribute("departments", departments);
        model.addAttribute("jobs", jobs); // <=== do selecta stanowisk

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

    /** PG + Neo – rekord do edycji dla modala; Neo płasko (bez 'row'). */
    @GetMapping("/api/employee/{id}")
    @ResponseBody
    public ResponseEntity<?> getEmployee(@PathVariable("id") Long id) {
        Map<String,Object> out = new HashMap<>();

        try {
            PgEmployeeRow pg = pgRepo.findRowById(id);
            if (pg != null) {
                out.put("pg", Map.of(
                    "employeeId",     pg.getEmployeeId(),
                    "firstName",      pg.getFirstName(),
                    "lastName",       pg.getLastName(),
                    "email",          pg.getEmail(),
                    "phone",          pg.getPhone(),
                    "hireDate",       toIso(pg.getHireDate()),
                    "departmentId",   pg.getDepartmentId(),
                    "departmentName", pg.getDepartmentName(),
                    "jobTitle",       pg.getJobTitle(),
                    "location",       pg.getLocation()
                ));
            } else out.put("pg", null);
        } catch (Exception e) { out.put("pg", null); }

        try {
            Map<String,Object> neo = neoRepo.findOneFlatByEmployeeId(id);
            if (neo == null || neo.isEmpty()) {
                String email = null;
                Object pgObj = out.get("pg");
                if (pgObj instanceof Map<?,?> m) {
                    Object em = m.get("email");
                    if (em != null) email = String.valueOf(em);
                }
                if (email != null) neo = neoRepo.findOneFlatByEmail(email);
            }
            if (neo != null) normalizeDates(neo);
            out.put("neo", neo);
        } catch (Exception e) { out.put("neo", null); }

        return ResponseEntity.ok(out);
    }

    private static String toIso(LocalDate d) { return d != null ? d.toString() : null; }
    private static void normalizeDates(Map<String,Object> neo) {
        Object hd = neo.get("hireDate"); if (hd != null) neo.put("hireDate", String.valueOf(hd));
        Object fd = neo.get("fromDate"); if (fd != null) neo.put("fromDate", String.valueOf(fd));
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
