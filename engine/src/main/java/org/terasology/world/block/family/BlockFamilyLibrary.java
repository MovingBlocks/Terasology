/*
 * Copyright 2018 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.module.ModuleEnvironment;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassLibrary;
import org.terasology.registry.InjectionHelper;
import org.terasology.util.reflection.ParameterProvider;
import org.terasology.util.reflection.SimpleClassFactory;
import org.terasology.world.block.BlockBuilderHelper;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;

import java.util.Optional;

/**
 * Simple Registry for Block Families.
 * <p>
 * The Registry maps a string id to an associated class implementation of a block Family.
 * </p>
 */
public class BlockFamilyLibrary {
    private static final Logger logger = LoggerFactory.getLogger(BlockFamilyLibrary.class);

    private ClassLibrary<BlockFamily> library;

    public BlockFamilyLibrary(ModuleEnvironment moduleEnvironment, Context context) {
        library = new DefaultClassLibrary<>(context);
        for (Class<?> entry : moduleEnvironment.getTypesAnnotatedWith(RegisterBlockFamily.class)) {

            if (!BlockFamily.class.isAssignableFrom(entry)) {
                logger.error("Cannot load {}, must be a subclass of BlockFamily", entry.getSimpleName());
                continue;
            }
            RegisterBlockFamily registerInfo = entry.getAnnotation(RegisterBlockFamily.class);
            String id = registerInfo.value();
            logger.debug("Registering blockFamily {}", id);
            library.register(new SimpleUri(moduleEnvironment.getModuleProviding(entry), registerInfo.value()), (Class<? extends BlockFamily>) entry);

        }
    }

    /**
     * returns the class representing the block family based off the registered id.
     *
     * @param uri
     * @return
     */
    public Class<? extends BlockFamily> getBlockFamily(String uri) {
        ClassMetadata<? extends BlockFamily, ?> resolved = library.resolve(uri);

        if (uri == null || uri.isEmpty() || resolved == null) {
            logger.error(" Failed to resolve Blockfamily {}", uri);
            return SymmetricFamily.class;
        }
        return resolved.getType();
    }

    /**
     * Create a family based on the type and instantiate from the the family definition of the block and builder
     *
     * @param blockFamily
     * @param blockFamilyDefinition
     * @param blockBuilderHelper
     * @return
     */
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
            if (result.getURI() == null) {
                throw new Exception("Family Is missng a BlockUri");
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to load blockFamily {}", blockFamily, e);
        }
        return null;
    }

    /**
     * Create a family based on the type and instantiate from the the family definition of the block and builder
     *
     * @param blockFamily
     * @param blockFamilyDefinition
     * @param blockBuilderHelper
     * @param shape
     * @return new BlockFamily
     */
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

            if (result.getURI() == null) {
                throw new Exception("Family Is missng a BlockUri");
            }
            return result;
        } catch (Exception e) {
            logger.error("Failed to load blockFamily {}", blockFamily, e);
        }
        return null;
    }


    public static String[] getSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null) {
            return new String[]{};
        }
        BlockSections sections = blockFamily.getAnnotation(BlockSections.class);
        if (sections == null) {
            return new String[]{};
        }
        return sections.value();
    }

    public static MultiSection[] getMultiSections(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null) {
            return new MultiSection[]{};
        }
        MultiSections sections = blockFamily.getAnnotation(MultiSections.class);
        if (sections == null) {
            return new MultiSection[]{};
        }
        return sections.value();
    }

    public static boolean isFreeformSupported(Class<? extends AbstractBlockFamily> blockFamily) {
        if (blockFamily == null) {
            return false;
        }
        FreeFormSupported freeFormSupported = blockFamily.getAnnotation(FreeFormSupported.class);
        if (freeFormSupported == null) {
            return false;
        }
        return freeFormSupported.value();
    }
}
