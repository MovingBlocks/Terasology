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
package org.terasology.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import gnu.trove.set.hash.TIntHashSet;

import java.lang.reflect.Type;

/**
 * @author soniex2
 */
public class TIntHashSetTypeAdapter implements JsonDeserializer<TIntHashSet>, JsonSerializer<TIntHashSet> {
    @Override
    public TIntHashSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int[] array = context.deserialize(json, int[].class);
        if (array == null) {
            return null;
        }
        TIntHashSet hashSet = new TIntHashSet();
        hashSet.addAll(array);
        return hashSet;
    }

    @Override
    public JsonElement serialize(TIntHashSet src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        return context.serialize(src.toArray());
    }
}
