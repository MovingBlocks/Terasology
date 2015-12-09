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

package org.terasology.world.viewer.layers;

import java.awt.image.BufferedImage;

import org.terasology.engine.Observer;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Region;

/**
 * A visual representation of a facet class
 */
public interface FacetLayer  {

    /**
     * The color for missing/wrong values.
     */
    Color MISSING = Color.MAGENTA;

    /**
     * Renders the content of a facet to an image
     * @param img the image to render on
     * @param region the region that provides the data
     */
    void render(BufferedImage img, Region region);

    /**
     * Returns a descriptive text for a specific target location
     * @param region the region of interest
     * @param wx the world x coordinate
     * @param wy the world y coordinate
     * @return a descriptive text or <code>null</code>
     */
    String getWorldText(Region region, int wx, int wy);

    /**
     * @return a config or <code>null</code>
     */
    FacetLayerConfig getConfig();

    /**
     * @return true if visible
     */
    boolean isVisible();

    /**
     * Note that changing visibility will notify all observers
     * @param yesno true if visible
     */
    void setVisible(boolean yesno);

    /**
     * @param obs the observer to add
     */
    void addObserver(Observer<FacetLayer> obs);

    /**
     * @param obs the observer to remove
     */
    void removeObserver(Observer<FacetLayer> obs);

    /**
     * Fires out a notification event
     */
    void notifyObservers();
}
