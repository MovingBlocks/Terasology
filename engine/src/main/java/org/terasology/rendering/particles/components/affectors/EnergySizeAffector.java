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
package org.terasology.rendering.particles.components.affectors;

import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Linus on 10-3-2015.
 */
public class EnergySizeAffector implements Affector {
    public final TreeMap<Float, Vector3f> gradientMap = new TreeMap<>();

    private final Vector3f min = new Vector3f();
    private final Vector3f max = new Vector3f();

    @Override
    public void onUpdate(ParticleData data, Random random, float delta) {
        Map.Entry<Float, Vector3f> left = gradientMap.floorEntry(data.energy);
        Map.Entry<Float, Vector3f> right = gradientMap.ceilingEntry(data.energy);

        if(left == null && right != null) {
            data.scale.set(right.getValue());
        }
        else if(right == null && left != null) {
            data.scale.set(left.getValue());
        }
        else if (left != null && right != null) {
            min.set(left.getValue());
            max.set(right.getValue());

            float rightAmmount = (data.energy - left.getKey()) / (right.getKey() - left.getKey());

            min.scale(1 - rightAmmount);
            max.scale(rightAmmount);
            min.add(max);
            data.scale.set(min);
        }
    }
}
