/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.biomesAPI;

import org.terasology.math.geom.Vector2f;
import org.terasology.world.generation.facets.base.FieldFacet2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseConditionalBiome implements ConditionalBiome {
    protected Map<Class<? extends FieldFacet2D>, Vector2f> limitedFacets = new HashMap<>();

    @Override
    public boolean isValid(Class<? extends FieldFacet2D> facetClass, Float value) {
        Vector2f constraints = limitedFacets.get(facetClass);
        return constraints == null || (value >= constraints.x && value <= constraints.y);
    }

    @Override
    public Set<Class<? extends FieldFacet2D>> getLimitedFacets() {
        return limitedFacets.keySet();
    }

    @Override
    public void setLowerLimit(Class<? extends FieldFacet2D> facetClass, Float minimum) {
        limitedFacets.compute(facetClass, (k, v) -> {
            if (v == null) v = new Vector2f(minimum, Float.MAX_VALUE);
            v.x = minimum;
            return v;
        });
    }

    @Override
    public void setUpperLimit(Class<? extends FieldFacet2D> facetClass, Float maximum) {
        limitedFacets.compute(facetClass, (k, v) -> {
            if (v == null) v = new Vector2f(Float.MIN_VALUE, maximum);
            v.y = maximum;
            return v;
        });
    }
}
