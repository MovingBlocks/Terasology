/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.config;

import org.terasology.engine.core.SimpleUri;

public class WorldGenerationConfig {
    private String worldTitle;
    private String defaultSeed;

    /**
     * A SimpleUri object with a blankly initalized constructor. Used as the default generator.
     */
    private SimpleUri defaultGenerator = new SimpleUri();

    /**
     * @return Returns the title of the world
     */
    public String getWorldTitle() {
        return worldTitle;
    }

    /**
     * @param worldTitle A String object that changes worldTitle
     */
    public void setWorldTitle(String worldTitle) {
        this.worldTitle = worldTitle;
    }

    /**
     * @return Returns the default seed
     */
    public String getDefaultSeed() {
        return defaultSeed;
    }

    /**
     * @param defaultSeed Sets the default seed
     */
    public void setDefaultSeed(String defaultSeed) {
        this.defaultSeed = defaultSeed;
    }

    /**
     * @return Returns the default generator
     */
    public SimpleUri getDefaultGenerator() {
        return defaultGenerator;
    }

    /**
     * @param defaultGenerator Sets the default generator
     */
    public void setDefaultGenerator(SimpleUri defaultGenerator) {
        this.defaultGenerator = defaultGenerator;
    }
}
