// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.Asset;

import java.lang.reflect.Type;

public class AssetTypeAdapter<V extends Asset> implements JsonDeserializer<V> {

    private Class<V> type;

    public AssetTypeAdapter(Class<V> type) {
        this.type = type;
    }

    @Override
    public V deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return type.cast(Assets.get(json.getAsString(), type).orElse(null));
    }
}
