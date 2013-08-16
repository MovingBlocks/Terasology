package org.terasology.logic.drowning;

import org.terasology.entitySystem.Component;

/**
 * Use to signify an entity is subject to drowning
 * @author ancaplinger
 */
public class DrownsComponent implements Component {

    public float timeBeforeDrownStart = 15.0f;
    public float timeBetweenDrownDamage = 1.0f;
    public int drownDamage = 10;

}
