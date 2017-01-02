/*
 * Copyright 2016 MovingBlocks
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

import java.util.List;

import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.layers.mainMenu.settings.CieCamColors;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

public class PlayerConfig {

    private static final float DEFAULT_PLAYER_HEIGHT = 1.8f;  // Default value for player's height

    private static final float DEFAULT_PLAYER_EYE_HEIGHT = 0.7f; // Default value for player's eye height

    private String name = defaultPlayerName(); // Sets player's default name

    private Color color = defaultPlayerColor(); // Sets player's default color

    private Float height = DEFAULT_PLAYER_HEIGHT; // Sets player's default height

    private Float eyeHeight = DEFAULT_PLAYER_EYE_HEIGHT;  // Sets player's default eye height

    private boolean hasEnteredUsername; // Property regarding whether or not player has entered username

    /**
     * Gets the player's name
     *
     * @return   A String representing the player's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the player's name
     *
     * @param name  A String representing the player's desired name
     */    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the player's color
     *
     * @return   A Color object representing the player's color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the player's color
     *
     * @param color  A Color object representing the player's desired color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Gets the player's height
     *
     * @return   A float representing the player's height
     */
    public Float getHeight() {
        return height;
    }
    
    /**
     * Sets the player's height
     *
     * @param height  A float representing the player's desired height
     */
    public void setHeight(Float height) {
        this.height = height;
    }

    /**
     * Gets the player's eye height
     *
     * @return  A float representing the player's eye height
     */
    public Float getEyeHeight() {
        return eyeHeight;
    }
    
    /**
     * Sets the player's eye height
     *
     * @param eyeHeight  A float representing the player's desired eye height
     */
    public void setEyeHeight(Float eyeHeight) {
        if (eyeHeight < this.height) {
            this.eyeHeight = eyeHeight;
        }
    }

    /**
     * Checks to see if the player has entered a username
     *
     * @return  A boolean returning true if the player has entered a username
     */    
    public boolean hasEnteredUsername() {
        return hasEnteredUsername;
    }

    /**
     * Configures the property of whether or not the player has entered a username
     *
     * @param entered  A boolean representing the desired state of the hasEnteredUsername property
     */ 
    public void setHasEnteredUsername(boolean entered) {
        this.hasEnteredUsername = entered;
    }

    /**
     * Returns a String representing the player's default name
     *
     * @return A String representing the player's default name
     */ 
    private static String defaultPlayerName() {
        return "Player" + new FastRandom().nextInt(10000, 99999);
    }
    
    /**
     * Returns a Color object representing the player's default color
     *
     * @return A Color object representing the player's default color
     */
    private Color defaultPlayerColor() {
        Random rng = new FastRandom();
        List<Color> colors = CieCamColors.L65C65;
        return colors.get(rng.nextInt(colors.size()));
    }
}
