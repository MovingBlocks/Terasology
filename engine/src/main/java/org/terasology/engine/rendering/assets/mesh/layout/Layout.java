// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.layout;

import java.nio.ByteBuffer;

public abstract class Layout<T> {
    public final int location;
    public final int size;
    public final T buffer;

    public Layout(int location, int size, T buffer) {
        this.location = location;
        this.size = size;
        this.buffer = buffer;
    }

    public abstract boolean hasContent();
    public abstract void write(int index, ByteBuffer data);
    public abstract int bytes();
}
