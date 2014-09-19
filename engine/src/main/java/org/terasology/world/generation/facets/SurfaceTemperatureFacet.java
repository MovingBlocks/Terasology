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
package org.terasology.world.generation.facets;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetName;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;
import org.terasology.world.generation.facets.base.ColorSummaryFacet;

/**
 * @author Immortius
 */
@FacetName("Temperature")
public class SurfaceTemperatureFacet extends BaseFieldFacet2D implements ColorSummaryFacet {
    static int maxSamplesPerRegion = 5;

    public SurfaceTemperatureFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    @Override
    public Color getColor() {
        float[] values = getInternal();
        float total = 0;
        int sampleRate = Math.max(1, values.length / maxSamplesPerRegion);
        for (int i = 0; i < values.length; i++) {
            if (i % sampleRate == 0) {
                total += values[i];
            }
        }
        float average = total / (values.length / sampleRate);
        return new Color(TeraMath.clamp(average, 0, 255), TeraMath.clamp(average * 0.2f, 0, 255), TeraMath.clamp(average * 0.2f));
    }
}
