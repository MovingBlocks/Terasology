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
package org.terasology.rendering.gui.framework.style;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public interface Style {

    /**
     * Get the layer on which the style should be drawn. Higher number styles will be drawn on top of lower number styles.
     *
     * @return Returns the layer.
     */
    public int getLayer();

}
