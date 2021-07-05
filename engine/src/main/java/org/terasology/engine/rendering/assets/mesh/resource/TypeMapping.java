// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.opengl.GL30;

public enum TypeMapping {
    ATTR_FLOAT(Float.BYTES, GL30.GL_FLOAT),
    ATTR_SHORT(Short.BYTES, GL30.GL_SHORT),
    ATTR_BYTE(Byte.BYTES, GL30.GL_BYTE),
    ATTR_INT(Integer.BYTES, GL30.GL_INT);

    public final int size;
    public final int glType;

    TypeMapping(int size, int glType) {
        this.size = size;
        this.glType = glType;
    }
}
