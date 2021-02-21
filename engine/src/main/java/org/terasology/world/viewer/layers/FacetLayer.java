// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.viewer.layers;

import org.terasology.engine.Observer;
import org.terasology.nui.Color;
import org.terasology.world.generation.Region;

import java.awt.image.BufferedImage;

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
