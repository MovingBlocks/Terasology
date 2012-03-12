package org.terasology.entityFactory;

import org.terasology.components.ItemComponent;
import org.terasology.components.PlaceableBlockComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.blocks.BlockGroup;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlaceableBlockFactory {
    private EntityManager entityManager;

    public PlaceableBlockFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(BlockGroup blockGroup) {
        return newInstance(blockGroup, 1);
    }

    public EntityRef newInstance(BlockGroup blockGroup, int quantity) {
        EntityRef entity = entityManager.create();
        ItemComponent item = new ItemComponent();
        item.name = blockGroup.getTitle();
        item.consumedOnUse = true;
        item.stackId = blockGroup.getTitle() + "Block";
        item.stackCount = quantity;
        item.usage = ItemComponent.UsageType.OnBlock;
        entity.addComponent(item);
        
        entity.addComponent(new PlaceableBlockComponent(blockGroup));
        
        return entity;
    }
    
}
