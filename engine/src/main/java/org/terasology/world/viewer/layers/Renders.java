// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.viewer.layers;

import org.terasology.world.generation.WorldFacet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to in {@link FacetLayer} implementations
 * to indicate the target facet that is rendered.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Renders {

    /**
     * @return the target facet class
     */
    Class<? extends WorldFacet> value();

    int order();
}
