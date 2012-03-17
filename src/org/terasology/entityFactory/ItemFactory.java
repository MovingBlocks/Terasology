package org.terasology.entityFactory;

import org.terasology.components.ItemComponent;
import org.terasology.components.actions.ExplosionActionComponent;
import org.terasology.components.actions.PlaySoundActionComponent;
import org.terasology.components.actions.TunnelActionComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.AudioManager;

import java.util.Arrays;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ItemFactory {
    private EntityManager entityManager;

    public ItemFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef createAxe() {
        EntityRef item = entityManager.create();
        ItemComponent itemComp = new ItemComponent();
        itemComp.name = "Axe";
        itemComp.icon = "Axe";
        itemComp.getPerBlockDamageBonus().put("BirkTrunk", 1);
        itemComp.getPerBlockDamageBonus().put("OakTrunk", 1);
        itemComp.getPerBlockDamageBonus().put("PineTrunk", 1);
        itemComp.renderWithIcon = true;
        item.addComponent(itemComp);
        return item;
    }
    
    public EntityRef createPickAxe() {
        EntityRef item = entityManager.create();
        ItemComponent itemComp = new ItemComponent();
        itemComp.name = "PickAxe";
        itemComp.icon = "PickAxe";
        itemComp.getPerBlockDamageBonus().put("Stone", 1);
        itemComp.renderWithIcon = true;
        item.addComponent(itemComp);
        return item;
    }
    
    public EntityRef createDynamite() {
        EntityRef item = entityManager.create();
        ItemComponent itemComp = new ItemComponent();
        itemComp.name = "Dynamite";
        itemComp.icon = "Dynamite";
        itemComp.usage = ItemComponent.UsageType.OnBlock;
        item.addComponent(itemComp);
        item.addComponent(new ExplosionActionComponent());
        item.addComponent(new PlaySoundActionComponent(AudioManager.sounds("Explode1", "Explode2", "Explode3", "Explode4", "Explode5")));
        return item;
    }
    
    public EntityRef createRailgun() {
        EntityRef item = entityManager.create();
        ItemComponent itemComp = new ItemComponent();
        itemComp.name = "Railgun";
        itemComp.icon = "Railgun";
        itemComp.usage = ItemComponent.UsageType.InDirection;
        item.addComponent(itemComp);
        item.addComponent(new PlaySoundActionComponent(AudioManager.sounds("Explode1", "Explode2", "Explode3", "Explode4", "Explode5")));
        item.addComponent(new TunnelActionComponent());
        return item;
    }
}
