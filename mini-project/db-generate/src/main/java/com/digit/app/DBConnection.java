package com.digit.app;

import com.digit.app.generator.PKGenerator;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class DBConnection implements AutoCloseable {
    private final Connection conn;

    public DBConnection() throws SQLException {
        conn = DBConnection.createConnection();
    }

    private static Connection createConnection() throws SQLException {
        log.info("Creating Postgres connection...");
        String url = "jdbc:postgresql://localhost:5432/cs386d";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres");
        Connection con = DriverManager.getConnection(url, props);
        log.info("Successfully connected to postgres.");

        return con;
    }

    public void createTable() throws SQLException {
        Statement statement = conn.createStatement();
        log.info("Dropping benchmark table...");
        statement.executeUpdate("DROP TABLE IF EXISTS benchmark;");
        log.info("Successfully dropped benchmark table.");
        log.info("Creating benchmark table...");
        statement.executeUpdate("""
                CREATE TABLE benchmark (
                theKey INTEGER PRIMARY KEY,
                columnA INTEGER,
                columnB INTEGER,
                filler CHAR(247)
                );
                """);
        log.info("Successfully created benchmark table.");
        statement.close();
    }

    public void noIndex() {
        // Do nothing
    }

    public void createAIndex() {
        log.info("Creating index...");
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE INDEX index_a ON benchmark (columna);");
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Finished creation of index.");
    }

    public void createBIndex() {
        log.info("Creating index...");
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE INDEX index_a ON benchmark (columnb);");
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Finished creation of index.");
    }

    public void createABIndex() {
        log.info("Creating index...");
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE INDEX index_a ON benchmark (columna, columnb);");
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Finished creation of index.");
    }

    public void upload(PKGenerator generator) throws SQLException {
        log.info("Inserting and generating {} entries...", generator.getTotal());
        PreparedStatement st = conn.prepareStatement("INSERT INTO benchmark VALUES (?, ?, ?, ?)");
        Random randomNumberGenerator = new Random(ThreadLocalRandom.current().nextInt());

        for (int i = 0; i < generator.getTotal(); i++) {
            st.setInt(1, generator.generatePK());

            st.setInt(2, randomNumberGenerator.nextInt(50000) + 1);
            st.setInt(3, randomNumberGenerator.nextInt(50000) + 1);
            st.setString(4, "I'm a comment");
            st.addBatch();

            if (i % 500000 == 0) {
                log.info("Finished entry {}", i);
            }
        }

        log.info("Submitting batch to database.");
        st.executeBatch();
        st.close();
        log.info("Completed inserts.");
    }

    @Override
    public void close() throws SQLException {
        conn.close();
        log.info("Connection closed.");
    }
}
