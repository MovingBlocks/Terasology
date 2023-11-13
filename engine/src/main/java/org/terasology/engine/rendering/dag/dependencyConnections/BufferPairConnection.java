// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.dag.dependencyConnections;

import org.terasology.engine.core.SimpleUri;

public class BufferPairConnection extends DependencyConnection<BufferPair> {

    /**
     *
     * @param name
     * @param type
     * @param parentNode
     */
    public BufferPairConnection(String name, Type type, SimpleUri parentNode) {
        super(name, type, parentNode);
    }

    /**
     * TODO unify naming/iding with other dep. conns
     * @param name
     * @param type
     * @param data
     * @param parentNode
     */
    public BufferPairConnection(String  name, Type type, BufferPair data, SimpleUri parentNode) {
        super(name, type, parentNode);
        super.setData(data);
    }

    /**
     * Return a new instance of BufferPairConnection based on this one with swapped FBOs.
     * @param type
     * @param parentNode
     * @return
     */
    public BufferPairConnection getSwappedCopy(Type type, SimpleUri parentNode) {
        BufferPair bufferPairToCopy = super.getData();
        BufferPair newBufferPair =  new BufferPair(bufferPairToCopy.getSecondaryFbo(), bufferPairToCopy.getPrimaryFbo());
        return new BufferPairConnection(this.getName(), type, newBufferPair, parentNode);
    }

    public BufferPair getBufferPair() {
        return super.getData();
    }

    public static String getConnectionName(int number, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":BufferPair").append(number).toString();
    }

}
