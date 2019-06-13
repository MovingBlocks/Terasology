/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.FBO;

public class FboConnection extends DependencyConnection<FBO> {
    // private SimpleUri fboUri;
    //private FBO data;

    /**
     *
     * @param name
     * @param type
     * @param parentNode
     */
    public FboConnection(String name, Type type, SimpleUri parentNode) {
        super(name, type, parentNode);
    }

    /**
     *
     * @param name
     * @param type
     * @param data
     * @param parentNode
     */
    public FboConnection(String name, Type type, FBO data, SimpleUri parentNode) {
        super(name, type, parentNode);
        super.setData(data);
    }

    /*
    /**
     * Set FBO for this connection
     * @param data FBO
     *//*
    protected void setData(FBO data) {
        this.data = data;
    }

    /**
     * Get FBO for this connection
     * @return FBO
     *//*
    public FBO getData() {
        return this.data;
    }*/

    public static String getConnectionName(int number) {
        return new StringBuilder("FBO").append(number).toString();
    }

}
