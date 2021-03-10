/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.utilities.time;

import org.junit.jupiter.api.Test;
import org.terasology.engine.utilities.time.DateTimeHelper;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DateTimeHelperTest {

    @Test
    public void testGetDeltaBetweenTimestamps() {
        assertEquals("00h 00m 01s", DateTimeHelper.getDeltaBetweenTimestamps(0, 1000));
        assertEquals("00h 00m 10s", DateTimeHelper.getDeltaBetweenTimestamps(0, 10 * 1000));
        assertEquals("00h 01m 00s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000));
        assertEquals("00h 11m 00s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000 * 11));
        assertEquals("01h 00m 00s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000 * 60));
        assertEquals("12h 34m 56s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000 * 60 * 12 + 60 * 1000 * 34 + 56 * 1000));

        assertEquals("1 Days 01h 00m 00s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000 * 60 * 25));
        assertEquals("1 Days 01h 20m 40s", DateTimeHelper.getDeltaBetweenTimestamps(0, 60 * 1000 * 60 * 24 + 60 * 1000 * 60 + 60 * 1000 * 20 + 40 * 1000));

        assertEquals("00h 00m 00s", DateTimeHelper.getDeltaBetweenTimestamps(1000, 1000));
        assertEquals("00h 00m 00s", DateTimeHelper.getDeltaBetweenTimestamps(50000, 50000));

        assertEquals("11h 01m 01s", DateTimeHelper.getDeltaBetweenTimestamps(1000 * 60 * 25, 1000 * 60 * 25 + 60 * 1000 * 60 * 11 + 60 * 1000 + 1000));
    }

    @Test
    public void testGetDeltaBetweenTimestampsWrongData() {

        Integer[][] wrongData = {
                {-1, 50000},
                {123, -1000},
                {-667, -1000},
                {123, 0},
                {12321321, 1231}
        };
        assertAll(
                Arrays.stream(wrongData)
                        .map((data) -> () ->
                                assertEquals("Wrong timestamp values: " + data[0] + " or " + data[1],
                                        assertThrows(IllegalArgumentException.class,
                                                () -> DateTimeHelper.getDeltaBetweenTimestamps(data[0], data[1])).getMessage())
                        ));
    }
}
