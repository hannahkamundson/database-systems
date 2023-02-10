package com.digit.app;

import com.digit.app.generator.PKGenerator;
import com.digit.app.generator.RandomGenerator;
import com.digit.app.generator.SortedGenerator;

import java.util.function.Supplier;

public enum SortType {
    SORT,
    RANDOM;

    /**
     * What generator should be created based on the enum inserted?
     */
    public static Supplier<PKGenerator> getGeneratorFn(SortType sortType) {
        return switch (sortType) {
            case SORT -> SortedGenerator::new;
            case RANDOM -> RandomGenerator::new;
        };
    }
}
