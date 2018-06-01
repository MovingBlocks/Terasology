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
package org.terasology.recording;

import java.util.HashMap;
import java.util.Map;

/**
 * Saves important EntityRef ids so a mapping from recording to replay can be done.
 */
public final class EntityIdMap {

    // This map saves the ids from the current game, be it a record or replay. If it is a record, this data will be saved in a file.
    private static Map<String, Long> currentMap = new HashMap<>();

    // When a replay begins, this variable is loaded with the Recording's "currentMap".
    private static Map<String, Long> previousMap = new HashMap<>();


    private EntityIdMap() {

    }

    public static void add(String key, long id) {
        currentMap.put(key, id);
    }


    public static long getId(String key) {
        return currentMap.get(key);
    }

    static long getIdFromPrevious(String key) {
        return previousMap.get(key);
    }

    static Map<String, Long> getCurrentMap() {
        return currentMap;
    }

    static void setPreviousMap(Map<String, Long> map) {
        previousMap = map;
    }
}
