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
package org.terasology.classMetadata;

import org.terasology.classMetadata.copying.CopyStrategy;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;

import java.lang.reflect.Field;

/**
 * A standard class metadata implementation using FieldMetadata.
 * @author Immortius
 */
public class DefaultClassMetadata<T> extends ClassMetadata<T, FieldMetadata<T, ?>> {

    /**
     * Creates a class metatdata
     *
     * @param type                The type to create the metadata for
     * @param factory             A reflection library to provide class construction and field get/set functionality
     * @param copyStrategyLibrary A copy strategy library
     * @param name                The name to identify the class with.
     * @throws NoSuchMethodException
     *          If the class has no default constructor
     */
    public DefaultClassMetadata(Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategyLibrary, String name) throws NoSuchMethodException {
        super(type, factory, copyStrategyLibrary, name);
    }

    @Override
    protected <V> FieldMetadata<T, V> createField(Field field, CopyStrategy<V> copyStrategy, ReflectFactory factory) {
        return new FieldMetadata<>(this, field, copyStrategy, factory);
    }
}
