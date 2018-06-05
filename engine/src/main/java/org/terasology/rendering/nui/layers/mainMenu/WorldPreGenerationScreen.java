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
package org.terasology.rendering.nui.layers.mainMenu;

import com.google.common.collect.Lists;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.math.TeraMath;
import org.terasology.module.ModuleEnvironment;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureData;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.mainMenu.preview.FacetLayerPreview;
import org.terasology.rendering.nui.layers.mainMenu.preview.PreviewGenerator;
import org.terasology.rendering.nui.widgets.UIDropdownScrollable;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.world.World;
import org.terasology.utilities.Assets;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.TempWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.zones.Zone;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class WorldPreGenerationScreen extends CoreScreenLayer {

    @In
    private ModuleManager moduleManager;

    @In
    private Config config;


    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:worldPreGenerationScreen");
    private ModuleEnvironment environment;
    private WorldGenerator worldGenerator;
    private Texture texture;
    private UIImage previewImage;
    private Context context;
    private PreviewGenerator previewGen;
    private List<World> worldList;
    private String selectedWorld;
    private List<String> worldNames;
    private UISlider zoomSlider;


    public WorldPreGenerationScreen() {
    }

    public void setEnvironment(Context subContext) throws UnresolvedWorldGeneratorException {

        context = subContext;
        environment = context.get(ModuleEnvironment.class);
        context.put(WorldGeneratorPluginLibrary.class, new TempWorldGeneratorPluginLibrary(environment, context));
        //worldGenerator = WorldGeneratorManager.createWorldGenerator(worldGenUri, context, environment);
        worldList = context.get(UniverseSetupScreen.class).getWorldsList();
        selectedWorld = context.get(UniverseSetupScreen.class).getSelectedWorld();
        worldNames = context.get(UniverseSetupScreen.class).worldNames();
        if (findWorldByName().getWorldGenerator() == null) {
            worldGenerator = WorldGeneratorManager.createWorldGenerator(findWorldByName().getWorldGeneratorInfo().getUri(), context, environment);
            findWorldByName().setWorldGenerator(worldGenerator);
        } else {
            worldGenerator = findWorldByName().getWorldGenerator();
        }
        //worldGenerator = WorldGeneratorManager.createWorldGenerator(findWorldByName().getWorldGeneratorInfo().getUri(), context, environment);

        worldGenerator.setWorldSeed("thisisjustrandom");
        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        worldsDropdown.setOptions(worldNames);
        genTexture();

        List<Zone> previewZones = Lists.newArrayList(worldGenerator.getZones())
                .stream()
                .filter(z -> !z.getPreviewLayers().isEmpty())
                .collect(Collectors.toList());
        if (previewZones.isEmpty()) {
            previewGen = new FacetLayerPreview(environment, worldGenerator);
        }
    }

    @Override
    public void initialise() {

        zoomSlider = find("zoomSlider", UISlider.class);
        if (zoomSlider != null) {
            zoomSlider.setValue(2f);
        }

        final UIDropdownScrollable worldsDropdown = find("worlds", UIDropdownScrollable.class);
        worldsDropdown.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return selectedWorld;
            }

            @Override
            public void set(String value) {
                selectedWorld = value;
                try {
                    if (findWorldByName().getWorldGenerator() == null) {
                        worldGenerator = WorldGeneratorManager.createWorldGenerator(findWorldByName().getWorldGeneratorInfo().getUri(), context, environment);
                        findWorldByName().setWorldGenerator(worldGenerator);
                    } else {
                        worldGenerator = findWorldByName().getWorldGenerator();
                    }
                    worldGenerator.setWorldSeed("thisisjustrandom");
                    previewGen = new FacetLayerPreview(environment, worldGenerator);
                    updatePreview();
                } catch (UnresolvedWorldGeneratorException e) {
                    e.getMessage();
                }
            }
        });

        StartPlayingScreen startPlayingScreen = getManager().createScreen(StartPlayingScreen.ASSET_URI, StartPlayingScreen.class);
        WidgetUtil.trySubscribe(this, "continue", button ->
                updatePreview()
        );

        WorldSetupScreen worldSetupScreen = getManager().createScreen(WorldSetupScreen.ASSET_URI, WorldSetupScreen.class);

        WidgetUtil.trySubscribe(this, "config", button -> {
            try {
                if (!selectedWorld.isEmpty()) {
                    worldSetupScreen.setWorld(context, findWorldByName());
                    triggerForwardAnimation(worldSetupScreen);
                } else {
                    getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Worlds List Empty!", "No world found to configure.");
                }
            } catch (UnresolvedWorldGeneratorException e) {
                e.getMessage();
            }
        });

        WidgetUtil.trySubscribe(this, "close", button ->
                triggerBackAnimation()
        );
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

    private void updatePreview() {

        final NUIManager manager = context.get(NUIManager.class);
        final WaitPopup<TextureData> popup = manager.pushScreen(WaitPopup.ASSET_URI, WaitPopup.class);
        popup.setMessage("Updating Preview", "Please wait ...");

        ProgressListener progressListener = progress ->
                popup.setMessage("Updating Preview", String.format("Please wait ... %d%%", (int) (progress * 100f)));

        Callable<TextureData> operation = () -> {
            int zoom = TeraMath.floorToInt(zoomSlider.getValue());
            TextureData data = texture.getData();

            previewGen.render(data, zoom, progressListener);

            return data;
        };

        popup.onSuccess(texture::reload);
        popup.startOperation(operation, true);
    }

    public World findWorldByName() {
        for (World world: worldList) {
            if (world.getWorldName().toString().equals(selectedWorld)) {
                return world;
            }
        }
        return null;
    }

}
