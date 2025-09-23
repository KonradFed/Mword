package com.example.demo.hrms;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.pg.EmployeeRepository;
// Uwaga: korzystamy z projekcji zagnieżdżonej w EmployeeRepository:
import com.example.demo.pg.EmployeeRepository.PgJobRow;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DualWriteService {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;

    public DualWriteService(EmployeeRepository pgRepo, NeoEmployeeRepository neoRepo) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
    }

    /* =========================================================
       CREATE (PG + Neo4j)
       ========================================================= */
    @Transactional
    public Result addToBoth(AddEmployeeForm f) {
        long t1 = System.nanoTime();

        // 1) (opcjonalnie) utworzenie JOB w PG, jeżeli nie wybrano istniejącego,
        // a podano title/min/max w formularzu tworzenia
        Integer jobId = f.getJobId();
        if (jobId == null && f.getTitle() != null && !f.getTitle().isBlank()) {
            pgRepo.insertJobAutoId(f.getTitle(), f.getMinSalary(), f.getMaxSalary());
            jobId = pgRepo.getLastJobIdFromSequence().intValue();
        }

        // 2) INSERT pracownika w PG (auto-increment employee_id)
        pgRepo.insertEmployeeAutoId(
            f.getFirstName(),
            f.getLastName(),
            f.getEmail(),
            f.getPhone(),
            f.getHireDate(),
            jobId,
            f.getDepartmentId()
        );
        Long employeeId = pgRepo.getLastEmployeeIdFromSequence();

        long t2 = System.nanoTime();

        // 3) Upsert w Neo4j
        Map<String, Object> pProps = new HashMap<>();
        pProps.put("employee_id",       employeeId);
        pProps.put("imię",              f.getFirstName());
        pProps.put("nazwisko",          f.getLastName());
        pProps.put("email",             f.getEmail());
        pProps.put("telefon",           f.getPhone());
        pProps.put("data_zatrudnienia", f.getHireDate());

        Map<String, Object> jProps = null;
        if (jobId != null) {
            PgJobRow j = pgRepo.getJobById(jobId);
            if (j != null) {
                jProps = new HashMap<>();
                jProps.put("tytuł",      j.getTitle());
                jProps.put("min_pensja", j.getMinSalary());
                jProps.put("max_pensja", j.getMaxSalary());
            }
        } else if (f.getTitle() != null && !f.getTitle().isBlank()) {
            // fallback – jeśli nowy job powstał tylko z formularza
            jProps = new HashMap<>();
            jProps.put("tytuł",      f.getTitle());
            jProps.put("min_pensja", f.getMinSalary());
            jProps.put("max_pensja", f.getMaxSalary());
        }

        Map<String, Object> dProps = null;
        if (f.getDepartmentName() != null || f.getLocation() != null) {
            dProps = new HashMap<>();
            dProps.put("nazwa",       f.getDepartmentName());
            dProps.put("lokalizacja", f.getLocation());
        }

        // Salaries pomijamy (null)
        Map<String, Object> sProps = null;

        neoRepo.upsertFull(employeeId, pProps, jProps, dProps, sProps);

        long t3 = System.nanoTime();

        Result r = new Result();
        r.effectiveEmployeeId = employeeId;
        r.pgMs  = (t2 - t1) / 1_000_000.0;
        r.neoMs = (t3 - t2) / 1_000_000.0;
        return r;
    }

    /* =========================================================
       UPDATE (PG + Neo4j) – bez edycji salary w formularzu
       ========================================================= */
    @Transactional
    public Result editBoth(EditEmployeeForm f) {
        long t1 = System.nanoTime();

        // 1) UPDATE PG (bez salary i bez tworzenia jobów)
        pgRepo.updateEmployee(
            f.getEmployeeId(),
            f.getFirstName(),
            f.getLastName(),
            f.getEmail(),
            f.getPhone(),
            f.getHireDate(),
            f.getDepartmentId()
        );

        // 2) Zmiana joba w PG (tylko gdy wybrano nowy jobId)
        Integer jobId = f.getJobId();
        if (jobId != null) {
            pgRepo.setEmployeeJob(f.getEmployeeId(), jobId);
        }

        long t2 = System.nanoTime();

        // 3) UPDATE Neo4j
        Map<String, Object> pProps = new HashMap<>();
        pProps.put("imię",              f.getFirstName());
        pProps.put("nazwisko",          f.getLastName());
        pProps.put("email",             f.getEmail());
        pProps.put("telefon",           f.getPhone());
        pProps.put("data_zatrudnienia", f.getHireDate());

        Map<String, Object> jProps = null;
        if (jobId != null) {
            // bierzemy dane joba z PG, NIE z formularza
            PgJobRow j = pgRepo.getJobById(jobId);
            if (j != null) {
                jProps = new HashMap<>();
                jProps.put("tytuł",      j.getTitle());
                jProps.put("min_pensja", j.getMinSalary());
                jProps.put("max_pensja", j.getMaxSalary());
            }
        }

        Map<String, Object> dProps = null;
        if (f.getDepartmentName() != null || f.getLocation() != null) {
            dProps = new HashMap<>();
            dProps.put("nazwa",       f.getDepartmentName());
            dProps.put("lokalizacja", f.getLocation());
        }

        Map<String, Object> sProps = null; // salary pomijamy

        neoRepo.updateFull(f.getEmployeeId(), pProps, jProps, dProps, sProps);

        long t3 = System.nanoTime();

        Result r = new Result();
        r.effectiveEmployeeId = f.getEmployeeId();
        r.pgMs  = (t2 - t1) / 1_000_000.0;
        r.neoMs = (t3 - t2) / 1_000_000.0;
        return r;
    }

    /* =========================================================
       DELETE (PG + Neo4j)
       ========================================================= */
    @Transactional
    public Result deleteBoth(Long employeeId) {
        long t1 = System.nanoTime();
        pgRepo.deleteEmployee(employeeId);
        long t2 = System.nanoTime();
        neoRepo.deleteByEmployeeId(employeeId);
        long t3 = System.nanoTime();

        Result r = new Result();
        r.effectiveEmployeeId = employeeId;
        r.pgMs  = (t2 - t1) / 1_000_000.0;
        r.neoMs = (t3 - t2) / 1_000_000.0;
        return r;
    }

    /* DTO wyniku */
    public static class Result {
        public Long   effectiveEmployeeId;
        public double pgMs;
        public double neoMs;
    }
}
