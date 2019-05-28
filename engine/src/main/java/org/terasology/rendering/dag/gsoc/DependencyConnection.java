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


import org.terasology.rendering.opengl.FBO;

public abstract class DependencyConnection {

    private String connectionName;
    private Type connectionType;

    DependencyConnection(String name, Type type) {
        this.connectionName = name;
        this.connectionType = type;
    }

    public enum Type { /*Might differentiate by name only*/
        INPUT,
        OUTPUT
    }

    public String getName() {
        return this.connectionName;
    }

    public Type getType() {
        return this.connectionType;
    }

    /**
     * TODO String to SimpleUri or make ConnectionUri and change Strings for names to ConnectionUris
     */

}
