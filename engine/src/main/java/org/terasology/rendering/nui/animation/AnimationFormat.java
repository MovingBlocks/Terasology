/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.rendering.nui.animation;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 */
@RegisterAssetFileFormat
public class MaterialFormat extends AbstractAssetFileFormat<MaterialData> {

    private final Gson gson;
    private final AssetManager assetManager;

    public MaterialFormat(AssetManager assetManager) {
        super("anim");
        this.gson = new GsonBuilder().registerTypeAdapter
            (Animation.class, new AnimationMetadataHandler()).create();
        this.assetManager = assetManager;
    }

    @Override
    public Animation load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        return gson.fromJson(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8), Animation.class);
    }

    private class AnimationHandler implements JsonDeserializer<Animation> {
        @Override
        public Animation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Animation metadata = new Animation();

            JsonObject obj = json.getAsJsonObject();
            // if (obj.has("shader")) {
            //     metadata.shader = obj.getAsJsonPrimitive("shader").getAsString();
            // }

            // if (obj.has("params") && obj.get("params").isJsonObject()) {
            //     JsonObject params = obj.get("params").getAsJsonObject();
            //     for (Map.Entry<String, JsonElement> prop : params.entrySet()) {
            //         if (prop.getValue().isJsonPrimitive()) {
            //             if (prop.getValue().getAsJsonPrimitive().isString()) {
            //                 Optional<? extends Texture> texture = assetManager.getAsset(prop.getValue().getAsString(), Texture.class);
            //                 if (texture.isPresent()) {
            //                     metadata.textures.put(prop.getKey(), texture.get());
            //                 }
            //             } else if (prop.getValue().getAsJsonPrimitive().isNumber()) {
            //                 metadata.floatParams.put(prop.getKey(), prop.getValue().getAsFloat());
            //             } else if (prop.getValue().getAsJsonPrimitive().isBoolean()) {
            //                 metadata.intParams.put(prop.getKey(), (prop.getValue().getAsBoolean()) ? 1 : 0);
            //             }
            //         } else if (prop.getValue().isJsonArray()) {
            //             JsonArray array = prop.getValue().getAsJsonArray();
            //             float[] result = new float[array.size()];
            //             boolean valid = true;
            //             for (int i = 0; i < array.size(); ++i) {
            //                 if (!array.get(i).isJsonPrimitive() || !array.get(i).getAsJsonPrimitive().isNumber()) {
            //                     valid = false;
            //                     break;
            //                 }
            //                 result[i] = array.get(i).getAsFloat();
            //             }
            //             if (valid) {
            //                 metadata.floatArrayParams.put(prop.getKey(), result);
            //             }
            //         }
            //     }
            // }
            return metadata;
        }
