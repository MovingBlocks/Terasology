// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark;

/**
 * Benchmark is an abstract class which is used to implement one particular benchmark.
 */
public interface Benchmark {

    String getTitle();

    int getWarmupRepetitions();

    int[] getRepetitions();

    void setup();

    void prerun();

    void run();

    void postrun();

    void finish(boolean aborted);
}
