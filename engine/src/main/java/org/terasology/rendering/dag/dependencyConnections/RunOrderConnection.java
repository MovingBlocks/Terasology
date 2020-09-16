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
package org.terasology.rendering.dag.dependencyConnections;

import org.terasology.engine.SimpleUri;

// TODO examine if we really need this connection type

/**
 *
 */
public class RunOrderConnection extends DependencyConnection {

    public RunOrderConnection(String name, Type type, SimpleUri parentNode) {
        super(name, type, parentNode);
    }

    public String toString() {
        return super.toString();
    }

    public static String getConnectionName(int id, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":RunOrder").append(id).toString();
    }
}
