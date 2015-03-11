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

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.rendering.particles.components.affectors.EnergySizeAffectorComponent;
import org.terasology.rendering.particles.internal.DataMask;
import org.terasology.utilities.random.Random;

import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by Linus on 11-3-2015.
 */
public final class EnergySizeAffectorFunction extends AffectorFunction<EnergySizeAffectorComponent> {

    public EnergySizeAffectorFunction() {
        super(EnergySizeAffectorComponent.class, DataMask.ENERGY, DataMask.SCALE);
    }

    @Override
    public void update(final EnergySizeAffectorComponent component,
                       final ParticleData particleData,
                       final Random random,
                       final float delta
    ) {
        //TODO: make this actually work
        NavigableMap<Float, Vector3f> sizeMap = (NavigableMap<Float, Vector3f>)component.sizeMap;
        Map.Entry<Float, Vector3f> left = sizeMap.floorEntry(particleData.energy);
        Map.Entry<Float, Vector3f> right = sizeMap.ceilingEntry(particleData.energy);

        if(left == null && right != null) {
            particleData.scale.set(right.getValue());
        }
        else if(right == null && left != null) {
            particleData.scale.set(left.getValue());
        }
        else if (left != null && right != null) {
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
