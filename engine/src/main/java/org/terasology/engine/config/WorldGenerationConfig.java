// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
