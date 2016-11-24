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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.reflection.MappedContainer;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

import java.util.*;

/**
 * Created by Linus on 8-3-2015.
 */
public class EnergyColorAffectorComponent implements Component {

    public List<EnergyAndColor> gradientMap = new ArrayList<>();

    @MappedContainer
    public static class EnergyAndColor {
        public float energy;
        public Vector4f color;

        public EnergyAndColor() {
            energy = 0.0f;
            color = new Vector4f();
        }

        public EnergyAndColor(float energy, Vector4f color) {
            this.energy = energy;
            this.color = new Vector4f(color);
        }
    }

    public EnergyColorAffectorComponent() {

    }

    public EnergyColorAffectorComponent(float[] keys, Vector4f[] values) {
        Preconditions.checkArgument(keys.length == values.length,
            "The number of keys (%s) does not match the number of values (%s).", keys.length, values.length
        );

        for (int i = 0; i < keys.length; i++) {
            gradientMap.add(new EnergyAndColor(keys[i], values[i]));
        }
    }

    public EnergyColorAffectorComponent(Iterable<Float> keys, Iterable<Vector4f> values) {
        final Iterator<Float> keyIterator = keys.iterator();
        final Iterator<Vector4f> valueIterator = values.iterator();


        while(keyIterator.hasNext()) {
            Preconditions.checkArgument(valueIterator.hasNext(),
                    "Received more keys than values."
            );

            gradientMap.add(new EnergyAndColor(keyIterator.next(), valueIterator.next()));
        }

        Preconditions.checkArgument(!valueIterator.hasNext(),
                "Received more values than keys."
        );
    }

}
