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
package org.terasology.reflection.metadata;

import com.google.common.base.Predicates;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.InaccessibleFieldException;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.SimpleUri;

import java.lang.reflect.Field;

/**
 * A standard class metadata implementation using FieldMetadata.
 *
 */
public class DefaultClassMetadata<T> extends ClassMetadata<T, FieldMetadata<T, ?>> {

    /**
     * Creates a class metatdata
     *
     * @param uri                 The uri that identifies this class
     * @param type                The type to create the metadata for
     * @param factory             A reflection library to provide class construction and field get/set functionality
     * @param copyStrategyLibrary A copy strategy library
     * @throws NoSuchMethodException If the class has no default constructor
     */
    public DefaultClassMetadata(SimpleUri uri, Class<T> type, ReflectFactory factory, CopyStrategyLibrary copyStrategyLibrary) throws NoSuchMethodException {
        super(uri, type, factory, copyStrategyLibrary, Predicates.<Field>alwaysTrue());
    }

    @Override
    protected <V> FieldMetadata<T, V> createField(Field field, CopyStrategy<V> copyStrategy, ReflectFactory factory) throws InaccessibleFieldException {
        return new FieldMetadata<>(this, field, copyStrategy, factory);
    }
}
