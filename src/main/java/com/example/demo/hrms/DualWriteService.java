package com.example.demo.hrms;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.pg.EmployeeRepository;
import com.example.demo.pg.EmployeeRepository.PgDeptInfo;
import com.example.demo.pg.EmployeeRepository.PgJobRow;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DualWriteService {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;

    public static final class WriteResult {
        public final long effectiveEmployeeId;
        public final double pgMs;
        public final double neoMs;
        public WriteResult(long id, double pgMs, double neoMs) {
            this.effectiveEmployeeId = id; this.pgMs = pgMs; this.neoMs = neoMs;
        }
    }

    public DualWriteService(EmployeeRepository pgRepo, NeoEmployeeRepository neoRepo) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
    }

    /* ================= ADD ================= */

    public WriteResult addToBoth(AddEmployeeForm f) {
        long t1 = System.nanoTime();

        // 1) Insert pracownika w PG
        pgRepo.insertEmployeeAutoId(
                n(f.getFirstName()),
                n(f.getLastName()),
                n(f.getEmail()),
                n(f.getPhone()),
                nz(f.getHireDate()),
                f.getJobId(),
                f.getDepartmentId()
        );
        Long newId = pgRepo.getLastEmployeeIdFromSequence();

        long t2 = System.nanoTime();
        double pgMs = (t2 - t1) / 1_000_000.0;

        // 2) Dociągamy dane pomocnicze do Neo
        PgJobRow job   = (f.getJobId() != null)       ? pgRepo.getJobById(f.getJobId())         : null;
        PgDeptInfo dep = (f.getDepartmentId() != null)? pgRepo.getDeptInfo(f.getDepartmentId()) : null;

        Map<String,Object> pProps = personProps(newId, f.getFirstName(), f.getLastName(),
                f.getEmail(), f.getPhone(), f.getHireDate());
        Map<String,Object> jProps = (job == null ? null : jobProps(job));
        Map<String,Object> dProps = (dep == null ? null : deptProps(dep));
        Map<String,Object> sProps = null; // brak zmian w wynagrodzeniach

        long t3 = System.nanoTime();
        neoRepo.upsertFull(newId, pProps, jProps, dProps, sProps);
        long t4 = System.nanoTime();
        double neoMs = (t4 - t3) / 1_000_000.0;

        return new WriteResult(newId, pgMs, neoMs);
    }

    /* ================= EDIT ================= */

    public WriteResult editBoth(EditEmployeeForm f) {
        Long id = f.getEmployeeId();
        if (id == null) throw new IllegalArgumentException("employeeId is required");

        // 1) PG
        long t1 = System.nanoTime();

        pgRepo.updateEmployee(
                id,
                n(f.getFirstName()),
                n(f.getLastName()),
                n(f.getEmail()),
                n(f.getPhone()),
                nz(f.getHireDate()),
                f.getDepartmentId()
        );

        if (f.getJobId() != null) {
            pgRepo.setEmployeeJob(id, f.getJobId());
        }

        long t2 = System.nanoTime();
        double pgMs = (t2 - t1) / 1_000_000.0;

        // 2) Neo4j — KLUCZOWE: zawsze pobieramy aktualne job/dept z PG i wpychamy do Neo
        PgJobRow job   = (f.getJobId() != null)        ? pgRepo.getJobById(f.getJobId())         : null;
        PgDeptInfo dep = (f.getDepartmentId() != null) ? pgRepo.getDeptInfo(f.getDepartmentId()) : null;

        Map<String,Object> pProps = personProps(id, f.getFirstName(), f.getLastName(),
                f.getEmail(), f.getPhone(), f.getHireDate());
        Map<String,Object> jProps = (job == null ? null : jobProps(job));
        Map<String,Object> dProps = (dep == null ? null : deptProps(dep));
        Map<String,Object> sProps = null;

        long t3 = System.nanoTime();
        neoRepo.updateFull(id, pProps, jProps, dProps, sProps);
        long t4 = System.nanoTime();
        double neoMs = (t4 - t3) / 1_000_000.0;

        return new WriteResult(id, pgMs, neoMs);
    }

    /* ================= DELETE ================= */

    public WriteResult deleteBoth(Long employeeId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is required");

        long t1 = System.nanoTime();
        pgRepo.deleteEmployee(employeeId);
        long t2 = System.nanoTime();
        double pgMs = (t2 - t1) / 1_000_000.0;

        long t3 = System.nanoTime();
        neoRepo.deleteByEmployeeId(employeeId);
        long t4 = System.nanoTime();
        double neoMs = (t4 - t3) / 1_000_000.0;

        return new WriteResult(employeeId, pgMs, neoMs);
    }

    /* ============ helpers (mapy dla Neo4j po polsku) ============ */

    private static Map<String,Object> personProps(Long id, String first, String last,
                                                  String email, String phone, LocalDate hireDate) {
        Map<String,Object> p = new HashMap<>();
        p.put("imię", n(first));
        p.put("nazwisko", n(last));
        p.put("email", n(email));
        p.put("telefon", n(phone));
        p.put("data_zatrudnienia", nz(hireDate));
        return p;
    }

    private static Map<String,Object> jobProps(PgJobRow j) {
        Map<String,Object> m = new HashMap<>();
        m.put("tytuł", j.getTitle());
        m.put("min_pensja", j.getMinSalary() == null ? null : j.getMinSalary());
        m.put("max_pensja", j.getMaxSalary() == null ? null : j.getMaxSalary());
        return m;
    }

    private static Map<String,Object> deptProps(PgDeptInfo d) {
        Map<String,Object> m = new HashMap<>();
        m.put("nazwa", d.getName());
        m.put("lokalizacja", d.getLocation());
        return m;
    }

    private static String n(String s) { return (s == null || s.isBlank()) ? null : s; }
    private static LocalDate nz(LocalDate d) { return d; }
}
