package com.digit.app.generator;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomGenerator implements PKGenerator {
    private final int[] numbers;
    private int index = -1;

    public RandomGenerator() {
        numbers = new int[getTotal()];
        Arrays.setAll(numbers, i -> i+1);
        shuffleArray(numbers);
    }
    @Override
    public int generatePK() {
        index++;
        return numbers[index];
    }

    /**
     * Fisher–Yates shuffle array function
     */
    private static void shuffleArray(int[] array) {
        int index;
        Random randomNumberGenerator = new Random(ThreadLocalRandom.current().nextInt());
        for (int i = array.length - 1; i > 0; i--) {
            index = randomNumberGenerator.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }
}
