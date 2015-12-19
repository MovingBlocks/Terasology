/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.bootstrap.EnvironmentSwitchHandler;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.game.GameManifest;
import org.terasology.math.TeraMath;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.NameVersion;
import org.terasology.network.NetworkMode;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.rendering.nui.layouts.PropertyLayout;
import org.terasology.rendering.nui.properties.Property;
import org.terasology.rendering.nui.properties.PropertyOrdering;
import org.terasology.rendering.nui.properties.PropertyProvider;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UIText;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.DefaultWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

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
    private GameEngine gameEngine;

    @In
    private Context context;

    private WorldGenerator worldGenerator;

    private UIImage previewImage;
    private UISlider zoomSlider;
    private UIButton applyButton;

    private UIText seed;

    private PreviewGenerator previewGen;

    private Texture texture;

    private boolean triggerUpdate;

    private boolean loadingAsServer;

    private GameManifest gameManifest;

    private ModuleEnvironment environment;

    @Override
    public void onOpened() {
        super.onOpened();
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

        ComponentLibrary compLib = context.get(ComponentLibrary.class);

        for (String label : params.keySet()) {

            PropertyProvider provider = new PropertyProvider() {
                @Override
                protected <T> Binding<T> createTextBinding(Object target, FieldMetadata<Object, T> fieldMetadata) {
                    return new WorldConfigBinding<T>(worldConfig, label, compLib, fieldMetadata);
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

    @Override
    public void onClosed() {

        previewGen.close();

        WorldConfigurator worldConfig = worldGenerator.getConfigurator();

        Map<String, Component> params = worldConfig.getProperties();
        if (params != null) {
            config.setModuleConfigs(worldGenerator.getUri(), params);
        }

        super.onClosed();
    }

    @Override
    public void initialise() {
        zoomSlider = find("zoomSlider", UISlider.class);
        if (zoomSlider != null) {
            zoomSlider.setValue(2f);
        }

        seed = find("seed", UIText.class);

        applyButton = find("apply", UIButton.class);
        if (applyButton != null) {
            applyButton.subscribe(new ActivateEventListener() {
                @Override
                public void onActivated(UIWidget widget) {
                    updatePreview();
                }
            });
        }

        WidgetUtil.trySubscribe(this, "play", button -> startGame());
        WidgetUtil.trySubscribe(this, "close", button -> backToMenu());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private void backToMenu() {
        if (environment != null) {
            WaitPopup<Void> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
            popup.setMessage("Unloading sandbox environment", "Please wait ...");
            popup.onSuccess(nil -> {
                getManager().popScreen();
            });

            popup.startOperation(() -> {
                EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
                environmentSwitchHandler.handleSwitchToEmptyEnivronment(context);
                environment.close();
                environment = null;
                return null;
            }, false);
        }
    }

    public void setGameManifest(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
        seed.bindText(BindHelper.bindBeanProperty("seed", gameManifest, String.class));

        WaitPopup<Void> popup = getManager().pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Loading sandbox environment", "Please wait ...");

        popup.onSuccess(nil -> {
            configureProperties();
            genTexture();
        });
        popup.startOperation(() -> { loadEnvironment(); return null; }, false);
    }

    private void loadEnvironment() {
        SimpleUri worldGenUri = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD).getWorldGenerator();

        try {
            Set<Module> moduleIds = new HashSet<>(gameManifest.getModules().size());
            for (NameVersion moduleInfo : gameManifest.getModules()) {
                moduleIds.add(moduleManager.getRegistry().getModule(moduleInfo.getName(), moduleInfo.getVersion()));
            }

            environment = moduleManager.loadEnvironment(moduleIds, true);
            for (Module moduleInfo : environment.getModulesOrderedByDependencies()) {
                logger.info("Activating module: {}:{}", moduleInfo.getId(), moduleInfo.getVersion());
            }

            context.put(WorldGeneratorPluginLibrary.class, new DefaultWorldGeneratorPluginLibrary(environment, context));

            EnvironmentSwitchHandler environmentSwitchHandler = context.get(EnvironmentSwitchHandler.class);
            environmentSwitchHandler.handleSwitchToGameEnvironment(context);

            worldGenerator = WorldGeneratorManager.createWorldGenerator(worldGenUri, context, environment);
            worldGenerator.setWorldSeed(gameManifest.getSeed());
            previewGen = new FacetLayerPreview(environment, worldGenerator);
        } catch (Exception e) {
            // if errors happen, don't enable this feature
            worldGenerator = null;
            logger.error("Unable to load world generator: " + worldGenUri + " for a 2d preview", e);
        }
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }

    private void startGame() {
        gameEngine.changeState(new StateLoading(gameManifest, (loadingAsServer)
                ? NetworkMode.DEDICATED_SERVER : NetworkMode.NONE));
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
                this.binding = new WorldConfigBinding<Integer>(config, label, compLib,
                        (FieldMetadata<Object, Integer>) field);
            } else if (type == Float.TYPE || type == Float.class) {
                this.binding = new WorldConfigBinding<Float>(config, label, compLib,
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
            return Float.valueOf(val.floatValue());
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Float value) {
            Class<? extends Number> type = binding.fieldMetadata.getType();
            if (type == Integer.TYPE || type == Integer.class) {
                ((Binding<Integer>) binding).set(Integer.valueOf(value.intValue()));
            } else if (type == Float.TYPE || type == Float.class) {
                ((Binding<Float>) binding).set(value);
            }
        }
    }
}
