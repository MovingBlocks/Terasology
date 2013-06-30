/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.assets.material;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.shader.Shader;
import org.terasology.rendering.assets.texture.Texture;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class MaterialLoader implements AssetLoader<MaterialData> {

    private Gson gson;

    public MaterialLoader() {
        gson = new GsonBuilder().registerTypeAdapter(MaterialMetadata.class, new MaterialMetadataHandler()).create();
    }

    @Override
    public MaterialData load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        MaterialMetadata metadata = gson.fromJson(new InputStreamReader(stream), MaterialMetadata.class);

        Shader shader = Assets.get(new AssetUri(AssetType.SHADER, metadata.shader), Shader.class);
        if (shader == null) {
            return null;
        }

        MaterialData data = new MaterialData(shader);
        data.setTextureParams(metadata.textures);
        data.setFloatParams(metadata.floatParams);
        data.setFloatArrayParams(metadata.floatArrayParams);
        data.setIntParams(metadata.intParams);
        return data;
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