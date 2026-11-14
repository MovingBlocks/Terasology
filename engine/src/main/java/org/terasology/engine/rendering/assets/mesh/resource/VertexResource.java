// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

/**
 * A resource that represents vertex data
 */
public class VertexResource extends BufferedResource {
    public static final int FEATURE_NORMALIZED = 0x1;
    public static final int FEATURE_INTEGER = 0x2;

    private int inStride = 0;
    private VertexDefinition[] attributes;

    public VertexResource() {

    }

    public VertexResource(int inSize, int inStride, VertexDefinition[] attributes) {
        ensureCapacity(inSize);
        this.inStride = inStride;
        this.attributes = attributes;
    }

    /**
     * the number of elements in the vertex resource / verticies
     * @return the number of verts
     */
    public int elements() {
        return inSize / inStride;
    }

    /**
     * definition information that the end consumer uses to determine the layout of the vertex data
     * @return the definition
     */
    public VertexDefinition[] definitions() {
        return this.attributes;
    }

    /**
     * set definition data that describes the {@link VertexResource}
     * @param attr
     */
    public void setDefinitions(VertexDefinition[] attr) {
        this.attributes = attr;
    }

    /**
     * the stride of the data where each jump is another vertex
     * @return
     */
    public int inStride() {
        return inStride;
    }

    /**
     * reserve the number of elements in the vertex resource to match ({@link #inStride()} * verts)
     * @param verts the number of verts to reserve
     */
    public void reserveElements(int verts) {
        int size = verts * inStride;
        reserve(size);
    }

    /**
     * allocate the {@link VertexResource} resource to match ({@link #inStride()} * verts)
     * and set the {@link #inSize()} of the buffer to match the number of verts.
     *
     * @param verts number of vertices
     */
    public void allocateElements(int verts) {
        int size = verts * inStride;
        allocate(size);
    }

    /**
     * Ensure the {@link VertexResource} resource has the minimum
     * capacity else increase to match({@link #inStride() * verts}
     *
     * @param verts number of vertices
     */
    public void ensureElements(int verts) {
        int size = verts * inStride;
        ensureCapacity(size);
    }

    /**
     * reallocate the vertex resource with the given size and stride
     * @param size the size
     * @param stride the stride of the data
     */
    public void allocate(int size, int stride) {
        this.allocate(size);
        this.inStride = stride;
    }

    @Override
    public boolean isEmpty() {
        return inSize == 0;
    }

    /**
     * describes the metadata and placement into the buffer based off the stride.
     */
    public static class VertexDefinition {

        public final int location;
        public final BaseVertexAttribute attribute;
        public final int offset;
        public final int features;

        public VertexDefinition(int location, int offset, BaseVertexAttribute attribute, int features) {
            this.location = location;
            this.attribute = attribute;
            this.offset = offset;
            this.features = features;
        }
    }
}
