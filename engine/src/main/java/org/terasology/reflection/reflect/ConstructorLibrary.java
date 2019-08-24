/*
 * Copyright 2018 MovingBlocks
 * Copyright (C) 2011 Google Inc.
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
 *
 * Based on Gson v2.6.2 com.google.gson.internal.ConstructorConstructor
 */
package org.terasology.reflection.reflect;

import org.terasology.persistence.typeHandling.InstanceCreator;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.internal.UnsafeAllocator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConstructorLibrary {
    private final Map<Type, InstanceCreator<?>> instanceCreators;

    public ConstructorLibrary(Map<Type, InstanceCreator<?>> instanceCreators) {
        this.instanceCreators = instanceCreators;
    }

    public <T> ObjectConstructor<T> get(TypeInfo<T> typeInfo) {
        final Type type = typeInfo.getType();
        final Class<? super T> rawType = typeInfo.getRawType();

        // first try an instance creator

        @SuppressWarnings("unchecked") // types must agree
        final InstanceCreator<T> typeCreator = (InstanceCreator<T>) instanceCreators.get(type);
        if (typeCreator != null) {
            return () -> typeCreator.createInstance(type);
        }

        // Next try raw type match for instance creators
        @SuppressWarnings("unchecked") // types must agree
        final InstanceCreator<T> rawTypeCreator =
                (InstanceCreator<T>) instanceCreators.get(rawType);
        if (rawTypeCreator != null) {
            return () -> rawTypeCreator.createInstance(type);
        }

        ObjectConstructor<T> defaultConstructor = newDefaultConstructor(rawType);
        if (defaultConstructor != null) {
            return defaultConstructor;
        }

        ObjectConstructor<T> defaultImplementation = newDefaultImplementationConstructor(type, rawType);
        if (defaultImplementation != null) {
            return defaultImplementation;
        }

        return newUnsafeAllocator(typeInfo);
    }

    private <T> ObjectConstructor<T> newUnsafeAllocator(TypeInfo<T> typeInfo) {
        return new ObjectConstructor<T>() {
            private final UnsafeAllocator unsafeAllocator = UnsafeAllocator.create();
            @SuppressWarnings("unchecked")
            @Override public T construct() {
                try {
                    Object newInstance = unsafeAllocator.newInstance(typeInfo.getRawType());
                    return (T) newInstance;
                } catch (Exception e) {
                    throw new RuntimeException("Unable to create an instance of " + typeInfo.getType() +
                            ". Registering an InstanceCreator for this type may fix this problem.", e);
                }
            }
        };
    }

    private <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> rawType) {
        @SuppressWarnings("unchecked") // T is the same raw type as is requested
                Constructor<T> constructor = (Constructor<T>) Arrays.stream(rawType.getDeclaredConstructors())
                .min(Comparator.comparingInt(c -> c.getParameterTypes().length))
                .orElse(null);

        if (constructor == null || constructor.getParameterTypes().length != 0) {
            return null;
        }

        if (!constructor.isAccessible()) {
            constructor.setAccessible(true);
        }
        return () -> {
            try {
                return constructor.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Failed to invoke " + constructor + " with no args",
                        e.getTargetException());
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        };
    }

    /**
     * Constructors for common interface types like Map and List and their
     * subtypes.
     */
    @SuppressWarnings("unchecked") // use runtime checks to guarantee that 'T' is what it is
    private <T> ObjectConstructor<T> newDefaultImplementationConstructor(
            final Type type, Class<? super T> rawType) {
        if (Collection.class.isAssignableFrom(rawType)) {
            if (SortedSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new TreeSet<Object>();
                    }
                };
            } else if (EnumSet.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @SuppressWarnings("rawtypes")
                    @Override
                    public T construct() {
                        if (type instanceof ParameterizedType) {
                            Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
                            if (elementType instanceof Class) {
                                return (T) EnumSet.noneOf((Class) elementType);
                            } else {
                                throw new SerializationException("Invalid EnumSet type: " + type.toString());
                            }
                        } else {
                            throw new SerializationException("Invalid EnumSet type: " + type.toString());
                        }
                    }
                };
            } else if (Set.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new LinkedHashSet<Object>();
                    }
                };
            } else if (Queue.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new LinkedList<Object>();
                    }
                };
            } else {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new ArrayList<Object>();
                    }
                };
            }
        }

        if (Map.class.isAssignableFrom(rawType)) {
            if (ConcurrentNavigableMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new ConcurrentSkipListMap<Object, Object>();
                    }
                };
            } else if (ConcurrentMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new ConcurrentHashMap<Object, Object>();
                    }
                };
            } else if (SortedMap.class.isAssignableFrom(rawType)) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new TreeMap<Object, Object>();
                    }
                };
            } else if (type instanceof ParameterizedType && !(String.class.isAssignableFrom(
                    TypeInfo.of(((ParameterizedType) type).getActualTypeArguments()[0]).getRawType()))) {
                return new ObjectConstructor<T>() {
                    @Override
                    public T construct() {
                        return (T) new LinkedHashMap<Object, Object>();
                    }
                };
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return instanceCreators.toString();
    }

}
