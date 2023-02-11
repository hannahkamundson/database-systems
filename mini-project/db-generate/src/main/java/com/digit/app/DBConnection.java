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

    public void queryA(int columnNumber) {
        log.info("Running query 1");

        // Sorry this is sloppy with the try catch
        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM benchmark WHERE benchmark.columnA = ?");
            st.setInt(1, columnNumber);
            ResultSet results = st.executeQuery();
            log.info("Result for query 1:\n{}", DBConnection.interpretResultSet(results));
            st.close();
            results.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Completed query 1");
    }

    public void queryB(int columnNumber) {
        log.info("Running query 2");

        // Sorry this is sloppy with the try catch
        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM benchmark WHERE benchmark.columnB = ?");

            st.setInt(1, columnNumber);
            ResultSet results = st.executeQuery();
            log.info("Result for query 2:\n{}", DBConnection.interpretResultSet(results));
            st.close();
            results.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Completed query 2");
    }

    public void queryAB(int columnNumber) {
        log.info("Running query 3");
        // Sorry this is sloppy with the try catch
        try {
            PreparedStatement st = conn.prepareStatement("SELECT * FROM benchmark WHERE benchmark.columnA = ? AND benchmark.columnB = ?");
            st.setInt(1, columnNumber);
            st.setInt(2, columnNumber);
            ResultSet results = st.executeQuery();
            log.info("Result for query 3:\n{}", DBConnection.interpretResultSet(results));
            st.close();
            results.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        log.info("Completed query 3");
    }

    private static String interpretResultSet(ResultSet result) throws SQLException {
        StringBuilder builder = new StringBuilder();
        while (result.next()) {
            int theKey = result.getInt("theKey");
            int columnA = result.getInt("columnA");
            int columnB = result.getInt("columnB");
            String filler = result.getString("filler");

            builder.append("%s, %s, %s, %s\n".formatted(theKey, columnA, columnB, filler));
        }

        String resultString = builder.toString();

        return resultString.isEmpty() ? "None" : resultString;
    }

    @Override
    public void close() throws SQLException {
        conn.close();
        log.info("Connection closed.");
    }
}
