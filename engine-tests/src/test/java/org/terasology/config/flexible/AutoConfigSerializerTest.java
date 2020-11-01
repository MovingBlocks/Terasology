/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AutoConfigSerializerTest {
    private static final String NON_DEFAULT_JSON = "{\"integerListSetting\":[1,2,3],\"Human Readable Name\":\"xyz\"}";
    private static final String DEFAULT_JSON = "{}";

    private final Gson gson = new Gson();
    private final TestAutoConfig config = new TestAutoConfig();

    private AutoConfigSerializer<TestAutoConfig> autoConfigSerializer;

    @BeforeEach
    public void setup() {
        TypeHandlerLibrary library = TypeHandlerLibrary.withReflections(mock(Reflections.class));
        autoConfigSerializer = new AutoConfigSerializer<>(TestAutoConfig.class, library);
    }

    @Test
    public void testSerializeAllDefault() {
        assertEquals(DEFAULT_JSON, serialize());
    }

    @Test
    public void testSerializeNonDefault() {
        config.integerListSetting.set(ImmutableList.of(1, 2, 3));
        config.stringSetting.set("xyz");

        assertEquals(NON_DEFAULT_JSON, serialize());
    }

    private String serialize() {
        return gson.toJson(autoConfigSerializer.serialize(config));
    }

    @Test
    public void testDeserializeAllDefault() {
        deserializeOnto(DEFAULT_JSON);

        assertEquals(config.stringSetting.getDefaultValue(), config.stringSetting.get());
        assertEquals(config.integerListSetting.getDefaultValue(), config.integerListSetting.get());
    }

    @Test
    public void testDeserializeNonDefault() {
        deserializeOnto(NON_DEFAULT_JSON);

        assertEquals("xyz", config.stringSetting.get());
        assertEquals(ImmutableList.of(1, 2, 3), config.integerListSetting.get());
    }

    private void deserializeOnto(String json) {
        autoConfigSerializer.deserializeOnto(config, gson.fromJson(json, JsonElement.class));
    }
}
