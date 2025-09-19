package com.example.demo.web;

import com.example.demo.graph.NeoEmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/neo")
public class NeoCrudController {

    private final NeoEmployeeRepository neoRepo;

    public NeoCrudController(NeoEmployeeRepository neoRepo) {
        this.neoRepo = neoRepo;
    }

    @PostMapping("/edit")
    public String upsert(@RequestParam("employeeId") Long employeeId,
                         @RequestParam(value = "firstName", required = false) String firstName,
                         @RequestParam(value = "lastName", required = false) String lastName,
                         @RequestParam(value = "email", required = false) String email,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "hireDate", required = false) String hireDateIso,
                         RedirectAttributes ra) {

        long t0 = System.nanoTime();
        String error = null;
        try {
            Map<String,Object> props = new HashMap<>();
            if (firstName != null && !firstName.isBlank()) props.put("imię", firstName);
            if (lastName  != null && !lastName.isBlank())  props.put("nazwisko", lastName);
            if (email     != null && !email.isBlank())     props.put("email", email);
            if (phone     != null && !phone.isBlank())     props.put("telefon", phone);
            if (hireDateIso != null && !hireDateIso.isBlank()) props.put("data_zatrudnienia", hireDateIso);
            props.entrySet().removeIf(e -> e.getValue() == null);

            // repozytorium udostępnia upsertFull(p, j, d, s); przekazujemy tylko właściwości osoby
            neoRepo.upsertFull(employeeId, props, null, null, null);
        } catch (Exception ex) {
            error = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        double neoMs = (System.nanoTime() - t0) / 1_000_000.0;

        ra.addFlashAttribute("neoTimeMs", neoMs);
        if (error != null) ra.addFlashAttribute("lastError", error);
        return "redirect:/dashboard";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("employeeId") Long employeeId, RedirectAttributes ra) {
        long t0 = System.nanoTime();
        String error = null;
        try {
            neoRepo.deleteByEmployeeId(employeeId);
        } catch (Exception ex) {
            error = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        double neoMs = (System.nanoTime() - t0) / 1_000_000.0;

        ra.addFlashAttribute("neoTimeMs", neoMs);
        if (error != null) ra.addFlashAttribute("lastError", error);
        return "redirect:/dashboard";
    }
}
