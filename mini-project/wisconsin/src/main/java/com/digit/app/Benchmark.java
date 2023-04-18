package com.digit.app;

import com.digit.app.generator.PKGenerator;
import com.digit.app.generator.RandomGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Arrays;

@Slf4j
public class Benchmark {
    public static void createTables(DBConnection conn) throws SQLException {
        // Create the tables
//        Benchmark.createBaseTable(conn);
        Benchmark.createOtherTables(conn);

        //
    }

    private static void createBaseTable(DBConnection conn) throws SQLException {
        log.info("--------- Starting A' table creation ---------");
        String tableName = "Aprime";
        conn.createTable();

        // Upload data to Aprime
        PKGenerator generator = new RandomGenerator();
        conn.upload(generator);

        // Add index
        conn.createIndex(tableName);
    }

    private static void createOtherTables(DBConnection conn) throws SQLException {
        String baseTable = "Aprime";
        // Copy tables with indexes
//        conn.copyTableWithIndex(baseTable, "BPrime");


        // Copy table without indexes
        conn.copyTableWithoutIndex(baseTable, "A");
        conn.copyTableWithoutIndex(baseTable, "B");
        conn.copyTableWithoutIndex(baseTable, "C");
        conn.copyTableWithIndex(baseTable, "CPrime");
    }
}
