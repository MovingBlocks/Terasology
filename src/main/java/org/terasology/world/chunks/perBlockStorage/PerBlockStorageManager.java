package org.terasology.world.chunks.perBlockStorage;

import java.lang.reflect.Modifier;
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

public class PerBlockStorageManager { 

    private static final Logger logger = LoggerFactory.getLogger(PerBlockStorageManager.class);
    
    private ModManager mods;
    private Reflections engineReflections;
    
    @SuppressWarnings("unchecked")
    private Class<? extends TeraArray.Factory<? extends TeraArray>> getArrayFactoryClass(Class<? extends TeraArray> array) {
        final Class<?>[] classes = array.getClasses();
        for (Class<?> factory : classes) {
            if (TeraArray.Factory.class.isAssignableFrom(factory) && !Modifier.isAbstract(factory.getModifiers())) {
                return (Class<? extends TeraArray.Factory<? extends TeraArray>>) factory;
            }
        }
        return null;
    }
    
    private void registerTeraArray(Class<? extends TeraArray> array) {
        if (!Modifier.isAbstract(array.getModifiers())) {
            final Class<? extends TeraArray.Factory<? extends TeraArray>> factoryClass = getArrayFactoryClass(array);
            if (factoryClass != null) {
                try {
                    final TeraArray.Factory<? extends TeraArray> factory = factoryClass.newInstance();
                    logger.info("Registered per block storage type '{}'", array.getSimpleName());
                } catch (Exception e) {
                    logger.error("Failed registering per block storage type '{}'", array.getSimpleName(), e);
                }
            } else
                logger.warn("Discovered invalid per block storage type '{}', no factory discovered, skipping", array.getSimpleName());
        }
    }

    private void scanForTeraArrays() {
        final Set<Class<? extends TeraArray>> teraArrays = engineReflections.getSubTypesOf(TeraArray.class);
        if (mods != null)
            for (final Mod mod : mods.getMods()) {
                final Reflections reflections = mod.getReflections();
                if (reflections != null)
                    teraArrays.addAll(reflections.getSubTypesOf(TeraArray.class));
            }
        for (final Class<? extends TeraArray> array : teraArrays) 
            registerTeraArray(array);
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
        if (getEngineReflections() == null) 
            throw new IllegalStateException("Unable to scan for available per block storage types.");
        scanForTeraArrays();
    }
}
