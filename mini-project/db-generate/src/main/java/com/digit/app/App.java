package com.digit.app;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class App {
    @Value
    private static class BenchmarkResult {
        SortType sortType;
        IndexType indexType;
        long duration;

        public String toString() {
            return "%s %s: %s".formatted(sortType, indexType, duration);
        }
    }
    public static void main(String[] args) {
        log.info("Starting benchmark test.");
        List<BenchmarkResult> benchmarkResults = new ArrayList<>();

        try (DBConnection conn = new DBConnection()) {

            // For every sorting type
            for (SortType sortType : SortType.values()) {
                // Create one of the tests and see how it goes
                for (IndexType indexType : IndexType.values()) {
                    // The name we will be using for the test
                    String name = "%s %s".formatted(sortType, indexType);

                    // Run the benchmark test
                    long duration = Benchmark.run(name, conn, SortType.getGeneratorFn(sortType),
                            IndexType.getIndexFn(indexType), IndexType.createIndexBeforeInsert(indexType));

                    benchmarkResults.add(new BenchmarkResult(sortType, indexType, duration));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info(benchmarkResults.toString());
        log.info("Closing benchmark test.");
    }
}
