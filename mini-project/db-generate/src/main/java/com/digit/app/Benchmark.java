package com.digit.app;

import com.digit.app.generator.PKGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class Benchmark {
    @Data
    @AllArgsConstructor
    public static class BenchmarkResult {
        String name;
        double creationDuration;

        double[] queryAResults;

        double[] queryBResults;

        double[] queryABResults;

        public String toString() {
            // If we ran the table querying, report results
            if (queryAResults.length > 1) {
                double avgA = Arrays.stream(queryAResults).average().orElse(Double.NaN);;
                double avgB = Arrays.stream(queryBResults).average().orElse(Double.NaN);;
                double avgAB = Arrays.stream(queryABResults).average().orElse(Double.NaN);;
                return """
                        \n----%s
                        creation time: %s
                        query a: %s
                        query b: %s
                        query ab: %s
                        avg a: %s
                        avg b: %s
                        avg ab: %s
                        
                        """.formatted(name,
                        creationDuration,
                        Arrays.toString(queryAResults),
                        Arrays.toString(queryBResults),
                        Arrays.toString(queryABResults),
                        avgA,
                        avgB,
                        avgAB);
            } else {
                return "\n----%s\ncreation time: %s\n".formatted(name, creationDuration);
            }
        }
    }

    private static final int[] benchmarkNumbers = new int[] {
            25000, 70, 100283, 435, 914324, 9999, 8, 8878728, 1082923, 23
    };

    public static BenchmarkResult run(String name,
                                      DBConnection conn,
                                      Supplier<PKGenerator> pkGeneratorFn,
                                      Consumer<DBConnection> createIndexFn,
                                      boolean indexBeforeInsert) throws SQLException {
        log.info("--------- Starting: {} ---------", name);
        // Run the benchmark to create the table
        double createDuration = createTableBenchmark(name, conn, pkGeneratorFn, createIndexFn, indexBeforeInsert);

        // Run the benchmarks for different queries
        // We don't need to run queries on identical tables that just had indexes created at different times, so we will
        // just do it on one

        // Not a good strategy here but just trying to get something out
        double[] aResults = new double[1];
        double[] bResults = new double[1];
        double[] abResults = new double[1];
        if (indexBeforeInsert) {
            aResults = queryTableBenchmark(name, conn::queryA, "a");
            bResults = queryTableBenchmark(name, conn::queryB, "b");
            abResults = queryTableBenchmark(name, conn::queryAB, "ab");
        }

        log.info("--------- Finished: {} in {} seconds ---------", name, createDuration);

        return new BenchmarkResult(name, createDuration, aResults, bResults, abResults);
    }

    private static double[] queryTableBenchmark(String tableIndexingName,
                                              Consumer<Integer> queryFn,
                                              String queryName) {
        double[] results = new double[10];

        for (int i = 0; i < benchmarkNumbers.length; i++) {
            int indexNumber = benchmarkNumbers[i];
            log.info("--------- Starting query {} index number {}: {} ---------", queryName, indexNumber, tableIndexingName);
            long startTime = System.currentTimeMillis();
            queryFn.accept(indexNumber);
            // store the duration
            long endTime = System.currentTimeMillis();
            double duration = (endTime - startTime) / (double) 1000;
            results[i] = duration;

            log.info("--------- Finished query {} index number {}: {} in {} seconds ---------", queryName, indexNumber, tableIndexingName, duration);
        }

        return results;
    }

    private static double createTableBenchmark(String name,
                                             DBConnection conn,
                                             Supplier<PKGenerator> pkGeneratorFn,
                                             Consumer<DBConnection> createIndexFn,
                                             boolean indexBeforeInsert) throws SQLException {
        log.info("--------- Starting table creation: {} ---------", name);
        conn.createTable();

        if (indexBeforeInsert) {
            // Get the index function associated with the index type and create the index
            createIndexFn.accept(conn);
        }

        // Get the generator associated with the sorting. Do we want to do random sort or real sort?
        PKGenerator generator = pkGeneratorFn.get();
        long startTime = System.currentTimeMillis();
        conn.upload(generator);

        if (!indexBeforeInsert) {
            // Get the index function associated with the index type and create the index
            createIndexFn.accept(conn);
        }

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / (double) 1000;
        log.info("--------- Ending table creation: {} in {} seconds ---------", name, duration);

        return duration;
    }
}
