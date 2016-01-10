/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.reflection.copy.strategy;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.terasology.reflection.copy.CopyStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 */
public class MapCopyStrategyTest {

    @Test
    public void testOrderedMapCopyStrategy() throws NoSuchMethodException {
        ReturnAsIsStrategy<String> keyStrategy = new ReturnAsIsStrategy<>();
        ReturnAsIsStrategy<Long> valueStrategy = new ReturnAsIsStrategy<>();

        Map<String, Long> originalOrderedMap = Maps.newLinkedHashMap();
        originalOrderedMap.put("one", 1L);
        originalOrderedMap.put("two", 2L);
        originalOrderedMap.put("three", 3L);
        originalOrderedMap.put("four", 4L);

        MapCopyStrategy<String, Long> strategy = new MapCopyStrategy<>(keyStrategy, valueStrategy);
        Map<String, Long> copiedMap = strategy.copy(originalOrderedMap);
        Set<String> keySet = copiedMap.keySet();
        List<String> keyList = new ArrayList<>(keySet);
        assertEquals(4, keyList.size());
        assertEquals("one", keyList.get(0));
        assertEquals("two", keyList.get(1));
        assertEquals("three", keyList.get(2));
        assertEquals("four", keyList.get(3));
        assertEquals(Long.valueOf(1), copiedMap.get("one"));
        assertEquals(Long.valueOf(2), copiedMap.get("two"));
        assertEquals(Long.valueOf(3), copiedMap.get("three"));
        assertEquals(Long.valueOf(4), copiedMap.get("four"));
    }

    /**
     * The default copy strategy - returns the original value.
     *
     * @param <T>
     */
    private static class ReturnAsIsStrategy<T> implements CopyStrategy<T> {

        @Override
        public T copy(T value) {
            return value;
        }
    }
}
