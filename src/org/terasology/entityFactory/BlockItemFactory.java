package org.terasology.entityFactory;

import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.BlockItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.blocks.BlockGroup;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockItemFactory {
    private EntityManager entityManager;

    public BlockItemFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityRef newInstance(BlockGroup blockGroup) {
        return newInstance(blockGroup, 1);
    }

    public EntityRef newInstance(BlockGroup blockGroup, int quantity) {
        EntityRef entity = entityManager.create();
        if (blockGroup.getArchetypeBlock().getLuminance() > 0) {
            entity.addComponent(new LightComponent());
        }
        ItemComponent item = new ItemComponent();
        item.name = blockGroup.getTitle();
        item.consumedOnUse = true;
        item.stackId = blockGroup.getTitle() + "Block";
        item.stackCount = (byte)quantity;
        item.usage = ItemComponent.UsageType.OnBlock;
        entity.addComponent(item);
        
        entity.addComponent(new BlockItemComponent(blockGroup));
        
        return entity;
    }
    
}
