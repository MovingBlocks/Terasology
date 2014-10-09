package org.terasology.logic.characters.interactions;

import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
/**
 * Entities with this component will show an UI during interactions.
 *
 * Iteractions can be started and stopped by calling {@link InteractionUtil#setInteractionTarget(EntityRef, EntityRef)}
 *
 * @author Florian <florian@fkoeberle.de>
 */
public class InteractionScreenComponent implements Component {
    public String screen;

}
