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

public class FboConnection extends DependencyConnection {

    // private SimpleUri fboUri;
    private FBO fboData;

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
     * @param fboData
     * @param parentNode
     */
    public FboConnection(String name, Type type, FBO fboData, SimpleUri parentNode) {
        super(name, type, parentNode);
        this.fboData = fboData;
    }

    public void setFboData(FBO fboData) {
        this.fboData = fboData;
    }

    public FBO getFboData() {
        return this.fboData;
    }

    static String getFboName(int number) {
        return new StringBuilder("FBO").append(number).toString();
    }

}
