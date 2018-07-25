/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.extensionTypes.factories;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.terasology.assets.Asset;
import org.terasology.audio.StaticSound;
import org.terasology.audio.StreamingSound;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.asset.UIElement;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class AssetTypeHandlerFactoryTest {
    @Test
    public void testCreate() {
        TypeHandlerFactory factory = new AssetTypeHandlerFactory();

        List<TypeInfo<? extends Asset>> typesToTest = Lists.newArrayList(
                TypeInfo.of(Texture.class),
                TypeInfo.of(UIElement.class),
                TypeInfo.of(StaticSound.class),
                TypeInfo.of(StreamingSound.class)
        );

        for (TypeInfo<? extends Asset> typeInfo : typesToTest) {
            Optional<? extends TypeHandler<? extends Asset>> typeHandler = factory.create(typeInfo, null);

            assertTrue(typeHandler.isPresent());

            assertTrue(typeHandler.get() instanceof AssetTypeHandler);
        }
    }
}
