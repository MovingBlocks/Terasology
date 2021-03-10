// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class AutoConfigSerializerTest {
    private static final String NON_DEFAULT_JSON = "{\"Human Readable Name\":\"xyz\",\"integerListSetting\":[1,2,3]}";
    private static final String DEFAULT_JSON = "{}";

    private final Gson gson = new Gson();
    private final TestAutoConfig config = new TestAutoConfig();

    private AutoConfigSerializer<TestAutoConfig> autoConfigSerializer;

    @BeforeEach
    public void setup() {
        TypeHandlerLibrary library = TypeHandlerLibraryImpl.withReflections(mock(Reflections.class));
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
