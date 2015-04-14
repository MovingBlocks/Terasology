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

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;

/**
 * Created by Linus on 13-4-2015.
 */
public class PointForceAffectorComponent implements Component {
    public Vector3f position;
    public float magnitude = 1.0f;
    public float radius = 1.0f;

    public PointForceAffectorComponent() {
        this.position = new Vector3f();
        this.magnitude = 1.0f;
        this.radius = 1.0f;
    }

    public PointForceAffectorComponent(Vector3f position, float magnitude, float radius) {
        this.position = new Vector3f(position);
        this.magnitude = magnitude;
        this.radius = Math.max(Math.abs(radius), 0.1e-6f);
    }
}
