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
package org.terasology.world.generation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.InjectionHelper;
import org.terasology.utilities.ReflectionUtil;
import org.terasology.world.generation.internal.WorldImpl;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class WorldBuilder {

    private static final Logger logger = LoggerFactory.getLogger(WorldBuilder.class);

    private long seed;
    private List<WorldDataProvider> providersList = Lists.newArrayList();
    private Map<Class<? extends WorldDataProvider>, WorldDataProvider> providersMap = Maps.newHashMap();
    private List<WorldRasterizer> rasterizers = Lists.newArrayList();

    public WorldBuilder(long seed) {
        this.seed = seed;
    }

    public WorldBuilder addProvider(WorldDataProvider provider) {
        provider.setSeed(seed);
        providersList.add(provider);
        for (Class implementedInterface : ReflectionUtil.getInheritanceTree(provider.getClass(), WorldDataProvider.class)) {
            if (WorldDataProvider.class != implementedInterface) {
                WorldDataProvider previous = providersMap.put(implementedInterface, provider);
                if (previous != null) {
                    injectChainedDependency(provider, previous, implementedInterface);
                }
            }
        }

        return this;
    }

    private void injectChainedDependency(final WorldDataProvider provider, final WorldDataProvider previous, final Class<? extends WorldDataProvider> implementedInterface) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                for (Field field : ReflectionUtils.getAllFields(provider.getClass(), ReflectionUtils.withAnnotation(Requires.class))) {
                    if (field.getType().equals(implementedInterface)) {
                        try {
                            field.setAccessible(true);
                            field.set(provider, previous);
                        } catch (IllegalAccessException e) {
                            logger.error("Failed to inject value {} into field {}", previous, field, e);
                        }
                    }
                }
                return null;
            }
        });
    }

    private void injectUnchainedDependencies(final WorldDataProvider provider, final Map<Class<? extends WorldDataProvider>, WorldDataProvider> sourceProviders) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                Set<Class<? extends WorldDataProvider>> implemented = Sets.newHashSet(ReflectionUtil.getInheritanceTree(provider.getClass(), WorldDataProvider.class));

                for (Field field : ReflectionUtils.getAllFields(provider.getClass(), ReflectionUtils.withAnnotation(Requires.class))) {
                    if (!implemented.contains(field.getType())) {
                        WorldDataProvider source = sourceProviders.get(field.getType());
                        if (source != null) {
                            try {
                                field.setAccessible(true);
                                field.set(provider, source);
                            } catch (IllegalAccessException e) {
                                logger.error("Failed to inject value {} into field {}", source, field, e);
                            }
                        } else {
                            logger.error("Missing required provider {} for {}", field.getType(), provider.getClass());
                        }
                    }
                }
                return null;
            }
        });
    }

    public WorldBuilder addRasterizer(WorldRasterizer rasterizer) {
        rasterizers.add(rasterizer);
        return this;
    }

    public World build() {
        for (WorldDataProvider provider : providersList) {
            injectUnchainedDependencies(provider, providersMap);
        }

        for (WorldRasterizer rasterizer : rasterizers) {
            InjectionHelper.inject(rasterizer, Requires.class, providersMap);
        }

        return new WorldImpl(seed, providersMap, rasterizers);
    }

}
