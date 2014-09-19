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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base implement of ClassLibrary.
 *
 * @author Immortius
 */
public abstract class AbstractClassLibrary<T> implements ClassLibrary<T> {

    private ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
    private CopyStrategyLibrary copyStrategyLibrary;
    private ReflectFactory reflectFactory;

    private Map<Class<? extends T>, ClassMetadata<? extends T, ?>> classLookup = Maps.newHashMap();
    private Table<Name, Name, ClassMetadata<? extends T, ?>> uriLookup = HashBasedTable.create();

    public AbstractClassLibrary(ReflectFactory factory, CopyStrategyLibrary copyStrategies) {
        this.reflectFactory = factory;
        this.copyStrategyLibrary = copyStrategies;
    }

    /**
     * @param type A type being registered into the library
     * @param name The name for the type
     * @param <C>  The class of the type
     * @return An instance of ClassMetadata (or a subtype) providing metadata for the given type
     */
    protected abstract <C extends T> ClassMetadata<C, ?> createMetadata(Class<C> type, ReflectFactory factory, CopyStrategyLibrary copyStrategies, SimpleUri name);

    @Override
    public void register(SimpleUri uri, Class<? extends T> clazz) {
        ClassMetadata<? extends T, ?> metadata = createMetadata(clazz, reflectFactory, copyStrategyLibrary, uri);

        if (metadata != null) {
            classLookup.put(clazz, metadata);
            uriLookup.put(uri.getObjectName(), uri.getModuleName(), metadata);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends T> ClassMetadata<U, ?> getMetadata(Class<U> clazz) {
        if (clazz == null) {
            return null;
        }
        return (ClassMetadata<U, ?>) classLookup.get(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U extends T> ClassMetadata<U, ?> getMetadata(U object) {
        if (object != null) {
            return getMetadata((Class<U>) (object.getClass()));
        }
        return null;
    }

    @Override
    public <TYPE extends T> TYPE copy(TYPE object) {
        ClassMetadata<TYPE, ?> info = getMetadata(object);
        if (info != null) {
            return info.copy(object);
        }
        return null;
    }

    @Override
    public ClassMetadata<? extends T, ?> getMetadata(SimpleUri uri) {
        return uriLookup.get(uri.getObjectName(), uri.getModuleName());
    }

    @Override
    public Iterator<ClassMetadata<? extends T, ?>> iterator() {
        return classLookup.values().iterator();
    }

    @Override
    public List<ClassMetadata<? extends T, ?>> getMetadata(String name) {
        return getMetadata(new Name(name));
    }

    @Override
    public List<ClassMetadata<? extends T, ?>> getMetadata(Name name) {
        return Lists.newArrayList(uriLookup.row(name).values());
    }

    @Override
    public ClassMetadata<? extends T, ?> resolve(String name, Name context) {
        Module moduleContext = moduleManager.getEnvironment().get(context);
        if (moduleContext != null) {
            return resolve(name, moduleContext);
        }
        return null;
    }

    @Override
    public ClassMetadata<? extends T, ?> resolve(String name) {
        SimpleUri uri = new SimpleUri(name);
        if (uri.isValid()) {
            return getMetadata(uri);
        }
        List<ClassMetadata<? extends T, ?>> possibilities = getMetadata(name);
        if (possibilities.size() == 1) {
            return possibilities.get(0);
        }
        return null;
    }

    @Override
    public ClassMetadata<? extends T, ?> resolve(String name, Module context) {
        SimpleUri uri = new SimpleUri(name);
        if (uri.isValid()) {
            return getMetadata(uri);
        }
        List<ClassMetadata<? extends T, ?>> possibilities = getMetadata(name);
        switch (possibilities.size()) {
            case 0:
                return null;
            case 1:
                return possibilities.get(0);
            default:
                if (context != null) {
                    Set<Name> dependencies = moduleManager.getEnvironment().getDependencyNamesOf(context.getId());
                    Iterator<ClassMetadata<? extends T, ?>> iterator = possibilities.iterator();
                    while (iterator.hasNext()) {
                        ClassMetadata<? extends T, ?> metadata = iterator.next();
                        if (context.getId().equals(metadata.getUri().getModuleName())) {
                            return metadata;
                        }
                        if (!dependencies.contains(metadata.getUri().getModuleName())) {
                            iterator.remove();
                        }
                    }
                    if (possibilities.size() == 1) {
                        return possibilities.get(0);
                    }
                }
                return null;
        }
    }
}
