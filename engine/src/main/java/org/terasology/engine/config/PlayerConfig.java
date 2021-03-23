// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.config;

import org.terasology.engine.rendering.nui.layers.mainMenu.settings.CieCamColors;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.utilities.subscribables.AbstractSubscribable;
import org.terasology.nui.Color;

import java.util.List;

public class PlayerConfig extends AbstractSubscribable {

    public static final String PLAYER_NAME = "PLAYER_NAME";

    private static final float DEFAULT_PLAYER_HEIGHT = 1.8f;

    private static final float DEFAULT_PLAYER_EYE_HEIGHT = 0.85f;


    private String name = defaultPlayerName();

    private Color color = defaultPlayerColor();

    private Float height = DEFAULT_PLAYER_HEIGHT;

    private Float eyeHeight = DEFAULT_PLAYER_EYE_HEIGHT;

    private boolean hasEnteredUsername;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        propertyChangeSupport.firePropertyChange(PLAYER_NAME, oldName, name);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public Float getEyeHeight() {
        return eyeHeight;
    }

    public void setEyeHeight(Float eyeHeight) {
        if (eyeHeight < this.height) {
            this.eyeHeight = eyeHeight;
        }
    }

    public boolean hasEnteredUsername() {
        return hasEnteredUsername;
    }

    public void setHasEnteredUsername(boolean entered) {
        this.hasEnteredUsername = entered;
    }

    /**
     * Generates the player's default name. The default name is the string "Player" followed by a random 5 digit code ranging from 10000 to 99999.
     *
     * @return a String with the player's default name.
     */
    private static String defaultPlayerName() {
        return "Player" + new FastRandom().nextInt(10000, 99999);
    }

    /**
     * Randomly generates a default color for the player via a random int generator using FastRandom object.
     *
     * @return a Color object with the player's default color.
     */
    private Color defaultPlayerColor() {
        Random rng = new FastRandom();
        List<Color> colors = CieCamColors.L65C65;
        return colors.get(rng.nextInt(colors.size()));
    }
}
