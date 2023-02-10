package com.digit.app;

import java.util.function.Consumer;

public enum IndexType {
    NO_INDEX,
    INDEX_A_BEFORE,
    INDEX_A_AFTER,
    INDEX_B_BEFORE,
    INDEX_B_AFTER,
    INDEX_A_B_BEFORE,
    INDEX_A_B_AFTER;

    /**
     * What index function should be used based on the enum?
     */
    public static Consumer<DBConnection> getIndexFn(IndexType indexType) {
        return switch (indexType) {
            case NO_INDEX -> DBConnection::noIndex;
            case INDEX_A_BEFORE, INDEX_A_AFTER -> DBConnection::createAIndex;
            case INDEX_B_BEFORE, INDEX_B_AFTER -> DBConnection::createBIndex;
            case INDEX_A_B_BEFORE, INDEX_A_B_AFTER -> DBConnection::createABIndex;
        };
    }

    /**
     * Should the index occur before or after the insert?
     */
    public static boolean createIndexBeforeInsert(IndexType indexType) {
        return switch (indexType) {
            case INDEX_A_AFTER, INDEX_B_AFTER, INDEX_A_B_BEFORE -> false;
            default -> true;
        };
    }
}
