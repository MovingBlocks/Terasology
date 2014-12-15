/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.internal.adapter;

import com.sun.istack.internal.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Limeth
 */
public class BasicCommandArgumentAdapter {
    public static final CommandArgumentAdapter<Long> LONG = new CommandArgumentAdapter<Long>() {
        @Override
        public Long parse(@NotNull String composed) {
            return Long.parseLong(composed);
        }

        @Override
        public String compose(@NotNull Long parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Integer> INTEGER = new CommandArgumentAdapter<Integer>() {
        @Override
        public Integer parse(@NotNull String composed) {
            return Integer.parseInt(composed);
        }

        @Override
        public String compose(@NotNull Integer parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Short> SHORT = new CommandArgumentAdapter<Short>() {
        @Override
        public Short parse(@NotNull String composed) {
            return Short.parseShort(composed);
        }

        @Override
        public String compose(@NotNull Short parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Byte> BYTE = new CommandArgumentAdapter<Byte>() {
        @Override
        public Byte parse(@NotNull String composed) {
            return Byte.parseByte(composed);
        }

        @Override
        public String compose(@NotNull Byte parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Character> CHARACTER = new CommandArgumentAdapter<Character>() {
        @Override
        public Character parse(@NotNull String composed) {
            if (composed.length() != 1) {
                throw new IllegalArgumentException("The string to parse must be of length 1");
            }

            return composed.charAt(0);
        }

        @Override
        public String compose(@NotNull Character parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Double> DOUBLE = new CommandArgumentAdapter<Double>() {
        @Override
        public Double parse(@NotNull String composed) {
            return Double.parseDouble(composed);
        }

        @Override
        public String compose(@NotNull Double parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Float> FLOAT = new CommandArgumentAdapter<Float>() {
        @Override
        public Float parse(@NotNull String composed) {
            return Float.parseFloat(composed);
        }

        @Override
        public String compose(@NotNull Float parsed) {
            return parsed.toString();
        }
    };

    public static final CommandArgumentAdapter<Boolean> BOOLEAN = new CommandArgumentAdapter<Boolean>() {
        @Override
        public Boolean parse(@NotNull String composed) {
            return Boolean.parseBoolean(composed);
        }

        @Override
        public String compose(@NotNull Boolean parsed) {
            return parsed.toString();
        }
    };

    public static CommandArgumentAdapter[] values() {
        return new CommandArgumentAdapter[] {
                LONG, INTEGER, SHORT, BYTE, CHARACTER, DOUBLE, FLOAT, BOOLEAN
        };
    }

    public static Map<Class<?>, CommandArgumentAdapter> map() {
        Map<Class<?>, CommandArgumentAdapter> map = new HashMap<>();

        map.put(Long.class, LONG);
        map.put(Integer.class, INTEGER);
        map.put(Short.class, SHORT);
        map.put(Byte.class, BYTE);
        map.put(Character.class, CHARACTER);
        map.put(Double.class, DOUBLE);
        map.put(Float.class, FLOAT);
        map.put(Boolean.class, BOOLEAN);

        return map;
    }
}
