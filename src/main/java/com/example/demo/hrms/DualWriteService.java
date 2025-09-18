package com.example.demo.hrms;

import com.example.demo.graph.NeoEmployeeRepository;
import com.example.demo.pg.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Zapis do PG + Neo4j z pomiarem czasów.
 * Uwaga: Neo4j – właściwości po polsku (imię, nazwisko, telefon, data_zatrudnienia),
 * żeby zapytania z repo je widziały.
 */
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

    public Result addToBoth(AddEmployeeForm f) {
        // ===== PG: INSERT + RETURNING =====
        long t0 = System.nanoTime();
        Long pgId = pgRepo.insertEmployeeReturningId(
                f.getEmployeeId(),
                f.getFirstName(),
                f.getLastName(),
                f.getEmail(),
                f.getPhone(),
                f.getHireDate(),
                f.getJobId(),
                f.getDepartmentId()
        );
        double pgMs = (System.nanoTime() - t0) / 1_000_000.0;

        Long idForNeo = pgId;

        // ===== Neo4j: upsert :Pracownik (polskie klucze) =====
        long t1 = System.nanoTime();
        Map<String,Object> props = new HashMap<>();
        if (f.getFirstName() != null) props.put("imię", f.getFirstName());
        if (f.getLastName()  != null) props.put("nazwisko", f.getLastName());
        if (f.getEmail()     != null) props.put("email", f.getEmail());
        if (f.getPhone()     != null) props.put("telefon", f.getPhone());
        if (f.getHireDate()  != null) props.put("data_zatrudnienia", f.getHireDate().toString());
        // (relacje Stanowisko/Dział/Wynagrodzenie pomijamy – tabela i tak je pokazuje opcjonalnie)

        props.entrySet().removeIf(e -> e.getValue() == null);
        neoRepo.upsertByEmployeeId(idForNeo, props);
        double neoMs = (System.nanoTime() - t1) / 1_000_000.0;

        return new Result(pgMs, neoMs, idForNeo);
    }
}
