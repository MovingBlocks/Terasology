// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.game;

public class Game {

    private String name = "";
    private String seed = "";

    public void load(GameManifest manifest) {
        this.name = manifest.getTitle();
        this.seed = manifest.getSeed();
    }

    public String getName() {
        return name;
    }

    public String getSeed() {
        return seed;
    }
}
