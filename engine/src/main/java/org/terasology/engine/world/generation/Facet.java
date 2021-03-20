// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is the sub-annotation for describing facets in Requires and Updates annotations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Facet {
    /**
     * @return The class of the facet
     */
    Class<? extends WorldFacet> value();

    /**
     * @return The desired minimum border around the main facet data
     */
    FacetBorder border() default @FacetBorder;
}
