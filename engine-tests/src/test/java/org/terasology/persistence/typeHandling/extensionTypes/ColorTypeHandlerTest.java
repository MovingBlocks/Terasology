/*
 * Copyright 2016 MovingBlocks
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

package org.terasology.persistence.typeHandling.extensionTypes;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataArray;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.rendering.nui.Color;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * Tests the {@link ColorTypeHandler} class.
 */
public class ColorTypeHandlerTest {

    private final ColorTypeHandler handler = new ColorTypeHandler();
    private final DeserializationContext deserializationContext = Mockito.mock(DeserializationContext.class);

    @Test
    public void testSerialize() {
        SerializationContext serializationContext = Mockito.mock(SerializationContext.class);
        handler.serialize(new Color(0x010380FF), serializationContext);
        Mockito.verify(serializationContext).create(1, 3, 128, 255);
    }

    @Test
    public void testDeserializeHex() {
        PersistedData data = new PersistedString("DEADBEEF");
        Color color = handler.deserialize(data, deserializationContext);
        Assert.assertEquals(0xDEADBEEF, color.rgba());
    }

    @Test
    public void testDeserializeArray() {
        JsonArray array = new Gson().fromJson("[12, 34, 56, 78]", JsonArray.class);
        PersistedData data = new GsonPersistedDataArray(array);
        Color color = handler.deserialize(data, deserializationContext);
        Assert.assertEquals(12, color.r());
        Assert.assertEquals(34, color.g());
        Assert.assertEquals(56, color.b());
        Assert.assertEquals(78, color.a());
    }
}
