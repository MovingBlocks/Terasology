// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.extensionTypes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.terasology.nui.Color;
import org.terasology.persistence.serializers.gson.GsonPersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the {@link ColorTypeHandler} class.
 */
public class ColorTypeHandlerTest {

    private final ColorTypeHandler handler = new ColorTypeHandler();

    @Test
    public void testSerialize() {
        PersistedDataSerializer persistedDataSerializer = Mockito.mock(PersistedDataSerializer.class);
        handler.serialize(new Color(0x010380FF), persistedDataSerializer);
        Mockito.verify(persistedDataSerializer).serialize(1, 3, 128, 255);
    }

    @Test
    public void testDeserializeHex() {
        PersistedData data = new PersistedString("DEADBEEF");
        Color color = handler.deserialize(data).get();
        assertEquals(0xDEADBEEF, color.rgba());
    }

    @Test
    public void testDeserializeArray() {
        JsonArray array = new Gson().fromJson("[12, 34, 56, 78]", JsonArray.class);
        PersistedData data = new GsonPersistedDataArray(array);
        Color color = handler.deserialize(data).get();
        assertEquals(12, color.r());
        assertEquals(34, color.g());
        assertEquals(56, color.b());
        assertEquals(78, color.a());
    }
}
