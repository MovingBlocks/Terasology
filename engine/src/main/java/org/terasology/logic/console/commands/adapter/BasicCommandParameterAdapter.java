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
package org.terasology.logic.console.commands.adapter;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Limeth
 */
public class BasicCommandParameterAdapter {
    public static final CommandParameterAdapter<Long> LONG = new CommandParameterAdapter<Long>() {
        @Override
        public Long parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Long.parseLong(composed);
        }

        @Override
        public String compose(Long parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Integer> INTEGER = new CommandParameterAdapter<Integer>() {
        @Override
        public Integer parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Integer.parseInt(composed);
        }

        @Override
        public String compose(Integer parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Short> SHORT = new CommandParameterAdapter<Short>() {
        @Override
        public Short parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Short.parseShort(composed);
        }

        @Override
        public String compose(Short parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Byte> BYTE = new CommandParameterAdapter<Byte>() {
        @Override
        public Byte parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Byte.parseByte(composed);
        }

        @Override
        public String compose(Byte parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Character> CHARACTER = new CommandParameterAdapter<Character>() {
        @Override
        public Character parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            if (composed.length() != 1) {
                throw new IllegalArgumentException("The string to parse must be of length 1");
            }

            return composed.charAt(0);
        }

        @Override
        public String compose(Character parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Double> DOUBLE = new CommandParameterAdapter<Double>() {
        @Override
        public Double parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Double.parseDouble(composed);
        }

        @Override
        public String compose(Double parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Float> FLOAT = new CommandParameterAdapter<Float>() {
        @Override
        public Float parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Float.parseFloat(composed);
        }

        @Override
        public String compose(Float parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<Boolean> BOOLEAN = new CommandParameterAdapter<Boolean>() {
        @Override
        public Boolean parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return Boolean.parseBoolean(composed);
        }

        @Override
        public String compose(Boolean parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed.toString();
        }
    };

    public static final CommandParameterAdapter<String> STRING = new CommandParameterAdapter<String>() {
        @Override
        public String parse(String composed) {
            Preconditions.checkNotNull(composed, "'composed' must not be null!");
            return composed;
        }

        @Override
        public String compose(String parsed) {
            Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
            return parsed;
        }
    };

    public static CommandParameterAdapter[] values() {
        return new CommandParameterAdapter[] {
                LONG, INTEGER, SHORT, BYTE, CHARACTER, DOUBLE, FLOAT, BOOLEAN, STRING
        };
    }

    public static Map<Class<?>, CommandParameterAdapter> map() {
        Map<Class<?>, CommandParameterAdapter> map = new HashMap<>();

        map.put(Long.class, LONG);
        map.put(Integer.class, INTEGER);
        map.put(Short.class, SHORT);
        map.put(Byte.class, BYTE);
        map.put(Character.class, CHARACTER);
        map.put(Double.class, DOUBLE);
        map.put(Float.class, FLOAT);
        map.put(Boolean.class, BOOLEAN);
        map.put(String.class, STRING);

        return map;
    }
}
