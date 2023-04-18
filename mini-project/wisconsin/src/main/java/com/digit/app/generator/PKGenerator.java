package com.digit.app.generator;

public interface PKGenerator {
    default int getTotal() {
        return 5000000;
    }

    int generatePK();
}
