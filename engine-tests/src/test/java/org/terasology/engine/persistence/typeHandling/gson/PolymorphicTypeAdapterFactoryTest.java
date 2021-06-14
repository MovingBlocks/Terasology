// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolymorphicTypeAdapterFactoryTest {
    private static final Dog DOG = new Dog(1.25f);
    private static final Animal CAT = new Animal("Cat");
    private static final Cheetah CHEETAH = new Cheetah(21);

    private final Gson baseClassGson = new GsonBuilder()
            .registerTypeAdapterFactory(PolymorphicTypeAdapterFactory.of(Animal.class))
            .create();

    private final Gson interfaceGson = new GsonBuilder()
            .registerTypeAdapterFactory(PolymorphicTypeAdapterFactory.of(Walker.class))
            .create();

    @Test
    public void testInterfaceReference() {
        String json = interfaceGson.toJson(CAT);

        Walker newAnimal = interfaceGson.fromJson(json, Walker.class);

        assertTrue(newAnimal instanceof Animal);
    }

    @Test
    public void testBaseClassReference() {
        String json = baseClassGson.toJson(CHEETAH);

        Animal newAnimal = baseClassGson.fromJson(json, Animal.class);

        assertTrue(newAnimal instanceof Cheetah);
    }

    @Test
    public void testInnerField() {
        Capsule capsule = new Capsule(DOG);

        String json = baseClassGson.toJson(capsule);

        Capsule newCapsule = baseClassGson.fromJson(json, Capsule.class);
        assertTrue(newCapsule.animal instanceof Dog);
    }

    @Test
    public void testBaseClassList() {
        List<Animal> animals = Lists.newArrayList(CAT, DOG, CHEETAH);

        String json = baseClassGson.toJson(animals);

        List<Animal> newAnimals = baseClassGson.fromJson(json, new TypeToken<List<Animal>>() {
        }.getType());

        assertTrue(newAnimals.get(1) instanceof Dog);
        assertTrue(newAnimals.get(2) instanceof Cheetah);
    }

    @Test
    public void testInterfaceList() {
        List<Walker> walkers = Lists.newArrayList(CAT, DOG, CHEETAH);

        String json = interfaceGson.toJson(walkers);

        List<Walker> newWalkers = interfaceGson.fromJson(json, new TypeToken<List<Walker>>() {
        }.getType());

        assertTrue(newWalkers.get(0) instanceof Animal);
        assertTrue(newWalkers.get(1) instanceof Dog);
        assertTrue(newWalkers.get(2) instanceof Cheetah);
    }

    private static final class Capsule {
        private final Animal animal;

        private Capsule(Animal animal) {
            this.animal = animal;
        }
    }

    private interface Walker {
    }

    @SuppressWarnings("checkstyle:FinalClass")
    private static class Animal implements Walker {
        private final String name;

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
            return Objects.equal(name, animal.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name);
        }
    }

    private static final class Dog extends Animal {
        private final float tailLength;

        private Dog(float tailLength) {
            super("Dog");
            this.tailLength = tailLength;
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
            return Float.compare(dog.tailLength, tailLength) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), tailLength);
        }
    }

    private static final class Cheetah extends Animal {
        private final int spotCount;

        private Cheetah(int spotCount) {
            super("Cheetah");
            this.spotCount = spotCount;
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
            return spotCount == cheetah.spotCount;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), spotCount);
        }
    }
}
