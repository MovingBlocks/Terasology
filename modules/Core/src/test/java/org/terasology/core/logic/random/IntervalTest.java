/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.logic.random;

import org.junit.Test;

import static org.junit.Assert.*;

public class IntervalTest {

  private static final Interval TEST_INTERVAL = new Interval();
  static {
    TEST_INTERVAL.fixed = 1000;
    TEST_INTERVAL.minRandom = 500;
    TEST_INTERVAL.maxRandom = 1000;
  }

  private static final int NUMBER_OF_SAMPLES = 20;

  @Test
  public void sampleShouldBeWithinBounds() {
    final long minRange = TEST_INTERVAL.fixed + TEST_INTERVAL.minRandom;
    final long maxRange = TEST_INTERVAL.fixed + TEST_INTERVAL.maxRandom;
    long value;
    for (int i = 1; i <= NUMBER_OF_SAMPLES; i++) {
      value = TEST_INTERVAL.sample();
      assertTrue(String.format("Sample %d not in range [%d, %d]", value, minRange, maxRange),
          value >= minRange && value <= maxRange);
    }
  }
}
