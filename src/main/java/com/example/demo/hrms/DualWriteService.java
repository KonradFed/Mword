package com.example.demo.hrms;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.pg.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class DualWriteService {

    private final EmployeeRepository pgRepo;
    private final NeoEmployeeRepository neoRepo;

    public static class Result {
        public final double pgMs;
        public final double neoMs;
        public final Long effectiveEmployeeId;
        public Result(double pgMs, double neoMs, Long id) {
            this.pgMs = pgMs; this.neoMs = neoMs; this.effectiveEmployeeId = id;
        }
    }

    public DualWriteService(EmployeeRepository pgRepo, NeoEmployeeRepository neoRepo) {
        this.pgRepo = pgRepo;
        this.neoRepo = neoRepo;
    }

    /** Używamy JPA TM (masz też neo4jTransactionManager). */
    @Transactional("transactionManager")
    public Result addToBoth(AddEmployeeForm f) {
        // ===== WALIDACJE (regexy + reguły) =====
        if (f.getFirstName() == null || !f.getFirstName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("First name: tylko litery/spacje/kreski (2–50 znaków).");

        if (f.getLastName() == null || !f.getLastName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("Last name: tylko litery/spacje/kreski (2–50 znaków).");

        if (f.getEmail() == null || !f.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email ma nieprawidłowy format.");

        if (f.getPhone() != null && !f.getPhone().isBlank()
                && !f.getPhone().matches("^[0-9]{6,20}$"))
            throw new IllegalArgumentException("Phone: tylko cyfry (6–20).");

        if (f.getMinSalary() != null && f.getMaxSalary() != null && f.getMinSalary() >= f.getMaxSalary())
            throw new IllegalArgumentException("Min salary musi być mniejsze niż Max salary.");

        if (f.getAmount() != null && f.getAmount() < 0)
            throw new IllegalArgumentException("Amount nie może być ujemny.");

        // ===== PG: najpierw ewentualne JOBS (auto-id) =====
        Long jobIdL = null;
        if ((f.getTitle() != null && !f.getTitle().isBlank())
                || f.getMinSalary() != null || f.getMaxSalary() != null) {
            pgRepo.insertJobAutoId(
                    (f.getTitle() != null && !f.getTitle().isBlank()) ? f.getTitle() : "Unknown",
                    f.getMinSalary(), f.getMaxSalary()
            );
            jobIdL = pgRepo.getLastJobIdFromSequence();
        }

        // ===== PG: EMPLOYEES (auto-id) =====
        long t0 = System.nanoTime();
        pgRepo.insertEmployeeAutoId(
                f.getFirstName(), f.getLastName(), f.getEmail(), f.getPhone(),
                f.getHireDate(), jobIdL == null ? null : jobIdL.intValue(), f.getDepartmentId()
        );
        Long pgId = pgRepo.getLastEmployeeIdFromSequence();
        if (pgId == null) throw new IllegalStateException("Nie udało się pobrać employee_id z sekwencji.");
        double pgMs = (System.nanoTime() - t0) / 1_000_000.0;

        // ===== Neo4j: upsert :Pracownik + (opcjonalnie) relacje =====
        long t1 = System.nanoTime();

        Map<String,Object> p = new HashMap<>();
        p.put("imię", f.getFirstName());
        p.put("nazwisko", f.getLastName());
        p.put("email", f.getEmail());
        if (f.getPhone() != null && !f.getPhone().isBlank()) p.put("telefon", f.getPhone());
        if (f.getHireDate() != null) p.put("data_zatrudnienia", f.getHireDate().toString());

        Map<String,Object> j = null;
        if (f.getTitle() != null || f.getMinSalary() != null || f.getMaxSalary() != null) {
            j = new HashMap<>();
            if (f.getTitle()     != null && !f.getTitle().isBlank()) j.put("tytuł", f.getTitle());
            if (f.getMinSalary() != null) j.put("min_pensja", f.getMinSalary());
            if (f.getMaxSalary() != null) j.put("max_pensja", f.getMaxSalary());
        }

        // Dept do Neo4j – z formularza albo z PG
        String neoDeptName = f.getDepartmentName();
        String neoLocation = f.getLocation();
        if ((neoDeptName == null || neoDeptName.isBlank() || neoLocation == null || neoLocation.isBlank())
                && f.getDepartmentId() != null) {
            var info = pgRepo.getDeptInfo(f.getDepartmentId());
            if (info != null) {
                if (neoDeptName == null || neoDeptName.isBlank()) neoDeptName = info.getName();
                if (neoLocation == null || neoLocation.isBlank()) neoLocation = info.getLocation();
            }
        }
        Map<String,Object> d = null;
        if ((neoDeptName != null && !neoDeptName.isBlank()) || (neoLocation != null && !neoLocation.isBlank())) {
            d = new HashMap<>();
            if (neoDeptName != null && !neoDeptName.isBlank()) d.put("nazwa", neoDeptName);
            if (neoLocation != null && !neoLocation.isBlank()) d.put("lokalizacja", neoLocation);
        }

        Map<String,Object> s = null;
        if (f.getAmount() != null || f.getFromDate() != null) {
            s = new HashMap<>();
            if (f.getAmount()   != null) s.put("kwota", f.getAmount());
            if (f.getFromDate() != null) s.put("od", f.getFromDate().toString());
        }

        neoRepo.upsertFull(pgId, p, j, d, s);
        double neoMs = (System.nanoTime() - t1) / 1_000_000.0;

        return new Result(pgMs, neoMs, pgId);
    }
}
