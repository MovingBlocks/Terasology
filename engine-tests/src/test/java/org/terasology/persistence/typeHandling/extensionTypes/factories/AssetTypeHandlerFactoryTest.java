// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes.factories;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.terasology.assets.Asset;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.StreamingSound;
import org.terasology.engine.persistence.typeHandling.extensionTypes.AssetTypeHandler;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.nui.asset.UIElement;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerFactory;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
