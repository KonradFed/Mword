package com.example.demo.web;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.hrms.AddEmployeeForm;
import com.example.demo.hrms.DualWriteService;
import com.example.demo.hrms.EditEmployeeForm;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.pg.PgDepartmentRow;
import com.example.demo.pg.PgEmployeeRow;
import com.example.demo.pg.PgJobWithDeptsRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        int pgPage  = (unifiedPage != null) ? Math.max(0, unifiedPage)
                : (pgPageParam  == null ? 0 : Math.max(0, pgPageParam));
        int neoPage = (unifiedPage != null) ? Math.max(0, unifiedPage)
                : (neoPageParam == null ? 0 : Math.max(0, neoPageParam));

        long pgCount = pgRepo.countAll();
        int pgPages  = (int) Math.max(1, Math.ceil(pgCount / (double) size));
        int pgOffset = Math.min(pgPage, Math.max(0, pgPages - 1)) * size;

        long neoCount = neoRepo.countPersons();
        int neoPages  = (int) Math.max(1, Math.ceil(neoCount / (double) size));
        int neoSkip   = Math.min(neoPage, Math.max(0, neoPages - 1)) * size;

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

        // ======== WAŻNE: rozpakuj ewentualne opakowanie 'row' z SDN ========
        neoRows = unwrapRowMaps(neoRows);

        List<PgDepartmentRow> departments     = pgRepo.listDepartments();
        List<PgJobWithDeptsRow> jobsWithDepts = pgRepo.listJobsWithDeptsAgg();

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
        model.addAttribute("jobs", jobsWithDepts);

        return "dashboard";
    }

    @PostMapping("/add-both")
    public String addBoth(@ModelAttribute AddEmployeeForm form, RedirectAttributes ra) {
        try {
            var res = dualWrite.addToBoth(form);
            ra.addFlashAttribute("pgTimeMs",  res.pgMs);
            ra.addFlashAttribute("neoTimeMs", res.neoMs);
            ra.addFlashAttribute("lastEmployeeId", res.effectiveEmployeeId);
        } catch (Exception ex) {
            ra.addFlashAttribute("lastError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return "redirect:/dashboard?refresh=1";
    }

    @PostMapping("/edit-both")
    public String editBoth(@ModelAttribute EditEmployeeForm form, RedirectAttributes ra) {
        try {
            var res = dualWrite.editBoth(form);
            ra.addFlashAttribute("pgTimeMs",  res.pgMs);
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
            ra.addFlashAttribute("pgTimeMs",  res.pgMs);
            ra.addFlashAttribute("neoTimeMs", res.neoMs);
            ra.addFlashAttribute("lastEmployeeId", res.effectiveEmployeeId);
        } catch (Exception ex) {
            ra.addFlashAttribute("lastError", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return "redirect:/dashboard?refresh=1";
    }

    @GetMapping("/api/employee/{id}")
    @ResponseBody
    public ResponseEntity<?> getEmployee(@PathVariable("id") Long id) {
        Map<String,Object> out = new HashMap<>();

        try {
            PgEmployeeRow pg = pgRepo.findRowById(id);
            if (pg != null) {
                Map<String,Object> pgMap = new HashMap<>();
                pgMap.put("employeeId",     pg.getEmployeeId());
                pgMap.put("firstName",      pg.getFirstName());
                pgMap.put("lastName",       pg.getLastName());
                pgMap.put("email",          pg.getEmail());
                pgMap.put("phone",          pg.getPhone());
                pgMap.put("hireDate",       toIso(pg.getHireDate()));
                pgMap.put("departmentId",   pg.getDepartmentId());
                pgMap.put("departmentName", pg.getDepartmentName());
                pgMap.put("jobId",          pg.getJobId());
                pgMap.put("jobTitle",       pg.getJobTitle());
                pgMap.put("location",       pg.getLocation());
                out.put("pg", pgMap);
            } else out.put("pg", null);
        } catch (Exception e) { out.put("pg", null); }

        try {
            Map<String,Object> neo = neoRepo.findOneFlatByEmployeeId(id);
            neo = unwrapRowMap(neo); // rozpakuj 'row' jeśli jest

            if (neo == null || neo.isEmpty()) {
                Object pgObj = out.get("pg");
                String email = null;
                if (pgObj instanceof Map<?,?> m) {
                    Object em = m.get("email");
                    if (em != null) email = String.valueOf(em);
                }
                if (email != null && !email.isBlank()) {
                    neo = unwrapRowMap(neoRepo.findOneFlatByEmail(email));
                }
            }
            if (neo != null && !neo.isEmpty()) {
                normalizeDates(neo);
                out.put("neo", neo);
            } else {
                out.put("neo", null);
            }
        } catch (Exception e) { out.put("neo", null); }

        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/jobs")
    @ResponseBody
    public List<Map<String,Object>> jobs(@RequestParam(name="deptId", required = false) Long deptId) {
        List<PgJobWithDeptsRow> all = pgRepo.listJobsWithDeptsAgg();
        return all.stream()
                .filter(j -> {
                    if (deptId == null) return true;
                    String ids = j.getDeptIds() == null ? "" : j.getDeptIds();
                    if (ids.isBlank()) return false;
                    for (String s : ids.split(",")) {
                        if (s != null && !s.isBlank()) {
                            try {
                                if (Long.parseLong(s.trim()) == deptId) return true;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparing(PgJobWithDeptsRow::getTitle, Comparator.nullsLast(String::compareTo)))
                .map(j -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("jobId", j.getJobId());
                    m.put("title", j.getTitle());
                    m.put("deptIds", j.getDeptIds());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /* ===== utils ===== */
    private static String toIso(LocalDate d) { return d != null ? d.toString() : null; }

    private static void normalizeDates(Map<String,Object> neo) {
        Object hd = neo.get("hireDate"); if (hd != null) neo.put("hireDate", String.valueOf(hd));
        Object fd = neo.get("fromDate"); if (fd != null) neo.put("fromDate", String.valueOf(fd));
    }

    /** Rozpakowanie listy map z ewentualnego klucza 'row' zwracanego przez SDN. */
    private static List<Map<String,Object>> unwrapRowMaps(List<Map<String,Object>> in) {
        if (in == null) return Collections.emptyList();
        List<Map<String,Object>> out = new ArrayList<>(in.size());
        for (Map<String,Object> m : in) out.add(unwrapRowMap(m));
        return out;
    }

    /** Rozpakowanie pojedynczej mapy z klucza 'row'. */
    @SuppressWarnings("unchecked")
    private static Map<String,Object> unwrapRowMap(Map<String,Object> m) {
        if (m == null) return null;
        Object row = m.get("row");
        if (row instanceof Map<?,?> rm) {
            return new HashMap<>((Map<String,Object>) rm);
        }
        return m;
    }
}
