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
package org.terasology.persistence.serializers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.reflections.Reflections;
import org.terasology.math.geom.Vector3f;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.Color;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class TypeSerializerTest {
    private static final SomeClass<Integer> INSTANCE = new SomeClass<>(0xdeadbeef);

    static {
        INSTANCE.list.addAll(Lists.newArrayList(50, 51, -52, -53));

        INSTANCE.animals.add(new Dog(new Vector3f(3.15f, 54.51f, -0.001f)));

        INSTANCE.animals.add(new Cheetah(Color.MAGENTA));
    }

    public static class Json {
        private static final String INSTANCE_JSON = "{\"data\":-559038737,\"list\":[50,51,-52,-53],\"animals\":[{\"class\":\"org.terasology.persistence.serializers.TypeSerializerTest$Dog\",\"content\":{\"tailPosition\":[3.15,54.51,-0.001],\"name\":\"Dog\"}},{\"class\":\"org.terasology.persistence.serializers.TypeSerializerTest$Cheetah\",\"content\":{\"spotColor\":[255,0,255,255],\"name\":\"Cheetah\"}}]}";

        private final Reflections reflections = new Reflections(getClass().getClassLoader());

        private final TypeHandlerLibrary typeHandlerLibrary =
                TypeHandlerLibrary.withDefaultHandlers(reflections);

        private final GsonSerializer serializer = new GsonSerializer(typeHandlerLibrary);

        @Test
        public void testJsonSerialize() {
            String serializedJson = serializer.toJson(INSTANCE, new TypeInfo<SomeClass<Integer>>() {});
            assertEquals(INSTANCE_JSON, serializedJson);
        }

        @Test
        public void testDeserialize() {
            SomeClass<Integer> deserialized =
                    serializer.fromJson(INSTANCE_JSON, new TypeInfo<SomeClass<Integer>>() {});

            assertEquals(INSTANCE, deserialized);
        }
    }

    public static class Protobuf {
        final Reflections reflections = new Reflections(getClass().getClassLoader());

        private final TypeHandlerLibrary typeHandlerLibrary =
                TypeHandlerLibrary.withDefaultHandlers(reflections);

        @Test
        public void testSerializeDeserialize() throws IOException {
            TypeHandler<SomeClass<Integer>> typeHandler = typeHandlerLibrary.getTypeHandler(new TypeInfo<SomeClass<Integer>>() {}).get();

            ProtobufSerializer protobufSerializer = new ProtobufSerializer();

            byte[] bytes = protobufSerializer.toBytes(INSTANCE, typeHandler);

            PersistedData persistedData = protobufSerializer.persistedDatafromBytes(bytes);

            SomeClass<Integer> deserializedInstance = typeHandler.deserialize(persistedData).get();

            assertEquals(INSTANCE, deserializedInstance);
        }
    }

    private static class SomeClass<T> {
        private T data;
        private List<T> list = Lists.newArrayList();
        private Set<Animal> animals = Sets.newHashSet();

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

    private static abstract class Animal {
        protected final String name;

        private Animal(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Animal animal = (Animal) o;
            return Objects.equals(name, animal.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    private static class Dog extends Animal {
        private final Vector3f tailPosition;

        private Dog(Vector3f tailPosition) {
            super("Dog");
            this.tailPosition = tailPosition;
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
            Dog dog = (Dog) o;
            return Objects.equals(tailPosition, dog.tailPosition);
        }

        @Override
        public String toString() {
            return "Dog{" +
                    "name='" + name + '\'' +
                    ", tailPosition=" + tailPosition +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tailPosition);
        }
    }

    private static class Cheetah extends Animal {
        private final Color spotColor;

        private Cheetah(Color spotColor) {
            super("Cheetah");
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
            Cheetah cheetah = (Cheetah) o;
            return Objects.equals(spotColor, cheetah.spotColor);
        }

        @Override
        public String toString() {
            return "Cheetah{" +
                    "name='" + name + '\'' +
                    ", spotColor=" + spotColor +
                    '}';
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), spotColor);
        }
    }
}
