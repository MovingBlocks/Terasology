// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.random;

import org.terasology.context.annotation.API;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of objects with associated probabilities, with the ability to sample an object at random from those probabilities.
 * The probabilities are {@code double}s with no inherent scale, only measured relative to each other.
 */
@API
public class DiscreteDistribution<T> {
    private final List<T> entries = new ArrayList<>();
    private final List<Double> cumulativeChances = new ArrayList<>();
    private double chanceSum = 0;

    public void add(T value, double prob) {
        chanceSum += prob;
        entries.add(value);
        cumulativeChances.add(chanceSum);
    }

    public T sample(Random rand) {
        if (entries.isEmpty()) {
            return null;
        }

        double n = rand.nextDouble(0, chanceSum);

        // Do a binary search to find the first element with a cumulative probability greater than n
        int i = entries.size() / 2;
        int d = entries.size() / 2;
        while (true) {
            if (cumulativeChances.get(i) > n && (i == 0 || cumulativeChances.get(i - 1) <= n)) {
                return entries.get(i);
            } else {
                d /= 2;
                if (d <= 0) {
                    d = 1;
                }
                if (cumulativeChances.get(i) > n) {
                    i -= d;
                } else {
                    i += d;
                }

                // If it went out-of-bounds, use the value on the end
                if (i < 0) {
                    return entries.get(0);
                }
                if (i >= cumulativeChances.size()) {
                    return entries.get(entries.size() - 1);
                }
            }
        }
    }
}
