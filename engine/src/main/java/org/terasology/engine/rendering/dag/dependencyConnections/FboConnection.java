// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.dag.dependencyConnections;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.opengl.FBO;

public class FboConnection extends DependencyConnection<FBO> {

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

    public static String getConnectionName(int number, SimpleUri nodeUri) {
        return new StringBuilder(nodeUri.toString()).append(":FBO").append(number).toString();
    }

}
