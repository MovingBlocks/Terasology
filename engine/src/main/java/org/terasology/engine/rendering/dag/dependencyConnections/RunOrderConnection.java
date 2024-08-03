// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.dependencyConnections;

import org.terasology.engine.core.SimpleUri;

// TODO examine if we really need this connection type


public class RunOrderConnection extends DependencyConnection {

    public RunOrderConnection(String name, Type type, SimpleUri parentNode) {
        super(name, type, parentNode);
    }

    public static String getConnectionName(int id, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":RunOrder").append(id).toString();
    }
}
