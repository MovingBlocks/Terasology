/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.terasology.logic.characters;

import org.terasology.logic.entities.MovableEntity;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Extends movable entities with character specific properties like health and experience points.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Character extends MovableEntity {

    private int _maxHealthPoints = 255;
    private int _healthPoints = 255;

    public Character(WorldRenderer parent, double walkingSpeed, double runningFactor, double jumpIntensity) {
        super(parent, walkingSpeed, runningFactor, jumpIntensity, false);
    }

    public Character(WorldRenderer parent, double walkingSpeed, double runningFactor, double jumpIntensity, boolean loadAudio) {
        super(parent, walkingSpeed, runningFactor, jumpIntensity, loadAudio);
    }

    /**
     * Damages the player by the given amount of health points.
     *
     * @param damage The amount of damage to deal
     */
    public void damage(int damage) {
        if (damage < 0 || isDead())
            return;

        _healthPoints -= damage;
    }

    /**
     * Heals the player by the given amount of health points.
     *
     * @param health The amount of health points to restore
     */
    public void heal(int health) {
        if (health < 0 || isDead())
            return;

        _healthPoints += health;
    }

    /**
     * Revives a dead player by restoring the health points back to maximum.
     */
    public void revive() {
        if (isDead())
            _healthPoints = _maxHealthPoints;
    }

    /**
     * Returns the maximum health points of this character.
     *
     * @return The maximum health points
     */
    public int getMaxHealthPoints() {
        return _maxHealthPoints;
    }

    /**
     * Sets the maximum amount of health points of this character.
     *
     * @param healthPoints The maximum health points
     */
    public void setMaxHealthPoints(int healthPoints) {
        _maxHealthPoints = healthPoints;
    }

    /**
     * Returns the current amount of health points.
     *
     * @return The health points
     */
    public int getHealthPoints() {
        return _healthPoints;
    }

    /**
     * Returns the percentage of health points relative to the maximum health points
     * of this character.
     *
     * @return The health percentage
     */
    public double getHealthPercentage() {
        return ((double) _healthPoints / (double) _maxHealthPoints);
    }

    /**
     * True if this character is dead.
     *
     * @return True if dead
     */
    public boolean isDead() {
        return _healthPoints <= 0;
    }

    @Override
    protected void handleVerticalCollision() {
        // Damage by falling
        if (_gravity < -0.3)
            damage((int) (((Math.abs(_gravity) - 0.3) / 0.3) * getMaxHealthPoints()));
    }

    @Override
    protected void handleHorizontalCollision() {
        // Nothing special to do.
    }
}