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
package org.terasology.rendering.particles.functions;

import org.terasology.rendering.particles.DataMask;

/**
 * Created by Linus on 11-3-2015.
 */
public abstract class ParticleSystemFunction<T> {
    private final int rawDataMask;
    private final Class<T> component;

    public ParticleSystemFunction(Class<T> component, DataMask dataMask, DataMask... dataMasks) {
        this.rawDataMask = DataMask.toInt(dataMask, dataMasks);
        this.component = component;
    }

    @Override
    public final int hashCode() {
        return component.hashCode();
    }

    @Override
    public final boolean equals(final Object object) {
        if(object != null && object.getClass().equals(this.getClass())) {
            ParticleSystemFunction other = (ParticleSystemFunction)object;
            return other.getComponentClass().equals(this.getComponentClass());
        }

        return false;
    }

    public final Class<T> getComponentClass() {
        return component;
    }

    public final int getDataMask() {
        return rawDataMask;
    }
}
