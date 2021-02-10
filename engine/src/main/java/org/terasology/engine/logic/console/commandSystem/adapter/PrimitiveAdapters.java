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
package org.terasology.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 */
public final class PrimitiveAdapters {

    public static final ParameterAdapter<Long> LONG = new ParameterAdapter<Long>() {
        @Override
        public Long parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Long.parseLong(raw);
        }

        @Override
        public String convertToString(Long value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Integer> INTEGER = new ParameterAdapter<Integer>() {
        @Override
        public Integer parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Integer.parseInt(raw);
        }

        @Override
        public String convertToString(Integer value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Short> SHORT = new ParameterAdapter<Short>() {
        @Override
        public Short parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Short.parseShort(raw);
        }

        @Override
        public String convertToString(Short value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Byte> BYTE = new ParameterAdapter<Byte>() {
        @Override
        public Byte parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Byte.parseByte(raw);
        }

        @Override
        public String convertToString(Byte value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Character> CHARACTER = new ParameterAdapter<Character>() {
        @Override
        public Character parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            if (raw.length() != 1) {
                throw new IllegalArgumentException("The string to parse must be of length 1");
            }

            return raw.charAt(0);
        }

        @Override
        public String convertToString(Character value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Double> DOUBLE = new ParameterAdapter<Double>() {
        @Override
        public Double parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Double.parseDouble(raw);
        }

        @Override
        public String convertToString(Double value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Float> FLOAT = new ParameterAdapter<Float>() {
        @Override
        public Float parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Float.parseFloat(raw);
        }

        @Override
        public String convertToString(Float value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<Boolean> BOOLEAN = new ParameterAdapter<Boolean>() {
        @Override
        public Boolean parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return Boolean.parseBoolean(raw);
        }

        @Override
        public String convertToString(Boolean value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value.toString();
        }
    };

    public static final ParameterAdapter<String> STRING = new ParameterAdapter<String>() {
        @Override
        public String parse(String raw) {
            Preconditions.checkNotNull(raw, "'raw' must not be null!");
            return raw;
        }

        @Override
        public String convertToString(String value) {
            Preconditions.checkNotNull(value, "'value' must not be null!");
            return value;
        }
    };

    public static final ImmutableMap<Class, ParameterAdapter> MAP =
            ImmutableMap.<Class, ParameterAdapter>builder()
                    .put(Long.class, LONG)
                    .put(Integer.class, INTEGER)
                    .put(Short.class, SHORT)
                    .put(Byte.class, BYTE)
                    .put(Character.class, CHARACTER)
                    .put(Double.class, DOUBLE)
                    .put(Float.class, FLOAT)
                    .put(Boolean.class, BOOLEAN)
                    .put(String.class, STRING).build();

    private PrimitiveAdapters() {
    }
}
