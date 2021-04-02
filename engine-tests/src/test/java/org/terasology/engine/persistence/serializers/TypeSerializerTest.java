// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.serializers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.terasology.engine.ModuleEnvironmentTest;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.Color;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.annotations.SerializedName;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeSerializerTest extends ModuleEnvironmentTest {
    private static final SomeClass<Integer> INSTANCE = new SomeClass<>(0xdeadbeef);
    private static final String INSTANCE_JSON = "{\"generic-t\":-559038737,\"list\":[50,51,-52,-53],\"animals\":[{\"class\":\"org.terasology.engine.persistence.serializers.TypeSerializerTest$Dog\",\"tailPosition\":[3.15,54.51,-0.001],\"headPosition\":[10.0,30.0,-0.001],\"data\":{\"class\":\"java.lang.Integer\",\"content\":1}},{\"class\":\"org.terasology.engine.persistence.serializers.TypeSerializerTest$Cheetah\",\"spotColor\":[255,0,255,255],\"data\":{\"class\":\"java.lang.Integer\",\"content\":2}}]}";

    static {
        INSTANCE.list.addAll(Lists.newArrayList(50, 51, -52, -53));

        INSTANCE.animals.add(new Dog<>(1, new Vector3f(3.15f, 54.51f, -0.001f), new org.joml.Vector3f(10.0f,30.0f,-0.001f)));

        INSTANCE.animals.add(new Cheetah<>(2, Color.MAGENTA));
    }

    private TypeHandlerLibrary typeHandlerLibrary;
    private ProtobufSerializer protobufSerializer;
    private Serializer gsonSerializer;

    @Override
    public void setup() {
        ModuleContext.setContext(moduleManager.getEnvironment().get(new Name("unittest")));

        typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);

        protobufSerializer = new ProtobufSerializer(typeHandlerLibrary);

        Gson gson = new Gson();
        gsonSerializer = new Serializer<>(typeHandlerLibrary,
                new GsonPersistedDataSerializer(),
                new GsonPersistedDataWriter(gson),
                new GsonPersistedDataReader(gson)
                );
    }

    @Test
    void testJsonSerialize() {
        Optional<byte[]> serialize = gsonSerializer.serialize(INSTANCE, new TypeInfo<SomeClass<Integer>>() {
        });
        assertTrue(serialize.isPresent());

        String serializedJson = new String(serialize.get(), TerasologyConstants.CHARSET);
        assertEquals(INSTANCE_JSON, serializedJson);
    }

    @Test
    void testDeserialize() {
        Optional<SomeClass<Integer>> deserialize = gsonSerializer.deserialize(
                new TypeInfo<SomeClass<Integer>>() {},
                INSTANCE_JSON.getBytes(TerasologyConstants.CHARSET));
        assertTrue(deserialize.isPresent());

        SomeClass<Integer> deserialized = deserialize.get();

        assertEquals(INSTANCE, deserialized);
    }

    @Test
    void testSerializeDeserialize() throws IOException {
        byte[] bytes = protobufSerializer.toBytes(INSTANCE, new TypeInfo<SomeClass<Integer>>() {
        });

        SomeClass<Integer> deserializedInstance =
                protobufSerializer.fromBytes(bytes, new TypeInfo<SomeClass<Integer>>() {
                });

        assertEquals(INSTANCE, deserializedInstance);
    }

    private static class SomeClass<T> {
        @SerializedName("generic-t")
        private T data;
        private List<T> list = Lists.newArrayList();
        private Set<Animal<?>> animals = Sets.newHashSet();

        private SomeClass(T data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SomeClass<?> someClass = (SomeClass<?>) o;
            return Objects.equals(data, someClass.data) &&
                    Objects.equals(list, someClass.list) &&
                    Objects.equals(animals, someClass.animals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data, list, animals);
        }

        @Override
        public String toString() {
            return "SomeClass{" +
                    "data=" + data +
                    ", list=" + list +
                    ", animals=" + animals +
                    '}';
        }
    }

    private static class Animal<T> {
        protected final T data;

        private Animal(T data) {
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Animal<?> animal = (Animal<?>) o;
            return Objects.equals(data, animal.data);
        }

        @Override
        public int hashCode() {
            return Objects.hash(data);
        }
    }

    private static class Dog<T> extends Animal<T> {
        private final Vector3f tailPosition;
        private final org.joml.Vector3f headPosition;

        private Dog(T data, Vector3f tailPosition,Vector3f headPosition) {
            super(data);
            this.tailPosition = tailPosition;
            this.headPosition = headPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Dog<?> dog = (Dog<?>) o;
            return Objects.equals(tailPosition, dog.tailPosition) &&  Objects.equals(headPosition, dog.headPosition);
        }

        @Override
        public String toString() {
            return "Dog{" +
                "name='" + data + '\'' +
                ", tailPosition=" + tailPosition +
                ", headPosition=" + headPosition +
                '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tailPosition, headPosition);
        }
    }

    private static class Cheetah<T> extends Animal<T> {
        private final Color spotColor;

        private Cheetah(T data, Color spotColor) {
            super(data);
            this.spotColor = spotColor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Cheetah<?> cheetah = (Cheetah<?>) o;
            return Objects.equals(spotColor, cheetah.spotColor);
        }

        @Override
        public String toString() {
            return "Cheetah{" +
                    "name='" + data + '\'' +
                    ", spotColor=" + spotColor +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), spotColor);
        }
    }
}
