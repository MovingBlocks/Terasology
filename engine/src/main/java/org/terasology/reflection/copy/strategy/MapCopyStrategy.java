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
import org.terasology.reflection.copy.CopyStrategy;

import java.util.Map;

/**
 * @author Immortius
 */
public class MapCopyStrategy<K, V> implements CopyStrategy<Map<K, V>> {

    private final CopyStrategy<K> keyStrategy;
    private final CopyStrategy<V> valueStrategy;

    public MapCopyStrategy(CopyStrategy<K> keyStrategy, CopyStrategy<V> valueStrategy) {
        this.keyStrategy = keyStrategy;
        this.valueStrategy = valueStrategy;
    }

    @Override
    public Map<K, V> copy(Map<K, V> map) {
        if (map != null) {
            Map<K, V> result = Maps.newLinkedHashMap();
            for (Map.Entry<K, V> entry : map.entrySet()) {
                result.put(keyStrategy.copy(entry.getKey()), valueStrategy.copy(entry.getValue()));
            }
            return result;
        }
        return null;
    }
}
