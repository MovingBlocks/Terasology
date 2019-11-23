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

import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.world.generation.facets.base.FieldFacet2D;

import java.util.Map;
import java.util.Set;

public abstract class BaseConditionalBiome implements ConditionalBiome {
    protected Map<Class<FieldFacet2D>, NumberRangeConstraint<Float>> limitedFacets;

    @Override
    public Set<Class<FieldFacet2D>> getLimitedFacets() {
        return limitedFacets.keySet();
    }

    @Override
    public boolean isValid(Class<FieldFacet2D> facetClass, Float value) {
        NumberRangeConstraint<Float> constraints = limitedFacets.get(facetClass);
        return constraints == null || constraints.isSatisfiedBy(value);
    }

    @Override
    public void setLowerLimit(Class<FieldFacet2D> facetClass, Float minimum) {
        limitedFacets.compute(facetClass, (k, v) -> {
            v = new NumberRangeConstraint<Float>(minimum, v != null ? v.getMax() : 1f, true, true);
            return v;
        });
    }

    @Override
    public void setUpperLimit(Class<FieldFacet2D> facetClass, Float maximum) {
        limitedFacets.compute(facetClass, (k, v) -> {
            v = new NumberRangeConstraint<Float>(v != null ? v.getMin() : 0f, maximum,true, true);
            return v;
        });
    }
}
