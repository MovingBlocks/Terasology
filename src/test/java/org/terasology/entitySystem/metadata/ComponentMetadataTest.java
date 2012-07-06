/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.entitySystem.stubs.UnsupportedTypeComponent;

import static org.junit.Assert.assertNull;

/**
 * @author Immortius
 */
public class ComponentMetadataTest {

    @Test
    public void staticFieldsIgnored() {
        ComponentLibrary lib = new ComponentLibraryImpl();
        lib.registerComponentClass(StringComponent.class);
        ComponentMetadata<StringComponent> metadata = lib.getMetadata(StringComponent.class);
        assertNull(metadata.getField("staticValue"));
    }

    @Test
    public void typesWithNoPublicConstructorIgnored() {
        ComponentLibrary lib = new ComponentLibraryImpl();
        lib.registerComponentClass(UnsupportedTypeComponent.class);
        ComponentMetadata<UnsupportedTypeComponent> metadata = lib.getMetadata(UnsupportedTypeComponent.class);
        assertNull(metadata.getField("value"));
        assertNull(metadata.getField("value2"));
        assertNull(metadata.getField("value3"));
    }

}
