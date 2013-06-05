package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class DefaultBlockFamilyFactoryRegistry implements BlockFamilyFactoryRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DefaultBlockFamilyFactoryRegistry.class);

    private Map<String, BlockFamilyFactory> blockFamilyFactoryRegistry = Maps.newHashMap();

    @Override
    public BlockFamilyFactory getBlockFamilyFactory(String id) {
        return blockFamilyFactoryRegistry.get(id.toLowerCase());
    }

    public void loadBlockFamilyFactories(String packageName, Reflections reflections) {
        Set<Class<?>> blockFamilyFactories = reflections.getTypesAnnotatedWith(RegisterBlockFamilyFactory.class);
        for (Class<?> blockFamilyFactory : blockFamilyFactories) {
            if (!BlockFamilyFactory.class.isAssignableFrom(blockFamilyFactory)) {
                logger.error("Cannot load {}, must be a subclass of BlockFamilyFactory", blockFamilyFactory.getSimpleName());
                continue;
            }

            RegisterBlockFamilyFactory registerInfo = blockFamilyFactory.getAnnotation(RegisterBlockFamilyFactory.class);
            String id = registerInfo.id();
            try {
                BlockFamilyFactory newBlockFamilyFactory = (BlockFamilyFactory) blockFamilyFactory.newInstance();
                blockFamilyFactoryRegistry.put(id.toLowerCase(), newBlockFamilyFactory);
                logger.debug("Loaded block family factory {}", id);
            } catch (InstantiationException e) {
                logger.error("Failed to load block family factory {}", id, e);
            } catch (IllegalAccessException e) {
                logger.error("Failed to load block family factory {}", id, e);
            }
        }

    }
}
