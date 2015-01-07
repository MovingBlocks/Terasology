/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.world.block.loader;

import com.google.common.base.Charsets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.persistence.ModuleContext;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.gson.JsonMergeUtil;
import org.terasology.world.block.BlockSounds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Block sounds loader will load all block sounds definitions and build them into immutable objects.
 */
public class BlockSoundsLoader {

    private static final Logger logger = LoggerFactory.getLogger(BlockSoundsLoader.class);

    private final BlockSoundsFactory soundsFactory;
    private JsonParser parser;
    private Gson gson;

    public BlockSoundsLoader(BlockSoundsFactory soundsFactory) {
        this.soundsFactory = soundsFactory;
        parser = new JsonParser();
        gson = new GsonBuilder().create();
    }

    public List<BlockSounds> loadBlockSoundsDefinitions() {
        logger.info("Loading Block Sounds...");

        List<BlockSounds> sounds = new ArrayList<>();
        for (AssetUri blockDefUri : Assets.list(AssetType.BLOCK_SOUNDS_DEFINITION)) {
            try (ModuleContext.ContextSpan ignored = ModuleContext.setContext(blockDefUri.getModuleName())) {
                JsonElement rawJson = readJson(blockDefUri);
                if (rawJson != null) {
                    JsonObject blockDefJson = rawJson.getAsJsonObject();

                    // Don't process templates
                    if (blockDefJson.has("template") && blockDefJson.get("template").getAsBoolean()) {
                        continue;
                    }
                    logger.debug("Loading {}", blockDefUri);

                    BlockSoundsDefinition definition = createBlockSoundsDefinition(inheritData(blockDefUri, blockDefJson));
                    sounds.add(soundsFactory.create(blockDefUri, definition));
                }
            } catch (Exception e) {
                logger.error("Error loading block sounds {}", blockDefUri, e);
            }
        }
        return sounds;
    }

    private JsonObject inheritData(AssetUri rootAssetUri, JsonObject blockDefJson) {
        JsonObject parentObj = blockDefJson;
        while (parentObj.has("basedOn")) {
            AssetUri parentUri = Assets.resolveAssetUri(AssetType.BLOCK_SOUNDS_DEFINITION, parentObj.get("basedOn").getAsString());
            if (rootAssetUri.equals(parentUri)) {
                logger.error("Circular inheritance detected in {}", rootAssetUri);
                break;
            } else if (!parentUri.isValid()) {
                logger.error("{} based on invalid uri: {}", rootAssetUri, parentObj.get("basedOn").getAsString());
                break;
            }
            JsonObject parent = readJson(parentUri).getAsJsonObject();
            JsonMergeUtil.mergeOnto(parent, blockDefJson);
            parentObj = parent;
        }
        return blockDefJson;
    }

    private JsonElement readJson(AssetUri blockDefUri) {
        try (InputStream stream = CoreRegistry.get(AssetManager.class).getAssetStream(blockDefUri)) {
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
                return parser.parse(reader);
            } else {
                logger.error("Failed to load block definition '{}'", blockDefUri);
            }
        } catch (JsonParseException e) {
            logger.error("Failed to parse block definition '{}'", blockDefUri, e);
        } catch (IOException e) {
            logger.error("Failed to load block definition '{}'", blockDefUri, e);
        }
        return null;
    }

    private BlockSoundsDefinition createBlockSoundsDefinition(JsonElement element) {
        return gson.fromJson(element, BlockSoundsDefinition.class);
    }

}
