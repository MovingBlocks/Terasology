// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

/**
 * a binding that maps depending on the type of attribute and a resource where the data is committed to
 * @param <TARGET>
 */
public abstract class VertexAttributeBinding<TARGET> {
    protected VertexResource resource;

    public VertexAttributeBinding(VertexResource resource) {
        this.resource = resource;
    }

    public VertexResource getResource() {
        return resource;
    }

    /**
     * A store of fixed elements that describes that data stored in the {@link VertexResource}.
     *
     * access a buffer is slow so examining an array of {@link TARGET}s is quicker.
     * @return the store that describes this attribute
     */
    public abstract TARGET[] getStore();

    /**
     * rewind the index back to 0
     */
    public abstract void rewind();

    /**
     * the number of {@link TARGET} elements that this binding supports.
     * @return the number of elements
     */
    public abstract int count();

    /**
     * maps a float array to the resource and to the {@link TARGET} store if one exist.
     * @param startIndex the starting index into the arr
     * @param endIndex the ending index into the array
     * @param arr the array
     * @param offsetIndex offset into the store
     */
    public abstract void map(int startIndex, int endIndex, float[] arr, int offsetIndex);

    /**
     * write a value by the index.
     * @param vertexIndex the index
     * @param value the value to commit
     */
    public abstract void put(int vertexIndex, TARGET value);

    /**
     * write a value and move the index forward
     * @param value
     */
    public abstract void put(TARGET value);

    /**
     * commits whatever is in the store to the buffer if the store has been modified in any way
     */
    public abstract void commit();
}
