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
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.rendering.nui.layouts.PropertyLayout;
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
import org.terasology.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

/**
 * Shows a preview of the generated world and provides some
 * configuration options to tweak the generation process.
 */
public class PreviewWorldScreen extends CoreScreenLayer {

    public static final AssetUri ASSET_URI = new AssetUri(AssetType.UI_ELEMENT, "engine:previewWorldScreen");

    private static final Logger logger = LoggerFactory.getLogger(PreviewWorldScreen.class);

    @In
    private ModuleManager moduleManager;

    @In
    private AssetManager assetManager;

    @In
    private WorldGeneratorManager worldGeneratorManager;

    @In
    private Config config;

    private WorldGenerator worldGenerator;

    private UIImage previewImage;
    private UISlider zoomSlider;
    private UIButton applyButton;

    private UIText seed;

    private PreviewGenerator previewGen;

    private ModuleEnvironment environment;

    private final Texture texture;

    private boolean triggerUpdate;

    public PreviewWorldScreen() {
        int imgWidth = 384;
        int imgHeight = 384;
        ByteBuffer buffer = ByteBuffer.allocateDirect(imgWidth * imgHeight * Integer.BYTES);
        ByteBuffer[] data = new ByteBuffer[]{buffer};
        AssetUri uri = new AssetUri(AssetType.TEXTURE, "engine:terrainPreview");
        TextureData texData = new TextureData(imgWidth, imgHeight, data, Texture.WrapMode.CLAMP, Texture.FilterMode.LINEAR);
        texture = Assets.generateAsset(uri, texData, Texture.class);
    }

    @Override
    public void onOpened() {
        super.onOpened();

        SimpleUri worldGenUri = config.getWorldGeneration().getDefaultGenerator();
        Name moduleName = worldGenUri.getModuleName();

        try {
            DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
            ResolutionResult result = resolver.resolve(moduleName);
            if (result.isSuccess()) {
                environment = moduleManager.loadEnvironment(result.getModules(), false);
                CoreRegistry.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment));
                assetManager.setEnvironment(environment);
                worldGenerator = worldGeneratorManager.searchForWorldGenerator(worldGenUri, environment);
                worldGenerator.setWorldSeed(seed.getText());
                previewGen = new FacetLayerPreview(environment, worldGenerator);
                configureProperties();
                triggerUpdate = true;
            } else {
                logger.error("Could not resolve modules for: {}", worldGenUri);
            }

        } catch (Exception e) {
            // if errors happen, don't enable this feature
            worldGenerator = null;
            logger.error("Unable to load world generator: " + worldGenUri + " for a 2d preview", e);
        }
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

        PropertyLayout properties = find("properties", PropertyLayout.class);
        properties.setOrdering(PropertyOrdering.byLabel());
        properties.clear();

        WorldConfigurator worldConfig = worldGenerator.getConfigurator();

        Map<String, Component> params = worldConfig.getProperties();

        for (String key : params.keySet()) {
            Class<? extends Component> clazz = params.get(key).getClass();
            Component comp = config.getModuleConfig(worldGenerator.getUri(), key, clazz);
            if (comp != null) {
                worldConfig.setProperty(key, comp);       // use the data from the config instead of defaults
            }
        }

        for (String label : params.keySet()) {
            PropertyProvider<?> provider = new PropertyProvider<>(params.get(label));
            properties.addPropertyProvider(label, provider);
        }
    }

    @Override
    public void onClosed() {

        CoreRegistry.remove(WorldGeneratorPluginLibrary.class);

        if (environment != null) {
            assetManager.setEnvironment(moduleManager.getEnvironment());
            environment.close();
            environment = null;
        }

        if (previewGen != null) {
            previewGen.close();
            previewGen = null;
        }

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

        previewImage = find("preview", UIImage.class);
        previewImage.setImage(texture);

        WidgetUtil.trySubscribe(this, "close", new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    public void bindSeed(Binding<String> binding) {
        if (seed != null) {
            seed.bindText(binding);
        }
    }

    private void updatePreview() {
        final NUIManager manager = CoreRegistry.get(NUIManager.class);
        final WaitPopup<TextureData> popup = manager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Updating Preview", "Please wait ...");

        ProgressListener progressListener = progress ->
                popup.setMessage("Updating Preview", String.format("Please wait ... %d%%", (int) (progress * 100f)));

        Callable<TextureData> operation = () -> {
            if (seed != null) {
                worldGenerator.setWorldSeed(seed.getText());
            }
            int zoom = TeraMath.floorToInt(zoomSlider.getValue());
            previewGen.render(texture.getData(), zoom, progressListener);
            return texture.getData();
        };

        popup.onSuccess(newData -> texture.reload(newData));
        popup.startOperation(operation, true);
    }
}


