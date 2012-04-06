package org.terasology.entityFactory;

import org.terasology.components.BlockItemComponent;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.components.actions.PlaySoundActionComponent;
import org.terasology.entitySystem.*;
import org.terasology.logic.manager.AudioManager;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockItemFactory {
    private EntityManager entityManager;
    private PrefabManager prefabManager;

    public BlockItemFactory(EntityManager entityManager, PrefabManager prefabManager) {
        this.entityManager = entityManager;
        this.prefabManager = prefabManager;
    }

    public EntityRef newInstance(BlockGroup blockGroup) {
        return newInstance(blockGroup, 1, null);
    }

    public EntityRef newInstance(BlockGroup blockGroup, EntityRef placedEntity) {
        return newInstance(blockGroup, 1, placedEntity);
    }

    public EntityRef newInstance(BlockGroup blockGroup, int quantity) {
        return newInstance(blockGroup, quantity, null);
    }

    private EntityRef newInstance(BlockGroup blockGroup, int quantity, EntityRef placedEntity) {
        EntityRef entity = entityManager.create();
        if (blockGroup.getArchetypeBlock().getLuminance() > 0) {
            entity.addComponent(new LightComponent());
        }
        ItemComponent item = new ItemComponent();
        item.name = blockGroup.getTitle();
        item.consumedOnUse = true;
        if (blockGroup.getArchetypeBlock().isStackable()) {
            item.stackId = blockGroup.getTitle() + "Block";
            item.stackCount = (byte)quantity;
        }
        item.usage = ItemComponent.UsageType.OnBlock;
        entity.addComponent(item);

        BlockItemComponent blockItem = new BlockItemComponent(blockGroup);

        if (blockGroup.getArchetypeBlock().isEntityRetainedWhenItem()) {
            if (placedEntity != null) {
                blockItem.placedEntity = placedEntity;
            } else {
                Prefab prefab = prefabManager.getPrefab(blockGroup.getArchetypeBlock().getEntityPrefab());
                if (prefab != null) {
                    placedEntity = entityManager.create();
                    for (Component component : prefab.listComponents()) {
                        placedEntity.addComponent(entityManager.copyComponent(component));
                    }
                    blockItem.placedEntity = placedEntity;
                }
            }
        }

        entity.addComponent(blockItem);
        
        return entity;
    }

    // TODO: Link blocks to prefabs for this
    public EntityRef newChest(BlockGroup blockGroup) {
        EntityRef entity = newInstance(blockGroup);
        EntityRef placedEntity = entityManager.create();
        InventoryComponent inventory = new InventoryComponent(16);
        inventory.itemSlots.set(0, newInstance(BlockManager.getInstance().getBlockGroup("Torch"), 3));
        placedEntity.addComponent(inventory);
        placedEntity.addComponent(new AccessInventoryActionComponent());
        placedEntity.addComponent(new PlaySoundActionComponent(AudioManager.sound("Click")));
        BlockItemComponent blockItem = entity.getComponent(BlockItemComponent.class);
        blockItem.placedEntity = placedEntity;

        return entity;
    }
    
}
