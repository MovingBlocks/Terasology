/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.dag.dependencyConnections;

import org.terasology.engine.SimpleUri;

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

    public String toString() {
        return super.toString();
    }

    public static String getConnectionName(int number, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":BufferPair").append(number).toString();
    }

}
