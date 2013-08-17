/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.metadata.internal;

import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Immortius
 */
public class MetadataBuilderTest {

    private TypeHandlerLibrary metadataBuilder;

    @Before
    public void setup() {
        this.metadataBuilder = new TypeHandlerLibraryBuilder().build();
    }

    @Test
    public void requireDefaultConstructor() {
        assertNull(metadataBuilder.build(NoDefaultConstructor.class, false));
    }

    @Test
    public void trivialMetadata() {
        ClassMetadata<Trivial> metadata = metadataBuilder.build(Trivial.class, false);
        assertNotNull(metadata);
        assertEquals(0, metadata.size());
    }

    @Test
    public void testPrivateField() {
        ClassMetadata<PrivateField> metadata = metadataBuilder.build(PrivateField.class, false);
        assertNotNull(metadata);
        assertEquals(1, metadata.size());
        FieldMetadata fieldMetadata = metadata.getField("name");
        assertNotNull(fieldMetadata);
        assertEquals(String.class, fieldMetadata.getType());
        assertEquals("name", fieldMetadata.getName());
        assertNotNull(metadata.newInstance());
    }

    @Test
    public void testInheritsFields() {
        ClassMetadata<Inheriting> metadata = metadataBuilder.build(Inheriting.class, false);
        assertNotNull(metadata);
        assertEquals(2, metadata.size());
        assertNotNull(metadata.getField("name"));
        assertNotNull(metadata.getField("value"));
        assertNotNull(metadata.newInstance());
    }

    private static class NoDefaultConstructor {
        public NoDefaultConstructor(String name) {

        }
    }

    public static class Trivial {
    }

    public static class PrivateField {
        private String name;

        private PrivateField() {
        }

        public PrivateField(String name) {
            this.name = name;
        }

        public String whatName() {
            return name;
        }
    }

    public static class Inheriting extends PrivateField {
        private String value;

        private Inheriting() {
        }

        public Inheriting(String name, String value) {
            super(name);
            this.value = value;
        }

        public String whatValue() {
            return value;
        }
    }
}
