/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.ingame.metrics;

import org.terasology.engine.Time;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * A wrapping {@link Binding} that calls {@link #get()} and {@link #set(T)}
 * only at given timer intervals and returns a cached value otherwise.
 * @author Martin Steiger
 */
public class TimedBinding<T> implements Binding<T> {

    private final Time time;
    private final float updateFreq;
    private final Binding<T> base;

    private T lastGetResult;

    private float lastGetTime = Float.NEGATIVE_INFINITY;
    private float lastSetTime = Float.NEGATIVE_INFINITY;

    /**
     * @param updateFreq update freqency in seconds
     * @param base the underlying base binding that is queried
     */
    public TimedBinding(float updateFreq, Binding<T> base) {
        this.updateFreq = updateFreq;
        this.time = CoreRegistry.get(Time.class);
        this.base = base;
    }

    @Override
    public T get() {
        float now = time.getRealTime();
        if (now - lastGetTime > updateFreq) {
            lastGetResult = base.get();
            lastGetTime = now;
        }

        return lastGetResult;
    }

    @Override
    public void set(T value) {
        float now = time.getRealTime();
        if (now - lastSetTime > updateFreq) {
            base.set(value);
            lastSetTime = now;
        }
    }
}
