// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.gson.models;

import java.util.Objects;

public class TestRect2i {
    int minX;
    int minY;
    int sizeX;
    int sizeY;

    public TestRect2i(int minX, int minY, int sizeX, int sizeY) {
        this.minX = minX;
        this.minY = minY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestRect2i that = (TestRect2i) o;
        return minX == that.minX &&
                minY == that.minY &&
                sizeX == that.sizeX &&
                sizeY == that.sizeY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, sizeX, sizeY);
    }
}
