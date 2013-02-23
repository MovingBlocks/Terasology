/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.assetLoaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.assets.MaterialShader;
import org.terasology.rendering.assets.Texture;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author Immortius
 */
public class MaterialLoader implements AssetLoader<Material> {

    Gson gson;

    public MaterialLoader() {
        gson = new GsonBuilder().registerTypeAdapter(MaterialMetadata.class, new MaterialMetadataHandler()).create();
    }

    @Override
    public Material load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        MaterialMetadata metadata = gson.fromJson(new InputStreamReader(stream), MaterialMetadata.class);

        MaterialShader materialShader = Assets.get(new AssetUri(AssetType.SHADER, metadata.shader), MaterialShader.class);
        if (materialShader == null) return null;

        Material result = new Material(uri, materialShader);

        for (Map.Entry<String, Texture> entry : metadata.textures.entrySet()) {
            result.setTexture(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Float> entry : metadata.floatParams.entrySet()) {
            result.setFloat(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Integer> entry : metadata.intParams.entrySet()) {
            result.setInt(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, float[]> entry : metadata.floatArrayParams.entrySet()) {
            switch (entry.getValue().length) {
                case 1:
                    result.setFloat(entry.getKey(), entry.getValue()[0]);
                    break;
                case 2:
                    result.setFloat2(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                    break;
                case 3:
                    result.setFloat3(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
                    break;
                case 4:
                    result.setFloat4(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2], entry.getValue()[3]);
                    break;
            }
        }

        return result;
    }

    private static class MaterialMetadata {
        String shader;
        Map<String, Texture> textures = Maps.newHashMap();
        Map<String, Float> floatParams = Maps.newHashMap();
        Map<String, float[]> floatArrayParams = Maps.newHashMap();
        Map<String, Integer> intParams = Maps.newHashMap();
    }

    private static class MaterialMetadataHandler implements JsonDeserializer<MaterialMetadata> {

        @Override
        public MaterialMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MaterialMetadata metadata = new MaterialMetadata();

            JsonObject obj = json.getAsJsonObject();
            if (obj.has("shader")) {
                metadata.shader = obj.getAsJsonPrimitive("shader").getAsString();
            }

            if (obj.has("params") && obj.get("params").isJsonObject()) {
                JsonObject params = obj.get("params").getAsJsonObject();
                for (Map.Entry<String, JsonElement> prop : params.entrySet()) {
                    if (prop.getValue().isJsonPrimitive()) {
                        if (prop.getValue().getAsJsonPrimitive().isString()) {
                            Texture texture = Assets.getTexture(prop.getValue().getAsString());
                            if (texture != null) {
                                metadata.textures.put(prop.getKey(), texture);
                            }
                        } else if (prop.getValue().getAsJsonPrimitive().isNumber()) {
                            metadata.floatParams.put(prop.getKey(), prop.getValue().getAsFloat());
                        } else if (prop.getValue().getAsJsonPrimitive().isBoolean()) {
                            metadata.intParams.put(prop.getKey(), (prop.getValue().getAsBoolean()) ? 1 : 0);
                        }
                    } else if (prop.getValue().isJsonArray()) {
                        JsonArray array = prop.getValue().getAsJsonArray();
                        float[] result = new float[array.size()];
                        boolean valid = true;
                        for (int i = 0; i < array.size(); ++i) {
                            if (!array.get(i).isJsonPrimitive() || !array.get(i).getAsJsonPrimitive().isNumber()) {
                                valid = false;
                                break;
                            }
                            result[i] = array.get(i).getAsFloat();
                        }
                        if (valid) {
                            metadata.floatArrayParams.put(prop.getKey(), result);
                        }
                    }
                }
            }
            return metadata;
        }
    }
}
