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
package org.terasology.persistence.typeHandling.gson;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 */
public class GsonPersistedDataSerializer implements PersistedDataSerializer {

    private static final PersistedData NULL_INSTANCE = new GsonPersistedData(JsonNull.INSTANCE);

    @Override
    public PersistedData create(String value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(String... values) {
        return createStrings(Arrays.asList(values));
    }

    @Override
    public PersistedData createStrings(Iterable<String> value) {
        JsonArray array = new JsonArray();
        for (String val : value) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(float value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(float... values) {
        JsonArray array = new JsonArray();
        for (float val : values) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(TFloatIterator value) {
        JsonArray array = new JsonArray();
        while (value.hasNext()) {
            array.add(new JsonPrimitive(value.next()));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(int value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(int... values) {
        JsonArray array = new JsonArray();
        for (int val : values) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(TIntIterator value) {
        JsonArray array = new JsonArray();
        while (value.hasNext()) {
            array.add(new JsonPrimitive(value.next()));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(long value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(long... values) {
        JsonArray array = new JsonArray();
        for (long val : values) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(TLongIterator value) {
        JsonArray array = new JsonArray();
        while (value.hasNext()) {
            array.add(new JsonPrimitive(value.next()));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(boolean value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(boolean... values) {
        JsonArray array = new JsonArray();
        for (boolean val : values) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(double value) {
        return new GsonPersistedData(new JsonPrimitive(value));
    }

    @Override
    public PersistedData create(double... values) {
        JsonArray array = new JsonArray();
        for (double val : values) {
            array.add(new JsonPrimitive(val));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(TDoubleIterator value) {
        JsonArray array = new JsonArray();
        while (value.hasNext()) {
            array.add(new JsonPrimitive(value.next()));
        }
        return new GsonPersistedData(array);
    }

    @Override
    public PersistedData create(byte[] value) {
        return new GsonPersistedData(new JsonPrimitive(BaseEncoding.base64().encode(value)));
    }

    @Override
    public PersistedData create(ByteBuffer value) {
        return create(value.array());
    }

    @Override
    public PersistedData create(PersistedData... data) {
        return create(Arrays.asList(data));
    }

    @Override
    public PersistedData create(Iterable<PersistedData> data) {
        JsonArray result = new JsonArray();
        for (PersistedData val : data) {
            if (val != null) {
                result.add(((GsonPersistedData) val).getElement());
            } else {
                result.add(JsonNull.INSTANCE);
            }
        }
        return new GsonPersistedData(result);
    }

    @Override
    public PersistedData create(Map<String, PersistedData> data) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, PersistedData> entry : data.entrySet()) {
            object.add(entry.getKey(), ((GsonPersistedData) entry.getValue()).getElement());
        }
        return new GsonPersistedData(object);
    }

    @Override
    public PersistedData createNull() {
        return NULL_INSTANCE;
    }
}
