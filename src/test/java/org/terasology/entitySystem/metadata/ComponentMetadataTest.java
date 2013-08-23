/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.entitySystem.metadata;

import org.junit.Test;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.stubs.OwnerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.entitySystem.stubs.UnsupportedTypeComponent;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius
 */
public class ComponentMetadataTest {

    @Test
    public void staticFieldsIgnored() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibraryImpl(new TypeHandlerLibraryBuilder().build());
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(StringComponent.class);
        ClassMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("STATIC_VALUE"));
    }

    @Test
    public void typesWithNoPublicConstructorIgnored() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibraryImpl(new TypeHandlerLibraryBuilder().build());
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(UnsupportedTypeComponent.class);
        ClassMetadata<UnsupportedTypeComponent> metadata = lib.getMetadata(UnsupportedTypeComponent.class);
        assertNull(metadata.getField("value"));
        assertNull(metadata.getField("value2"));
        assertNull(metadata.getField("value3"));
    }

    @Test
    public void ownsReferencesPopulated() {
        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibraryImpl(new TypeHandlerLibraryBuilder().add(EntityRef.class, new EntityRefTypeHandler(null)).build());
        ComponentLibrary lib = entitySystemLibrary.getComponentLibrary();
        lib.register(OwnerComponent.class);
        ComponentMetadata<OwnerComponent> metadata = lib.getMetadata(OwnerComponent.class);
        assertTrue(metadata.isReferenceOwner());
    }

}
