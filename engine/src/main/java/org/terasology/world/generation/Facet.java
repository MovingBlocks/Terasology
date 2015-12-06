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
