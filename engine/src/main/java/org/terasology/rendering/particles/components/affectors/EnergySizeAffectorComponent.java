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
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.reflection.MappedContainer;
import org.terasology.rendering.particles.ParticleData;
import org.terasology.utilities.random.Random;

import java.util.*;

/**
 * Created by Linus on 10-3-2015.
 */
public class EnergySizeAffectorComponent implements Component {

    public List<EnergyAndSize> sizeMap = new ArrayList<>();

    @MappedContainer
    public static class EnergyAndSize {
        public float energy;
        public Vector3f size;

        public EnergyAndSize() {
            energy = 0.0f;
            size = new Vector3f();
        }

        public EnergyAndSize(float energy, Vector3f size) {
            this.energy = energy;
            this.size = new Vector3f(size);
        }
    }

    public EnergySizeAffectorComponent() {

    }

    public EnergySizeAffectorComponent(float[] keys, Vector3f[] values) {
        Preconditions.checkArgument(keys.length == values.length);

        for (int i = 0; i < keys.length; i++) {
            sizeMap.add(new EnergyAndSize(keys[i], values[i]));
        }
    }

    public EnergySizeAffectorComponent(Iterable<Float> keys, Iterable<Vector3f> values) {
        final Iterator<Float> keyIterator = keys.iterator();
        final Iterator<Vector3f> valueIterator = values.iterator();


        while(keyIterator.hasNext()) {
            Preconditions.checkArgument(valueIterator.hasNext(),
                    "Received more keys than values"
            );

            sizeMap.add(new EnergyAndSize(keyIterator.next(), valueIterator.next()));
        }

        Preconditions.checkArgument(!valueIterator.hasNext(),
                "Received more values than keys"
        );
    }
}
