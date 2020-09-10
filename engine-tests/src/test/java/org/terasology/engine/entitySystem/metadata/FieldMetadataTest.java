// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.stubs.OwnerComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.nui.reflection.copy.CopyStrategyLibrary;
import org.terasology.nui.reflection.reflect.ReflectFactory;
import org.terasology.nui.reflection.reflect.ReflectionReflectFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 */
public class FieldMetadataTest {

    private final ReflectFactory factory = new ReflectionReflectFactory();
    private final CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(factory);

    @Test
    public void testOwnsAnnotationProcessed() throws NoSuchMethodException {
        ComponentMetadata<OwnerComponent> classMetadata = new ComponentMetadata<>(new ResourceUrn("unittest:owner"), OwnerComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("child");
        assertTrue(metadata.isOwnedReference());
    }

    @Test
    public void testOwnsAnnotationCollectionProcessed() throws NoSuchMethodException {
        ComponentMetadata<OwnedCollectionComponent> classMetadata =
                new ComponentMetadata<>(new ResourceUrn("unittest:OwnedCollectionComponent"), OwnedCollectionComponent.class, factory, copyStrategyLibrary);
        ComponentFieldMetadata metadata = classMetadata.getField("items");
        assertTrue(metadata.isOwnedReference());
    }

}
