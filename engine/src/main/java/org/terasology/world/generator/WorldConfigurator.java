/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.generator;

import java.util.Map;

import org.terasology.entitySystem.Component;

/**
 * Allows for configuration of world generators.
 */
public interface WorldConfigurator  {

    /**
     * The values are supposed to be annotated with {@link org.terasology.rendering.nui.properties.Property}
     * @return a map (label to object)
     */
    Map<String, Component> getProperties();

    /**
     * @param key the name of the configuration
     * @param comp the configuration component
     */
    void setProperty(String key, Component comp);
}
