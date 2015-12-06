/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling;

import java.util.Map;
import java.util.Set;

/**
 */
public interface PersistedDataMap extends PersistedData {

    boolean has(String name);

    PersistedData get(String name);

    float getAsFloat(String name);

    int getAsInteger(String name);

    double getAsDouble(String name);

    long getAsLong(String name);

    String getAsString(String name);

    boolean getAsBoolean(String name);

    PersistedDataMap getAsMap(String name);

    PersistedDataArray getAsArray(String name);

    Set<Map.Entry<String, PersistedData>> entrySet();

}
