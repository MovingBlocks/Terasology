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

package org.terasology.rendering.dag.gsoc;

import javafx.util.Pair;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.FBO;

public class BufferPairConnection extends DependencyConnection<Pair<FBO,FBO>> {

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
    public BufferPairConnection(String  name, Type type, Pair<FBO,FBO> data, SimpleUri parentNode) {
        super(name, type, parentNode);
        super.setData(data);
    }

    /**
     * Return a new instance of BufferPairConnection based on this one with swapped FBOs.
     * @param id TODO Uri
     * @param type
     * @param parentNode
     * @return
     */
    public BufferPairConnection copySwapped(int id, Type type, SimpleUri parentNode) {
        Pair<FBO,FBO> bufferPairToCopy = super.getData();
        Pair<FBO,FBO> newBufferPair =  new Pair<>(bufferPairToCopy.getValue(), bufferPairToCopy.getKey());
        return new BufferPairConnection(BufferPairConnection.getConnectionName(id, parentNode), type, newBufferPair, parentNode);
    }

    public FBO getPrimaryFbo() {
        return super.getData().getKey();
    }

    public FBO getSecondaryFbo() {
        return super.getData().getValue();
    }

    public String toString() {
        return super.toString();
    }

    public static String getConnectionName(int number, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":BufferPair").append(number).toString();
    }

}
