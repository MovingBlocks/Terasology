/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.Locale;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Event;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class MetadataUtil {

    private MetadataUtil() {
    }

    public static String getComponentClassName(Component component) {
        return getComponentClassName(component.getClass());
    }

    public static String getComponentClassName(Class<? extends Component> componentClass) {
        String name = componentClass.getSimpleName();
        int index = name.toLowerCase(Locale.ENGLISH).lastIndexOf("component");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
