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
package org.terasology.math;

/**
 * @author DizzyDragon
 * Interface for objects on which linear interpolation can be applied.
 *
 * @param <T> Reference to the implementing class itself
 */
public interface Lerpable<T> {

    /**
     * Linear interpolation between the instance on which it is called
     * and a other instance of the same class.
     *
     * When providing an implementation, remember that any instance of T
     * can be cast to the implementing class and vice versa.
     *
     * @param other second instance (of same class) to interpolate with
     * @param point point of interpolation
     * @param <T> Reference to the implementing class itself
     * @return interpolation result
     */
    public <T extends Lerpable <? super T>> T lerp(T other, double point);
}
