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

package org.terasology.entitySystem.metadata.internal;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.metadata.ClassLibrary;
import org.terasology.entitySystem.metadata.ClassMetadata;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius
 */
public abstract class BaseLibraryImpl<T, U extends ClassMetadata<? extends T>> implements ClassLibrary<T, U> {

    private Map<Class<? extends T>, U> serializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends T>> typeLookup = Maps.newHashMap();

    public abstract String getNameFor(Class<? extends T> clazz);

    protected abstract <CLASS extends T> U createMetadata(Class<CLASS> clazz, String name);

    @Override
    public void register(Class<? extends T> clazz) {
        register(clazz, getNameFor(clazz));
    }

    @Override
    public void register(Class<? extends T> clazz, String name) {
        U metadata = createMetadata(clazz, name);

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
    public Iterator<U> iterator() {
        return serializationLookup.values().iterator();
    }
}
