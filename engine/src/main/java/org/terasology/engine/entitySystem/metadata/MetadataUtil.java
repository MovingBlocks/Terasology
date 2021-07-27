// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.metadata;

import org.terasology.engine.entitySystem.Component;

import java.util.Locale;

public final class MetadataUtil {

    private MetadataUtil() {
    }

    public static String getComponentClassName(Class<? extends Component> componentClass) {
        String name = componentClass.getSimpleName();
        Class<?> outer = componentClass.getEnclosingClass();
        if (outer != null) {
            name = outer.getSimpleName() + name;
        }

        int index = name.toLowerCase(Locale.ENGLISH).lastIndexOf("component");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
