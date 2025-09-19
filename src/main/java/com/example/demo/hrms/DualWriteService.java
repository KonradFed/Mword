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

    /* ===== ADD (z Twojej działającej wersji) ===== */
    @Transactional("transactionManager")
    public Result addToBoth(AddEmployeeForm f) {
        if (f.getFirstName() == null || !f.getFirstName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("First name: tylko litery/spacje/kreski (2–50).");
        if (f.getLastName() == null || !f.getLastName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("Last name: tylko litery/spacje/kreski (2–50).");
        if (f.getEmail() == null || !f.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email ma nieprawidłowy format.");
        if (f.getPhone() != null && !f.getPhone().isBlank() && !f.getPhone().matches("^[0-9]{6,20}$"))
            throw new IllegalArgumentException("Phone: tylko cyfry (6–20).");
        if (f.getMinSalary() != null && f.getMaxSalary() != null && f.getMinSalary() >= f.getMaxSalary())
            throw new IllegalArgumentException("Min salary musi być mniejsze niż Max salary.");
        if (f.getAmount() != null && f.getAmount() < 0)
            throw new IllegalArgumentException("Amount nie może być ujemny.");

        // ewentualny JOB
        Long jobIdL = null;
        if ((f.getTitle() != null && !f.getTitle().isBlank()) || f.getMinSalary() != null || f.getMaxSalary() != null) {
            pgRepo.insertJobAutoId((f.getTitle()!=null && !f.getTitle().isBlank())?f.getTitle():"Unknown",
                    f.getMinSalary(), f.getMaxSalary());
            jobIdL = pgRepo.getLastJobIdFromSequence();
        }

        long t0 = System.nanoTime();
        pgRepo.insertEmployeeAutoId(
                f.getFirstName(), f.getLastName(), f.getEmail(), f.getPhone(),
                f.getHireDate(), jobIdL==null?null:jobIdL.intValue(), f.getDepartmentId());
        Long pgId = pgRepo.getLastEmployeeIdFromSequence();
        if (pgId == null) throw new IllegalStateException("Nie udało się pobrać employee_id z sekwencji.");
        double pgMs = (System.nanoTime() - t0) / 1_000_000.0;

        String neoDeptName = f.getDepartmentName(), neoLocation = f.getLocation();
        if ((neoDeptName==null||neoDeptName.isBlank()||neoLocation==null||neoLocation.isBlank()) && f.getDepartmentId()!=null) {
            var info = pgRepo.getDeptInfo(f.getDepartmentId());
            if (info!=null) {
                if (neoDeptName==null||neoDeptName.isBlank()) neoDeptName = info.getName();
                if (neoLocation==null||neoLocation.isBlank()) neoLocation = info.getLocation();
            }
        }

        long t1 = System.nanoTime();
        neoRepo.upsertFull(pgId, propsP(f.getFirstName(), f.getLastName(), f.getEmail(), f.getPhone(), f.getHireDate()),
                propsJ(f.getTitle(), f.getMinSalary(), f.getMaxSalary()),
                propsD(neoDeptName, neoLocation),
                propsS(f.getAmount(), f.getFromDate()));
        double neoMs = (System.nanoTime() - t1) / 1_000_000.0;

        return new Result(pgMs, neoMs, pgId);
    }

    /* ===== EDIT ===== */
    @Transactional("transactionManager")
    public Result editBoth(EditEmployeeForm f) {
        if (f.getEmployeeId() == null) throw new IllegalArgumentException("Brak employeeId.");
        if (f.getFirstName() != null && !f.getFirstName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("First name ma zły format.");
        if (f.getLastName() != null && !f.getLastName().matches("^[\\p{L}][\\p{L} .'-]{1,49}$"))
            throw new IllegalArgumentException("Last name ma zły format.");
        if (f.getEmail() != null && !f.getEmail().matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Email ma zły format.");
        if (f.getPhone() != null && !f.getPhone().isBlank() && !f.getPhone().matches("^[0-9]{6,20}$"))
            throw new IllegalArgumentException("Phone: tylko cyfry (6–20).");
        if (f.getMinSalary() != null && f.getMaxSalary() != null && f.getMinSalary() >= f.getMaxSalary())
            throw new IllegalArgumentException("Min salary musi być mniejsze niż Max salary.");
        if (f.getAmount() != null && f.getAmount() < 0)
            throw new IllegalArgumentException("Amount nie może być ujemny.");

        long t0 = System.nanoTime();
        pgRepo.updateEmployee(f.getEmployeeId(), f.getFirstName(), f.getLastName(), f.getEmail(),
                f.getPhone(), f.getHireDate(), f.getDepartmentId());
        double pgMs = (System.nanoTime() - t0) / 1_000_000.0;

        // Neo – dept fallback z PG jeśli brak
        String neoDeptName = f.getDepartmentName(), neoLocation = f.getLocation();
        if ((neoDeptName==null||neoDeptName.isBlank()||neoLocation==null||neoLocation.isBlank())
                && f.getDepartmentId()!=null) {
            var info = pgRepo.getDeptInfo(f.getDepartmentId());
            if (info!=null) {
                if (neoDeptName==null||neoDeptName.isBlank()) neoDeptName = info.getName();
                if (neoLocation==null||neoLocation.isBlank()) neoLocation = info.getLocation();
            }
        }

        long t1 = System.nanoTime();
        neoRepo.updateFull(f.getEmployeeId(),
                propsP(f.getFirstName(), f.getLastName(), f.getEmail(), f.getPhone(), f.getHireDate()),
                propsJ(f.getTitle(), f.getMinSalary(), f.getMaxSalary()),
                propsD(neoDeptName, neoLocation),
                propsS(f.getAmount(), f.getFromDate()));
        double neoMs = (System.nanoTime() - t1) / 1_000_000.0;

        return new Result(pgMs, neoMs, f.getEmployeeId());
    }

    /* ===== DELETE ===== */
    @Transactional("transactionManager")
    public Result deleteBoth(long employeeId) {
        long t0 = System.nanoTime();
        int rows = pgRepo.deleteEmployee(employeeId);
        double pgMs = (System.nanoTime() - t0) / 1_000_000.0;

        long t1 = System.nanoTime();
        neoRepo.deleteByEmployeeId(employeeId);
        double neoMs = (System.nanoTime() - t1) / 1_000_000.0;

        if (rows == 0) throw new IllegalArgumentException("Brak pracownika o ID " + employeeId + " w PG.");
        return new Result(pgMs, neoMs, employeeId);
    }

    /* ===== Helpers ===== */
    private Map<String,Object> propsP(String fn, String ln, String em, String ph, java.time.LocalDate hd) {
        Map<String,Object> m = new HashMap<>();
        if (fn!=null) m.put("imię", fn);
        if (ln!=null) m.put("nazwisko", ln);
        if (em!=null) m.put("email", em);
        if (ph!=null && !ph.isBlank()) m.put("telefon", ph);
        if (hd!=null) m.put("data_zatrudnienia", hd.toString());
        return m.isEmpty()? null : m;
        }
    private Map<String,Object> propsJ(String t, Long min, Long max) {
        if (t==null && min==null && max==null) return null;
        Map<String,Object> m = new HashMap<>();
        if (t!=null && !t.isBlank()) m.put("tytuł", t);
        if (min!=null) m.put("min_pensja", min);
        if (max!=null) m.put("max_pensja", max);
        return m.isEmpty()? null : m;
    }
    private Map<String,Object> propsD(String name, String loc) {
        if ((name==null||name.isBlank()) && (loc==null||loc.isBlank())) return null;
        Map<String,Object> m = new HashMap<>();
        if (name!=null && !name.isBlank()) m.put("nazwa", name);
        if (loc!=null && !loc.isBlank())  m.put("lokalizacja", loc);
        return m.isEmpty()? null : m;
    }
    private Map<String,Object> propsS(Long amount, java.time.LocalDate from) {
        if (amount==null && from==null) return null;
        Map<String,Object> m = new HashMap<>();
        if (amount!=null) m.put("kwota", amount);
        if (from!=null)   m.put("od", from.toString());
        return m.isEmpty()? null : m;
    }
}
