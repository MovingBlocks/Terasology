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
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.affectors.EnergyScaleAffectorComponent;
import org.terasology.rendering.particles.DataMask;
import org.terasology.utilities.random.Random;

import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by Linus on 11-3-2015.
 */
public final class EnergyScaleAffectorFunction extends AffectorFunction<EnergyScaleAffectorComponent> {

    public EnergyScaleAffectorFunction() {
        super(EnergyScaleAffectorComponent.class, DataMask.ENERGY, DataMask.SCALE);
    }

    private NavigableMap<Float,Vector3f> scaleMap = Maps.newTreeMap();

    @Override
    public void update(final EnergyScaleAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        Map.Entry<Float, Vector3f> left = scaleMap.floorEntry(particleData.energy);
        Map.Entry<Float, Vector3f> right = scaleMap.ceilingEntry(particleData.energy);

        if (left == null && right != null) {
            particleData.scale.set(right.getValue());
        } else if (right == null && left != null) {
            particleData.scale.set(left.getValue());
        } else if (left != null && right != null) {
            float rightAmount = (particleData.energy - left.getKey()) / (right.getKey() - left.getKey());

            final Vector3f leftValue = left.getValue();
            final Vector3f rightValue = right.getValue();

            particleData.scale.set(
                    TeraMath.lerp(leftValue.x(), rightValue.x(), rightAmount),
                    TeraMath.lerp(leftValue.y(), rightValue.y(), rightAmount),
                    TeraMath.lerp(leftValue.z(), rightValue.z(), rightAmount)
            );
        }
    }

    @Override
    public void beforeUpdates(final EnergyScaleAffectorComponent component,
                              final Random random,
                              final float delta
    ) {
        scaleMap.clear();

        for(EnergyScaleAffectorComponent.EnergyAndScale energyAndScale : component.sizeMap) {
            scaleMap.put(energyAndScale.energy, energyAndScale.scale);
        }
    }

    private static void lerpAndSetSize(final Map.Entry<Float, Vector3f> left,
                                        final Map.Entry<Float, Vector3f> right,
                                        final ParticleData particleData
    ) {
        if (left == null && right != null) {
            particleData.scale.set(right.getValue());
        } else if (right == null && left != null) {
            particleData.scale.set(left.getValue());
        } else if (left != null && right != null) {
            float rightAmount = (particleData.energy - left.getKey()) / (right.getKey() - left.getKey());

            final Vector3f leftValue = left.getValue();
            final Vector3f rightValue = right.getValue();

            particleData.scale.set(
                    TeraMath.lerp(leftValue.x(), rightValue.x(), rightAmount),
                    TeraMath.lerp(leftValue.y(), rightValue.y(), rightAmount),
                    TeraMath.lerp(leftValue.z(), rightValue.z(), rightAmount)
            );
        }
    }
}
