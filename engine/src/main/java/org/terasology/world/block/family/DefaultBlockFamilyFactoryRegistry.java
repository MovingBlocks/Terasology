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
package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.registry.InjectionHelper;
import org.terasology.util.reflection.ClassFactory;
import org.terasology.util.reflection.ParameterProvider;
import org.terasology.util.reflection.SimpleClassFactory;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBlockFamilyFactoryRegistry.class);
    private Context context;


    private Map<String, Class<? extends AbstractBlockFamily>> registryMap = Maps.newHashMap();

    public DefaultBlockFamilyFactoryRegistry(Context context) {
        this.context = context;
    }

    public static BlockFamily createFamily(Class<? extends AbstractBlockFamily> blockFamily, BlockFamilyDefinition blockFamilyDefinition, BlockBuilderHelper blockBuilderHelper) {
        try {
            SimpleClassFactory simpleClassFactory = new SimpleClassFactory(new ParameterProvider() {
                @Override
                public <T> Optional<T> get(Class<T> type) {
                    if (type.isAssignableFrom(BlockBuilderHelper.class)) {
                        return Optional.ofNullable((T) blockBuilderHelper);
                    } else if (type.isAssignableFrom(BlockFamilyDefinition.class)) {
                        return Optional.ofNullable((T) blockFamilyDefinition);
                    }
                    return Optional.empty();
                }
            });
            BlockFamily result = simpleClassFactory.instantiateClass(blockFamily).get();
            InjectionHelper.inject(result);
            if(result.getURI() == null)
                throw new Exception("Family Is missng a BlockUri");

            return result;
        } catch (Exception e) {
            logger.error("Failed to load blockFamily {}", blockFamily, e);
            e.printStackTrace();
        }
        return null;
    }

    public static BlockFamily createFamily(Class<? extends AbstractBlockFamily> blockFamily, BlockFamilyDefinition blockFamilyDefinition, BlockShape shape, BlockBuilderHelper blockBuilderHelper) {
        try {
            SimpleClassFactory simpleClassFactory = new SimpleClassFactory(new ParameterProvider() {
                @Override
                public <T> Optional<T> get(Class<T> type) {
                    if (type.isAssignableFrom(BlockBuilderHelper.class)) {
                        return Optional.ofNullable((T) blockBuilderHelper);
                    } else if (type.isAssignableFrom(BlockFamilyDefinition.class)) {
                        return Optional.ofNullable((T) blockFamilyDefinition);
                    } else if (type.isAssignableFrom(BlockShape.class)) {
                        return Optional.ofNullable((T) shape);
                    }
                    return Optional.empty();
                }
            });
            BlockFamily result = simpleClassFactory.instantiateClass(blockFamily).get();
            InjectionHelper.inject(result);

            if(result.getURI() == null)
                throw new Exception("Family Is missng a BlockUri");

            return result;
        } catch (Exception e) {
            logger.error("Failed to load blockFamily {}", blockFamily, e);
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null)
            return new String[]{};
        BlockSections sections = blockFamily.getAnnotation(BlockSections.class);
        if (sections == null)
            return new String[]{};
        return sections.value();
    }

    public static MultiSection[] getMultiSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null)
            return new MultiSection[]{};
        MultiSections sections = blockFamily.getAnnotation(MultiSections.class);
        if (sections == null)
            return new MultiSection[]{};
        return sections.value();
    }

    public static boolean isFreeformSupported(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null)
            return false;
        FreeFormSupported freeFormSupported = blockFamily.getAnnotation(FreeFormSupported.class);
        return freeFormSupported != null;
    }

    public void setBlockFamily(String id, Class<? extends AbstractBlockFamily> blockFamily) {
        registryMap.put(id.toLowerCase(), blockFamily);
    }

    @Override
    public Class<? extends AbstractBlockFamily> getBlockFamily(String blockFamilyId) {
        if (blockFamilyId == null || blockFamilyId.isEmpty()) {
            return SymmetricFamily.class;
        }
        return registryMap.get(blockFamilyId.toLowerCase());
    }

    public void clear() {
        registryMap.clear();
    }
}
