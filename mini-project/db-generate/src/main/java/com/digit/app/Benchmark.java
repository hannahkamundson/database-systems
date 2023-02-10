package com.digit.app;

import com.digit.app.generator.PKGenerator;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class Benchmark {

    public static long run(String name,
                           DBConnection conn,
                           Supplier<PKGenerator> pkGeneratorFn,
                           Consumer<DBConnection> createIndexFn,
                           boolean indexBeforeInsert) throws SQLException {
        log.info("--------- Starting: {} ---------", name);
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
        long duration = (endTime - startTime) / 1000;
        log.info("--------- Finished: {} in {} seconds ---------", name, duration);

        return duration;
    }
}
