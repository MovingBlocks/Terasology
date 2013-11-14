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

package org.terasology.config;

import org.terasology.engine.SimpleUri;

/**
 * @author Immortius
 */
public class WorldGenerationConfig {
    private String worldTitle = "New World";
    private float forestGrassDensity = 0.3f;
    private float plainsGrassDensity = 0.2f;
    private float snowGrassDensity = 0.001f;
    private float mountainGrassDensity = 0.2f;
    private float desertGrassDensity = 0.001f;
    private String defaultSeed = "Terasology";

    private SimpleUri defaultGenerator = new SimpleUri("engine:perlin");

    public String getWorldTitle() {
        return worldTitle;
    }

    public void setWorldTitle(String worldTitle) {
        this.worldTitle = worldTitle;
    }

    public String getDefaultSeed() {
        return defaultSeed;
    }

    public void setDefaultSeed(String defaultSeed) {
        this.defaultSeed = defaultSeed;
    }

    public SimpleUri getDefaultGenerator() {
        return defaultGenerator;
    }

    public void setDefaultGenerator(SimpleUri defaultGenerator) {
        this.defaultGenerator = defaultGenerator;
    }
}
