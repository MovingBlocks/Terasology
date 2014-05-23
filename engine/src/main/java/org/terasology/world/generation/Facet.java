/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.generation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is the sub-annotation for describing facets in Requires and Updates annotations
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Facet {
    /**
     * @return The class of the facet
     */
    Class<? extends WorldFacet> value();

    /**
     * This allows a facet provider to be ordered after the completion of a facet - this should be used where a provider
     * uses one or more facets to produce a derivative facet.
     * @return Whether the facet should be complete before this system is called (for @Requires)
     */
    boolean complete() default true;

    /**
     * @return The desired minimum border around the main facet data
     */
    FacetBorder border() default @FacetBorder;
}
