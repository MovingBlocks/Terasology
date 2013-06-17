package org.terasology.world.chunks.perBlockStorage;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class PerBlockStorageManager { 

    private static final Logger logger = LoggerFactory.getLogger(PerBlockStorageManager.class);
    
    private ModManager mods;
    private Reflections engineReflections;
    
    @SuppressWarnings("unchecked")
    private List<Class<? extends TeraArray.Factory<? extends TeraArray>>> getArrayFactoryClasses(Class<? extends TeraArray> array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final Class<?>[] classes = array.getClasses();
        final List<Class<? extends TeraArray.Factory<? extends TeraArray>>> result = Lists.newArrayList();
        for (Class<?> factory : classes) {
            if (TeraArray.Factory.class.isAssignableFrom(factory) && !Modifier.isAbstract(factory.getModifiers())) {
                result.add((Class<? extends TeraArray.Factory<? extends TeraArray>>) factory);
            }
        }
        return result;
    }
    
    private void registerTeraArray(String pakkage, Class<? extends TeraArray> array) {
        Preconditions.checkNotNull(pakkage, "The parameter 'pakkage' must not be null");
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        if (!Modifier.isAbstract(array.getModifiers())) {
            final List<Class<? extends TeraArray.Factory<? extends TeraArray>>> factoryClasses = getArrayFactoryClasses(array);
            for (final Class<? extends TeraArray.Factory<? extends TeraArray>> factoryClass : factoryClasses)
                if (factoryClass != null) {
                    try {
                        final TeraArray.Factory<? extends TeraArray> factory = factoryClass.newInstance();
                        final String arrayId = pakkage + ":" + factory.getId();
                        logger.info("Registered per block storage type '{}' of class '{}'", arrayId, array.getSimpleName());
                    } catch (Exception e) {
                        logger.error("Failed registering per block storage type '{}'", array.getSimpleName(), e);
                    }
                } else
                    logger.warn("Discovered invalid per block storage type '{}', no factory discovered, skipping", array.getSimpleName());
        }
    }

    private void scanForTeraArrays() {
        // scanning engine for tera arrays
        Set<Class<? extends TeraArray>> teraArrays = engineReflections.getSubTypesOf(TeraArray.class);
        for (final Class<? extends TeraArray> array : teraArrays) 
            registerTeraArray("engine", array);

        // scanning mods for tera arrays
        if (mods != null)
            for (final Mod mod : mods.getMods()) {
                final Reflections reflections = mod.getReflections();
                if (reflections != null) {
                    teraArrays = reflections.getSubTypesOf(TeraArray.class);
                    for (final Class<? extends TeraArray> array : teraArrays) 
                        registerTeraArray(mod.getModInfo().getId(), array);
                }
            }
    }

    public PerBlockStorageManager() {
        refresh();
    }

    public ModManager getModManager() {
        if (mods == null) {
            mods = CoreRegistry.get(ModManager.class);
        }
        return mods;
    }
    
    public Reflections getEngineReflections() {
        if (engineReflections == null) {
            if (getModManager() != null)
                engineReflections = mods.getEngineReflections();
            else {
                logger.warn("No mod manager available. Setting up my own reflections.");
                engineReflections = new Reflections(
                        new ConfigurationBuilder()
                                .addClassLoader(getClass().getClassLoader())
                                .addUrls(ClasspathHelper.forPackage("org.terasology", getClass().getClassLoader()))
                                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
            }
        }
        return engineReflections;
    }
    
    public void refresh() {
        if (getEngineReflections() == null) {
            logger.error("Unable to scan for available per block storage types.");
            return;
        }
        scanForTeraArrays();
    }
}
