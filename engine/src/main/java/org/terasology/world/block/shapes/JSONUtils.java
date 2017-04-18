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
package org.terasology.world.block.shapes;

import com.google.gson.JsonObject;

import java.util.Optional;

/**
 * FIXME find better location
 */
public class JSONUtils {

    public static boolean has(JsonObject jsonObj, String name) {
        return jsonObj.has(name);
    }

    public static boolean hasObject(JsonObject jsonObj, String name) {
        return jsonObj.has(name) && jsonObj.get(name).isJsonObject();
    }

    public static <E extends Enum<E>> E getEnumOrDefault(JsonObject jsonObj, String name, Class<E> enumClass, E defaultValue ) {
        if (jsonObj.has(name)) {
            return Enum.valueOf(enumClass, jsonObj.getAsJsonPrimitive(name).getAsString());
        } else {
            return defaultValue;
        }
    }

    public static <E extends Enum<E>> Optional<E> getEnum(JsonObject jsonObj, String name, Class<E> enumClass ) {
        if( jsonObj.has(name) ) {
            return Optional.of(Enum.valueOf(enumClass, jsonObj.getAsJsonPrimitive(name).getAsString()));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<String> getString(JsonObject jsonObj, String name) {
        if( jsonObj.has(name) ) {
            return Optional.of(jsonObj.getAsJsonPrimitive(name).getAsString());
        } else {
            return Optional.empty();
        }
    }
    
}
