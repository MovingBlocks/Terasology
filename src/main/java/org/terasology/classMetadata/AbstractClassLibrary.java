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

import com.google.common.collect.Maps;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Abstract base implement of ClassLibrary.
 *
 * @author Immortius
 */
public abstract class AbstractClassLibrary<T> implements ClassLibrary<T> {

    private CopyStrategyLibrary copyStrategyLibrary;
    private ReflectFactory reflectFactory;

    private Map<Class<? extends T>, ClassMetadata<? extends T>> serializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends T>> typeLookup = Maps.newHashMap();

    public AbstractClassLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        this.reflectFactory = factory;
        this.copyStrategyLibrary = copyStrategies;
    }

    /**
     * @param type A type being registered into the library
     * @return The name to use to identify the provided type
     */
    protected abstract String getNameFor(Class<? extends T> type);

    /**
     * @param type    A type being registered into the library
     * @param name    The name for the type
     * @param <CLASS> The class of the type
     * @return An instance of ClassMetadata (or a subtype) providing metadata for the given type
     */
    protected abstract <CLASS extends T> ClassMetadata<CLASS> createMetadata(Class<CLASS> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, String name);

    @Override
    public void register(Class<? extends T> clazz) {
        register(clazz, getNameFor(clazz));
    }

    @Override
    public void register(Class<? extends T> clazz, String name) {
        ClassMetadata<? extends T> metadata = createMetadata(clazz, reflectFactory, copyStrategyLibrary, name);

        serializationLookup.put(clazz, metadata);

        typeLookup.put(name.toLowerCase(Locale.ENGLISH), clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends T> ClassMetadata<U> getMetadata(Class<U> clazz) {
        if (clazz == null) {
            return null;
        }
        return (ClassMetadata<U>) serializationLookup.get(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends T> ClassMetadata<U> getMetadata(U object) {
        if (object != null) {
            return getMetadata((Class<U>) (object.getClass()));
        }
        return null;
    }

    @Override
    public <TYPE extends T> TYPE copy(TYPE object) {
        ClassMetadata<TYPE> info = getMetadata(object);
        if (info != null) {
            return info.copy(object);
        }
        return null;
    }

    @Override
    public ClassMetadata<? extends T> getMetadata(String className) {
        return getMetadata(typeLookup.get(className.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Iterator<ClassMetadata<? extends T>> iterator() {
        return serializationLookup.values().iterator();
    }
}
