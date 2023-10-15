// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.layouts.PropertyLayout;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.nui.properties.Property;
import org.terasology.nui.properties.PropertyOrdering;
import org.terasology.nui.properties.PropertyProvider;
import org.terasology.nui.widgets.UILabel;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Allows configuration of a single world.
 */
public class WorldSetupScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:worldSetupScreen");

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private Config config;

    @In
    private TranslationSystem translationSystem;

    private WorldGenerator worldGenerator;
    private UniverseWrapper universe;
    private ModuleEnvironment environment;
    private Context context;
    private WorldConfigurator oldWorldConfig;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "close", button -> {
            triggerBackAnimation();
        });
    }
    @Override
    public void onOpened() {
        super.onOpened();

        UILabel subtitle = find("subtitle", UILabel.class);
        subtitle.setText(translationSystem.translate("${engine:menu#world-setup}") + " for " + universe.getGameName().toString());
    }

    /**
     * This method sets the world whose properties are to be changed. This function is called before the screen comes
     * to the forefront.
     *
     * @param subContext    the new environment created in {@link UniverseSetupScreen}
     * @param universe      the universe whose world's configurations are to be changed.
     * @throws UnresolvedWorldGeneratorException
     */
    public void setWorld(Context subContext, UniverseWrapper universe) {
        this.universe = universe;
        context = subContext;
        SimpleUri worldGenUri = universe.getWorldGenerator().getUri();
        environment = context.get(ModuleEnvironment.class);
        context.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment, context));
        worldGenerator = universe.getWorldGenerator();
        configureProperties();
    }

    /**
     * Assigns a {@link WorldConfigurator} for every world if it doesn't exist. Using
     * the WorldConfigurator it gets the properties associated with that world.
     */
    private void configureProperties() {

        PropertyLayout propLayout = find("properties", PropertyLayout.class);
        propLayout.setOrdering(PropertyOrdering.byLabel());
        propLayout.clear();
        WorldConfigurator worldConfig;
        if (universe.getWorldConfigurator() != null) {
            worldConfig = universe.getWorldConfigurator();
        } else {
            worldConfig = worldGenerator.getConfigurator();
            universe.setWorldConfigurator(worldConfig);
        }
        oldWorldConfig = worldConfig;

        Map<String, Component> params = worldConfig.getProperties();

        for (String key : params.keySet()) {
            Class<? extends Component> clazz = params.get(key).getClass();
            Component comp = config.getModuleConfig(worldGenerator.getUri(), key, clazz);
            if (comp != null) {
                // use the data from the config instead of defaults
                worldConfig.setProperty(key, comp);
            }
        }

        ComponentLibrary compLib = context.get(ComponentLibrary.class);

        for (String label : params.keySet()) {

            PropertyProvider provider = new PropertyProvider(context.get(ReflectFactory.class), context.get(OneOfProviderFactory.class)) {
                @Override
                protected <T> Binding<T> createTextBinding(Object target, FieldMetadata<Object, T> fieldMetadata) {
                    return new WorldSetupScreen.WorldConfigBinding<>(worldConfig, label, compLib, fieldMetadata);
                }

                @Override
                protected Binding<Float> createFloatBinding(Object target, FieldMetadata<Object, ?> fieldMetadata) {
                    return new WorldSetupScreen.WorldConfigNumberBinding(worldConfig, label, compLib, fieldMetadata);
                }
            };

            Component target = params.get(label);
            List<Property<?, ?>> properties = provider.createProperties(target);
            propLayout.addProperties(label, properties);
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    /**
     * Updates a world configurator through setProperty() whenever Binding#set() is called.
     */
    private static class WorldConfigBinding<T> implements Binding<T> {
        private final String label;
        private final WorldConfigurator worldConfig;
        private final FieldMetadata<Object, T> fieldMetadata;
        private final ComponentLibrary compLib;

        protected WorldConfigBinding(WorldConfigurator config, String label, ComponentLibrary compLib, FieldMetadata<Object, T> fieldMetadata) {
            this.worldConfig = config;
            this.label = label;
            this.compLib = compLib;
            this.fieldMetadata = fieldMetadata;
        }

        @Override
        public T get() {
            Component comp = worldConfig.getProperties().get(label);
            return fieldMetadata.getValue(comp);
        }

        @Override
        public void set(T value) {
            T old = get();

            if (!Objects.equals(old, value)) {
                cloneAndSet(label, value);
            }
        }

        private void cloneAndSet(String group, Object value) {
            Component comp = worldConfig.getProperties().get(group);
            Component clone = compLib.copy(comp);
            fieldMetadata.setValue(clone, value);

            // notify the world generator about the new component
            worldConfig.setProperty(label, clone);
        }
    }

    private static class WorldConfigNumberBinding implements Binding<Float> {

        private WorldSetupScreen.WorldConfigBinding<? extends Number> binding;

        @SuppressWarnings("unchecked")
        protected WorldConfigNumberBinding(WorldConfigurator config, String label, ComponentLibrary compLib, FieldMetadata<Object, ?> field) {
            Class<?> type = field.getType();
            if (type == Integer.TYPE || type == Integer.class) {
                this.binding = new WorldSetupScreen.WorldConfigBinding<>(config, label, compLib, (FieldMetadata<Object, Integer>) field);
            } else if (type == Float.TYPE || type == Float.class) {
                this.binding = new WorldSetupScreen.WorldConfigBinding<>(config, label, compLib, (FieldMetadata<Object, Float>) field);
            }
        }

        @Override
        public Float get() {
            Number val = binding.get();
            if (val instanceof Float) {
                // use boxed instance directly
                return (Float) val;
            }
            // create a boxed instance otherwise
            return val.floatValue();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Float value) {
            Class<? extends Number> type = binding.fieldMetadata.getType();
            if (type == Integer.TYPE || type == Integer.class) {
                ((Binding<Integer>) binding).set(value.intValue());
            } else if (type == Float.TYPE || type == Float.class) {
                ((Binding<Float>) binding).set(value);
            }
        }
    }
}
