package com.example.demo.pg;

/** Lekka projekcja listy stanowisk do selecta. */
public interface PgJobRow {
    Integer getJobId();
    String getJobTitle();
}
