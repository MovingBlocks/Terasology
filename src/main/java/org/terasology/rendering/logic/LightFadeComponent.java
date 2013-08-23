/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.logic;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public final class LightFadeComponent implements Component {

    @Replicate
    public float targetDiffuseIntensity = 1.0f;

    @Replicate
    public float targetAmbientIntensity = 1.0f;

    @Replicate
    public boolean removeLightAfterFadeComplete;

    @Replicate
    public float diffuseFadeRate = 2.0f;

    @Replicate
    public float ambientFadeRate = 2.0f;

}
