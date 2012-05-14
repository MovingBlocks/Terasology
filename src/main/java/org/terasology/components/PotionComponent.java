package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 *
 */
public class PotionComponent implements Component {
    public enum PotionType {      //Actual potions
        Empty,
        Red,
        Orange,
        Green,
        Purple,
        Cerulean,
        Blue,
        Black
    }

    public PotionType type;
}
