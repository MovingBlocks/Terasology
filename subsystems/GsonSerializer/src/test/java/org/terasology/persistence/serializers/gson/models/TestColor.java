// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.serializers.gson.models;

import java.util.Objects;

public class TestColor {
    int representation;

    public TestColor(int representation) {
        this.representation = representation;
    }

    public TestColor(int r, int g, int b, int a) {
        representation = (r << 24) + (g << 16) + (b << 8) + a;
    }

    public int r() {
        return (representation >> 24) & 0xFF;
    }

    public int g() {
        return (representation >> 16) & 0xFF;
    }

    public int b() {
        return (representation >> 8) & 0xFF;
    }

    public int a() {
        return representation & 0xFF;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestColor testColor = (TestColor) o;
        return representation == testColor.representation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(representation);
    }
}
