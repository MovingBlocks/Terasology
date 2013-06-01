/*
 * Copyright 2013 Moving Blocks
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

    public abstract String[] getNamesFor(Class<? extends T> clazz);

    @Override
    public void register(Class<? extends T> clazz) {
        register(clazz, getNamesFor(clazz));
    }

    public void register(Class<? extends T> clazz, String... names) {
        U metadata = createMetadata(clazz, names);

        serializationLookup.put(clazz, metadata);

        for (String name : names) {
            typeLookup.put(name.toLowerCase(Locale.ENGLISH), clazz);
        }
    }

    protected abstract <CLAZZ extends T> U createMetadata(Class<CLAZZ> clazz, String... names);

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
            return (ClassMetadata<U>) serializationLookup.get(object.getClass());
        }
        return null;
    }

    @Override
    public <TYPE extends T> TYPE copy(TYPE object) {
        ClassMetadata<TYPE> info = (ClassMetadata<TYPE>) getMetadata(object);
        if (info != null) {
            return info.clone(object);
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
