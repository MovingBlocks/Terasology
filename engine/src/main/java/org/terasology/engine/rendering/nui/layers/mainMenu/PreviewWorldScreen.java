// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureData;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.engine.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.module.dependencyresolution.DependencyResolver;
import org.terasology.gestalt.module.dependencyresolution.ResolutionResult;
import org.terasology.gestalt.module.exceptions.UnresolvedDependencyException;
import org.terasology.math.TeraMath;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.layouts.PropertyLayout;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.nui.properties.Property;
import org.terasology.nui.properties.PropertyOrdering;
import org.terasology.nui.properties.PropertyProvider;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIDropdown;
import org.terasology.nui.widgets.UIImage;
import org.terasology.nui.widgets.UISlider;
import org.terasology.nui.widgets.UIText;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.zones.Zone;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Shows a preview of the generated world and provides some
 * configuration options to tweak the generation process.
 */
public class PreviewWorldScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:previewWorldScreen");

    private static final Logger logger = LoggerFactory.getLogger(PreviewWorldScreen.class);

    @In
    private ModuleManager moduleManager;

    @In
    private ModuleAwareAssetTypeManager assetTypeManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private Config config;

    @In
    private Context context;

    private WorldGenerator worldGenerator;

    private UIImage previewImage;
    private UISlider zoomSlider;
    private UIDropdown<Zone> zoneSelector;
    private UIButton applyButton;

    private UIText seed;

    private PreviewGenerator previewGen;


    private Context subContext;
    private ModuleEnvironment environment;

    private Texture texture;

    private boolean triggerUpdate;
    private String targetZone = "Surface";

    public PreviewWorldScreen() {
    }

    public void setEnvironment() throws Exception {

        // TODO: pass world gen and module list directly rather than using the config
        SimpleUri worldGenUri = config.getWorldGeneration().getDefaultGenerator();

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(config.getDefaultModSelection().listModules());
        if (result.isSuccess()) {
            subContext = new ContextImpl(context);
            CoreRegistry.setContext(subContext);
            environment = moduleManager.loadEnvironment(result.getModules(), false);
            subContext.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment, subContext));
            EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
            environmentSwitchHandler.handleSwitchToPreviewEnvironment(subContext, environment);
            genTexture();

            worldGenerator = WorldGeneratorManager.createWorldGenerator(worldGenUri, subContext, environment);
            worldGenerator.setWorldSeed(seed.getText());

            List<Zone> previewZones = Lists.newArrayList(worldGenerator.getZones())
                    .stream()
                    .filter(z -> !z.getPreviewLayers().isEmpty())
                    .collect(Collectors.toList());
            if (previewZones.isEmpty()) {
                zoneSelector.setVisible(false);
                previewGen = new FacetLayerPreview(environment, worldGenerator);
            } else {
                zoneSelector.setVisible(true);
                zoneSelector.setOptions(previewZones);
                zoneSelector.setSelection(previewZones.get(0));
            }

            configureProperties();
        } else {
            throw new UnresolvedDependencyException("Unable to resolve dependencies for " + worldGenUri);
        }
    }

    private void genTexture() {
        int imgWidth = 384;
        int imgHeight = 384;
        ByteBuffer buffer = ByteBuffer.allocateDirect(imgWidth * imgHeight * Integer.BYTES);
        ByteBuffer[] data = new ByteBuffer[]{buffer};
        ResourceUrn uri = new ResourceUrn("engine:terrainPreview");
        TextureData texData = new TextureData(imgWidth, imgHeight, data, Texture.WrapMode.CLAMP, Texture.FilterMode.LINEAR);
        texture = Assets.generateAsset(uri, texData, Texture.class);

        previewImage = find("preview", UIImage.class);
        previewImage.setImage(texture);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (triggerUpdate) {
            updatePreview();
            triggerUpdate = false;
        }
    }

    private void configureProperties() {

        PropertyLayout propLayout = find("properties", PropertyLayout.class);
        propLayout.setOrdering(PropertyOrdering.byLabel());
        propLayout.clear();

        WorldConfigurator worldConfig = worldGenerator.getConfigurator();

        Map<String, Component> params = worldConfig.getProperties();

        for (String key : params.keySet()) {
            Class<? extends Component> clazz = params.get(key).getClass();
            Component comp = config.getModuleConfig(worldGenerator.getUri(), key, clazz);
            if (comp != null) {
                worldConfig.setProperty(key, comp);       // use the data from the config instead of defaults
            }
        }

        ComponentLibrary compLib = subContext.get(ComponentLibrary.class);

        for (String label : params.keySet()) {

            PropertyProvider provider = new PropertyProvider(context.get(ReflectFactory.class), context.get(OneOfProviderFactory.class)) {
                @Override
                protected <T> Binding<T> createTextBinding(Object target, FieldMetadata<Object, T> fieldMetadata) {
                    return new WorldConfigBinding<>(worldConfig, label, compLib, fieldMetadata);
                }

                @Override
                protected Binding<Float> createFloatBinding(Object target, FieldMetadata<Object, ?> fieldMetadata) {
                    return new WorldConfigNumberBinding(worldConfig, label, compLib, fieldMetadata);
                }
            };

            Component target = params.get(label);
            List<Property<?, ?>> properties = provider.createProperties(target);
            propLayout.addProperties(label, properties);
        }
    }

    private void resetEnvironment() {

        CoreRegistry.setContext(context);

        if (environment != null) {
            EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
            environmentSwitchHandler.handleSwitchBackFromPreviewEnvironment(subContext);
            environment.close();
            environment = null;
        }

        previewGen.close();

        WorldConfigurator worldConfig = worldGenerator.getConfigurator();

        Map<String, Component> params = worldConfig.getProperties();
        if (params != null) {
            config.setModuleConfigs(worldGenerator.getUri(), params);
        }
    }

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        zoomSlider = find("zoomSlider", UISlider.class);
        if (zoomSlider != null) {
            zoomSlider.setValue(2f);
        }

        seed = find("seed", UIText.class);

        zoneSelector = find("zoneSelector", UIDropdown.class);

        applyButton = find("apply", UIButton.class);
        if (applyButton != null) {
            applyButton.subscribe(widget -> updatePreview());
        }

        WidgetUtil.trySubscribe(this, "close", w -> {
            resetEnvironment();
            triggerBackAnimation();
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    public void bindSeed(Binding<String> binding) {
        if (seed == null) {
            // TODO: call initialize through NUIManager instead of onOpened()
            seed = find("seed", UIText.class);
        }
        seed.bindText(binding);
    }

    private void updatePreview() {

        final NUIManager manager = context.get(NUIManager.class);
        final WaitPopup<TextureData> popup = manager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Updating Preview", "Please wait ...");

        ProgressListener progressListener = progress ->
                popup.setMessage("Updating Preview", String.format("Please wait ... %d%%", (int) (progress * 100f)));

        Callable<TextureData> operation = () -> {
            if (seed != null) {
                worldGenerator.setWorldSeed(seed.getText());
            }
            int zoom = TeraMath.floorToInt(zoomSlider.getValue());
            TextureData data = texture.getData();

            if (zoneSelector.isVisible()) {
                previewGen = zoneSelector.getSelection().preview(worldGenerator);
            }
            previewGen.render(data, zoom, progressListener);

            return data;
        };

        popup.onSuccess(texture::reload);
        popup.startOperation(operation, true);
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

        private WorldConfigBinding<? extends Number> binding;

        @SuppressWarnings("unchecked")
        protected WorldConfigNumberBinding(WorldConfigurator config, String label, ComponentLibrary compLib, FieldMetadata<Object, ?> field) {
            Class<?> type = field.getType();
            if (type == Integer.TYPE || type == Integer.class) {
                this.binding = new WorldConfigBinding<>(config, label, compLib,
                        (FieldMetadata<Object, Integer>) field);
            } else if (type == Float.TYPE || type == Float.class) {
                this.binding = new WorldConfigBinding<>(config, label, compLib,
                        (FieldMetadata<Object, Float>) field);
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


