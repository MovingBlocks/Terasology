/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;

import java.util.Locale;

/**
 */
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
