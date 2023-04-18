package com.digit.app;


import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class App {
    public static void main(String[] args) {
        log.info("Starting benchmark test.");
//        List<Benchmark.BenchmarkResult> benchmarkResults = new ArrayList<>();

        try (DBConnection conn = new DBConnection()) {
            // Create the first table
            Benchmark.createTables(conn);



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
//        log.info(benchmarkResults.toString());
        log.info("Closing benchmark test.");
    }
}
