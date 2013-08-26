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
package org.terasology.entitySystem.metadata.internal;

import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.copying.CopyStrategyLibrary;
import org.terasology.entitySystem.metadata.reflect.ReflectFactory;
import org.terasology.entitySystem.metadata.reflect.ReflectionReflectFactory;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Immortius
 */
public class MetadataBuilderTest {

    private ReflectFactory factory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategyLibrary = CopyStrategyLibrary.create(factory);
    private TypeSerializationLibrary metadataBuilder;

    @Before
    public void setup() {
        this.metadataBuilder = new TypeSerializationLibrary(factory, copyStrategyLibrary);
    }

    @Test(expected = NoSuchMethodException.class)
    public void requireDefaultConstructor() throws Exception {
        new ClassMetadataImpl<>(NoDefaultConstructor.class, copyStrategyLibrary, factory, "");
    }

    @Test
    public void trivialMetadata() throws Exception {
        ClassMetadata<Trivial> metadata = new ClassMetadataImpl<>(Trivial.class, copyStrategyLibrary, factory, "");
        assertNotNull(metadata);
        assertEquals(0, metadata.getFieldCount());
    }

    @Test
    public void testPrivateField() throws Exception {
        ClassMetadata<PrivateField> metadata = new ClassMetadataImpl<>(PrivateField.class, copyStrategyLibrary, factory, "");
        assertNotNull(metadata);
        assertEquals(1, metadata.getFieldCount());
        FieldMetadata fieldMetadata = metadata.getField("name");
        assertNotNull(fieldMetadata);
        assertEquals(String.class, fieldMetadata.getType());
        assertEquals("name", fieldMetadata.getName());
        assertNotNull(metadata.newInstance());
    }

    @Test
    public void testInheritsFields() throws Exception {
        ClassMetadata<Inheriting> metadata = new ClassMetadataImpl<>(Inheriting.class, copyStrategyLibrary, factory, "");
        assertNotNull(metadata);
        assertEquals(2, metadata.getFieldCount());
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
