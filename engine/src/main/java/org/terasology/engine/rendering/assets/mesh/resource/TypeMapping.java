// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.opengl.GL30;

import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_FLOAT;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_INT16;
import static org.lwjgl.bgfx.BGFX.BGFX_ATTRIB_TYPE_UINT8;

public enum TypeMapping {
    ATTR_FLOAT(Float.BYTES, GL30.GL_FLOAT, BGFX_ATTRIB_TYPE_FLOAT),
    ATTR_SHORT(Short.BYTES, GL30.GL_SHORT, BGFX_ATTRIB_TYPE_INT16),
    ATTR_BYTE(Byte.BYTES, GL30.GL_BYTE, BGFX_ATTRIB_TYPE_UINT8),
    ATTR_INT(Integer.BYTES, GL30.GL_INT, BGFX_ATTRIB_TYPE_FLOAT);

    public final int size;
    public final int glType;
    public final int bgfxType;

    TypeMapping(int size, int glType, int bgfxType) {
        this.size = size;
        this.glType = glType;
        this.bgfxType = bgfxType;
    }
}
