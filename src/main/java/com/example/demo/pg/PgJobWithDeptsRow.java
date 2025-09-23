package com.example.demo.pg;

public interface PgJobWithDeptsRow {
    Long getJobId();
    String getTitle();
    String getDeptIds(); // "1,2,3" – lista ID działów
}
