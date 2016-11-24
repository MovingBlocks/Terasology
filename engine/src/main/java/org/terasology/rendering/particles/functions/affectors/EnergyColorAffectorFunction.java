/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.particles.functions.affectors;

import com.google.common.collect.Maps;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.affectors.EnergyColorAffectorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by Linus on 11-3-2015.
 */
public final class EnergyColorAffectorFunction extends AffectorFunction<EnergyColorAffectorComponent> {

    public EnergyColorAffectorFunction() {
        super(EnergyColorAffectorComponent.class, DataMask.ENERGY, DataMask.COLOR);
    }

    private NavigableMap<Float,Vector4f> navigableGradientMap = Maps.newTreeMap();

    @Override
    public void update(final EnergyColorAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        Map.Entry<Float, Vector4f> left = navigableGradientMap.floorEntry(particleData.energy);
        Map.Entry<Float, Vector4f> right = navigableGradientMap.ceilingEntry(particleData.energy);

        lerpAndSetColor(left, right, particleData);
    }

    @Override
    public void beforeUpdates(final EnergyColorAffectorComponent component,
                              final Random random,
                              final float delta
    ) {
        navigableGradientMap.clear();
        for(EnergyColorAffectorComponent.EnergyAndColor energyAndColor: component.gradientMap)
        navigableGradientMap.put(energyAndColor.energy, energyAndColor.color);
    }

    private static void lerpAndSetColor(final Map.Entry<Float, Vector4f> left,
                                        final Map.Entry<Float, Vector4f> right,
                                        final ParticleData particleData
    ) {
        if (left == null && right != null) {
            particleData.color.set(right.getValue());
        } else if (right == null && left != null) {
            particleData.color.set(left.getValue());
        } else if (left != null && right != null) {
            float rightAmount = (particleData.energy - left.getKey()) / (right.getKey() - left.getKey());

            final Vector4f leftValue = left.getValue();
            final Vector4f rightValue = right.getValue();

            particleData.color.set(
                    TeraMath.lerp(leftValue.x(), rightValue.x(), rightAmount),
                    TeraMath.lerp(leftValue.y(), rightValue.y(), rightAmount),
                    TeraMath.lerp(leftValue.z(), rightValue.z(), rightAmount),
                    TeraMath.lerp(leftValue.w(), rightValue.w(), rightAmount)
            );
        }
    }
}
