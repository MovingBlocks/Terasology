// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.sounds;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.audio.StaticSound;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

/**
 * Block sounds loader will load all block sounds definitions and build them into immutable objects.
 */
@RegisterAssetFileFormat
public class BlockSoundsFormat extends AbstractAssetFileFormat<BlockSoundsData> {

    private final AssetManager assetManager;
    private JsonParser parser;

    public BlockSoundsFormat(AssetManager assetManager) {
        super("blocksounds");
        this.assetManager = assetManager;
        parser = new JsonParser();
    }

    @Override
    public BlockSoundsData load(ResourceUrn resourceUrn, List<AssetDataFile> list) throws IOException {
        JsonElement rawJson = readJson(list.get(0));
        JsonObject blockDefJson = rawJson.getAsJsonObject();

        BlockSoundsData data = new BlockSoundsData();
        if (blockDefJson.has("basedOn")) {
            Optional<BlockSounds> parentBlockSounds = assetManager.getAsset(blockDefJson.get("basedOn").getAsString(), BlockSounds.class);
            if (parentBlockSounds.isPresent()) {
                data.getStepSounds().addAll(parentBlockSounds.get().getStepSounds());
                data.getStepSounds().addAll(parentBlockSounds.get().getDestroySounds());
                data.getStepSounds().addAll(parentBlockSounds.get().getDigSounds());
            } else {
                throw new IOException("Unable to resolve parent '" + blockDefJson.get("basedOn").getAsString() + "'");
            }
        }

        loadBlockSounds(blockDefJson, data);
        return data;
    }

    private JsonElement readJson(AssetDataFile input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input.openStream(), Charsets.UTF_8))) {
            return parser.parse(reader);
        }
    }

    private void loadBlockSounds(JsonObject element, BlockSoundsData data) throws IOException {
        readSoundList(element, "stepSounds", data.getStepSounds());
        readSoundList(element, "destroySounds", data.getDestroySounds());
        readSoundList(element, "digSounds", data.getDigSounds());
    }

    private void readSoundList(JsonObject element, String field, List<StaticSound> sounds) throws IOException {
        if (element.has(field) && element.get(field).isJsonArray()) {
            sounds.clear();
            for (JsonElement item : element.getAsJsonArray(field)) {
                Optional<StaticSound> sound = assetManager.getAsset(item.getAsString(), StaticSound.class);
                if (sound.isPresent()) {
                    sounds.add(sound.get());
                } else {
                    throw new IOException("Unable to resolve sound '" + item.getAsString() + "'");
                }
            }
        }
    }


}
