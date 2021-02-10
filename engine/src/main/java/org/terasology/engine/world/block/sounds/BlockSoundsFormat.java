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

package org.terasology.world.block.sounds;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.audio.StaticSound;

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
