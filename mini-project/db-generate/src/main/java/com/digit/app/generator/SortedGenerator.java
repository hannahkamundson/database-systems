package com.digit.app.generator;

public class SortedGenerator implements PKGenerator {
    private int count = 0;

    @Override
    public int generatePK() {
        count++;
        return count;
    }
}
