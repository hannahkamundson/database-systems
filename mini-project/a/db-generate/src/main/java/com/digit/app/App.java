package com.digit.app;


import com.digit.app.generator.PKGenerator;
import com.digit.app.generator.RandomGenerator;
import com.digit.app.generator.SortedGenerator;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class App {
    public static void main(String[] args) {
        log.info("Starting benchmark test.");
        try (DBConnection conn = new DBConnection()) {
            // Sorted PK
            runAllTests("Sorted", conn, SortedGenerator::new);

            // Randomly generated PK
            runAllTests("Randomly generated", conn, RandomGenerator::new);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Closing benchmark test.");
    }

    private static void runAllTests(String name,
                                    DBConnection conn,
                                    Supplier<PKGenerator> createGeneratorFn) throws SQLException {
        // No index
        App.runTest("%s without extra index".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::noIndex,
                true
        );

        // Index A before
        App.runTest("%s with index A before".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createAIndex,
                true
        );

        // Index A after
        App.runTest("%s with index A after".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createAIndex,
                false
        );

        // Index B before
        App.runTest("%s with index B before".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createBIndex,
                true
        );

        // Index B after
        App.runTest("%s with index B after".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createBIndex,
                false
        );

        // Index A & B before
        App.runTest("%s with index A & B before".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createABIndex,
                true
        );

        // Index A & B after
        App.runTest("%s with index A & B after".formatted(name),
                conn,
                createGeneratorFn,
                DBConnection::createABIndex,
                false
        );
    }

    private static void runTest(String name,
                                DBConnection conn,
                                Supplier<PKGenerator> createGeneratorFn,
                                Consumer<DBConnection> createIndexFn,
                                boolean indexBefore) throws SQLException {
        log.info("--------- Starting: {} ---------", name);
        conn.createTable();
        if (indexBefore) {
            createIndexFn.accept(conn);
        }
        PKGenerator generator = createGeneratorFn.get();
        long startTime = System.currentTimeMillis();
        conn.upload(generator);
        if (!indexBefore) {
            createIndexFn.accept(conn);
        }

        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime)/1000;
        log.info("--------- Finished: {} in {} seconds ---------", name, duration);
    }
}
