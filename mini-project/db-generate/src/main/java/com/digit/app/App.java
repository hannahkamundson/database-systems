package com.digit.app;


import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class App {
    public static void main(String[] args) {
        log.info("Starting benchmark test.");
        List<Benchmark.BenchmarkResult> benchmarkResults = new ArrayList<>();

        try (DBConnection conn = new DBConnection()) {

            // For every sorting type
            for (SortType sortType : SortType.values()) {
                // Create one of the tests and see how it goes
                for (IndexType indexType : IndexType.values()) {
//            for (SortType sortType : List.of(SortType.RANDOM)) {
//                for (IndexType indexType : List.of(IndexType.INDEX_B_BEFORE)) {
                    // The name we will be using for the test
                    String name = "%s %s".formatted(sortType, indexType);

                    // Run the benchmark test
                    Benchmark.BenchmarkResult result = Benchmark.run(name, conn, SortType.getGeneratorFn(sortType),
                            IndexType.getIndexFn(indexType), IndexType.createIndexBeforeInsert(indexType));

                    benchmarkResults.add(result);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.info(benchmarkResults.toString());
        log.info("Closing benchmark test.");
    }
}
