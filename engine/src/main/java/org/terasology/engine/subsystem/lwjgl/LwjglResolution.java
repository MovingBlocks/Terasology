// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.glfw.GLFWVidMode;
import org.terasology.engine.subsystem.Resolution;

import java.util.Objects;

public final class LwjglResolution implements Resolution {

    private int width;
    private int height;
    private int redBits;
    private int greenBits;
    private int blueBits;
    private int refreshRate;

    public LwjglResolution(GLFWVidMode vidMode) {
        this.width = vidMode.width();
        this.height = vidMode.height();
        this.redBits = vidMode.redBits();
        this.greenBits = vidMode.greenBits();
        this.blueBits = vidMode.blueBits();
        this.refreshRate = vidMode.refreshRate();
    }

    public LwjglResolution(int width, int height, int redBits, int greenBits, int blueBits, int refreshRate) {
        this.width = width;
        this.height = height;
        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;
        this.refreshRate = refreshRate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRedBits() {
        return redBits;
    }

    public int getGreenBits() {
        return greenBits;
    }

    public int getBlueBits() {
        return blueBits;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        } else if (o instanceof GLFWVidMode) {
            return equals(new LwjglResolution((GLFWVidMode) o));
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        LwjglResolution that = (LwjglResolution) o;
        return width == that.width
                && height == that.height
                && redBits == that.redBits
                && greenBits == that.greenBits
                && blueBits == that.blueBits
                && refreshRate == that.refreshRate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, redBits, greenBits, blueBits, refreshRate);
    }

    @Override
    public String toString() {
        int bpp = getRedBits() + getGreenBits() + getBlueBits();
        // If colorbits < 15 (e.g. 0) or >= 24, default to 32 bpp
        if (bpp < 15 || bpp >= 24) {
            bpp = 32;
        }
        StringBuilder sb = new StringBuilder(32);
        sb.append(getWidth());
        sb.append("x");
        sb.append(getHeight());
        sb.append("x");
        sb.append(bpp);
        sb.append("@");
        sb.append(getRefreshRate());
        sb.append("Hz");
        return sb.toString();
    }
}
