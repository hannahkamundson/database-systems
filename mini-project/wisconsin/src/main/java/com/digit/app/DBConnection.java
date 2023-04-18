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
        log.info("Dropping benchmark table...");
        PreparedStatement drop = conn.prepareStatement("DROP TABLE IF EXISTS Aprime;");
        drop.execute();
        drop.close();
        log.info("Successfully dropped benchmark table.");
        log.info("Creating benchmark table...");
        PreparedStatement make = conn.prepareStatement("""
                CREATE TABLE Aprime (
                pk INTEGER PRIMARY KEY,
                ht INTEGER,
                tt INTEGER,
                ot INTEGER,
                hund INTEGER,
                ten INTEGER,
                filler CHAR(204)
                );
                """);
        make.execute();
        log.info("Successfully created benchmark table.");
        make.close();
    }

    public void upload(PKGenerator generator) throws SQLException {
        log.info("Inserting and generating {} entries for {}...", generator.getTotal(), "Aprime");
        PreparedStatement st = conn.prepareStatement("INSERT INTO Aprime VALUES (?, ?, ?, ?, ?, ?, ?)");
        Random randomNumberGenerator = new Random(ThreadLocalRandom.current().nextInt());

        for (int i = 0; i < generator.getTotal(); i++) {
            st.setInt(1, generator.generatePK());

            st.setInt(2, randomNumberGenerator.nextInt(99999));
            st.setInt(3, randomNumberGenerator.nextInt(9999));
            st.setInt(4, randomNumberGenerator.nextInt(999));
            st.setInt(5, randomNumberGenerator.nextInt(99));
            st.setInt(6, randomNumberGenerator.nextInt(9));
            st.setString(7, "I'm a comment");
            st.addBatch();

            if (i % 500000 == 0) {
                log.info("Finished entry {}", i);
            }
        }

        log.info("Submitting batch to database {}.", "Aprime");
        st.executeBatch();
        st.close();
        log.info("Completed inserts to {}.", "Aprime");
    }

    public void createIndex(String tableName) {
        log.info("Creating index {}...", tableName);
        try {
            Statement st = conn.createStatement();
            st.executeUpdate("CREATE INDEX index_ht_" + tableName + " ON " + tableName + " (ht);");
            st.executeUpdate("CREATE INDEX index_tt_" + tableName + " ON " + tableName + " (tt);");
            st.executeUpdate("CREATE INDEX index_ot_" + tableName + " ON " + tableName + " (ot);");
            st.executeUpdate("CREATE INDEX index_hund_" + tableName + " ON " + tableName + " (hund);");
            st.executeUpdate("CREATE INDEX index_ten_" + tableName + " ON " + tableName + " (ten);");
            st.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Finished creation of index {}.", tableName);
    }

    public void copyTableWithIndex(String from, String to) throws SQLException {
        log.info("Copying {} to {}...", from, to);
        Statement st = conn.createStatement();
        st.executeUpdate("DROP TABLE IF EXISTS " + to + ";");
        st.executeUpdate("CREATE TABLE " + to + " (LIKE " + from + " INCLUDING ALL);");
        st.executeUpdate("INSERT INTO " + to + " SELECT * FROM " + from + ";");
        st.close();

        log.info("Finished copying {} to {}", from, to);
    }

    public void copyTableWithoutIndex(String from, String to) throws SQLException {
        log.info("Copying {} to {}...", from, to);
        Statement st = conn.createStatement();
        st.executeUpdate("DROP TABLE IF EXISTS " + to + ";");
        st.executeUpdate("CREATE TABLE " + to + " AS TABLE " + from + ";");
        st.executeUpdate("ALTER TABLE " + to + " ADD PRIMARY KEY (pk);");
        st.close();

        log.info("Finished copying {} to {}", from, to);
    }



    @Override
    public void close() throws SQLException {
        conn.close();
        log.info("Connection closed.");
    }
}
