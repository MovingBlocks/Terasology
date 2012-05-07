package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;

/**
 *
 */
public class PotionComponent extends AbstractComponent {
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
