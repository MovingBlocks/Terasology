/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.asset.Asset;
import org.terasology.asset.AssetType;
import org.terasology.asset.Assets;

import java.lang.reflect.Type;

/**
 * @author Immortius
 */
public class AssetTypeAdapter<V extends Asset> implements JsonDeserializer<V> {

    private AssetType type;

    public AssetTypeAdapter(AssetType type) {
        this.type = type;
    }

    @Override
    public V deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return (V) Assets.get(type, json.getAsString());
    }
}
