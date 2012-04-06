package org.terasology.entityFactory;

import org.terasology.components.BlockItemComponent;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.actions.AccessInventoryActionComponent;
import org.terasology.components.actions.PlaySoundActionComponent;
import org.terasology.entitySystem.*;
import org.terasology.logic.manager.AudioManager;
import org.terasology.model.blocks.BlockFamily;
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

    public EntityRef newInstance(BlockFamily blockFamily) {
        return newInstance(blockFamily, 1, null);
    }

    public EntityRef newInstance(BlockFamily blockFamily, EntityRef placedEntity) {
        return newInstance(blockFamily, 1, placedEntity);
    }

    public EntityRef newInstance(BlockFamily blockFamily, int quantity) {
        return newInstance(blockFamily, quantity, null);
    }

    private EntityRef newInstance(BlockFamily blockFamily, int quantity, EntityRef placedEntity) {
        EntityRef entity = entityManager.create();
        if (blockFamily.getArchetypeBlock().getLuminance() > 0) {
            entity.addComponent(new LightComponent());
        }
        ItemComponent item = new ItemComponent();
        item.name = blockFamily.getTitle();
        item.consumedOnUse = true;
        if (blockFamily.getArchetypeBlock().isStackable()) {
            item.stackId = blockFamily.getTitle() + "Block";
            item.stackCount = (byte)quantity;
        }
        item.usage = ItemComponent.UsageType.OnBlock;
        entity.addComponent(item);

        BlockItemComponent blockItem = new BlockItemComponent(blockFamily);

        if (blockFamily.getArchetypeBlock().isEntityRetainedWhenItem()) {
            if (placedEntity != null) {
                blockItem.placedEntity = placedEntity;
            } else {
                Prefab prefab = prefabManager.getPrefab(blockFamily.getArchetypeBlock().getEntityPrefab());
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
    public EntityRef newChest(BlockFamily blockFamily) {
        EntityRef entity = newInstance(blockFamily);
        EntityRef placedEntity = entityManager.create();
        InventoryComponent inventory = new InventoryComponent(16);
        inventory.itemSlots.set(0, newInstance(BlockManager.getInstance().getBlockFamily("Torch"), 3));
        placedEntity.addComponent(inventory);
        placedEntity.addComponent(new AccessInventoryActionComponent());
        placedEntity.addComponent(new PlaySoundActionComponent(AudioManager.sound("Click")));
        BlockItemComponent blockItem = entity.getComponent(BlockItemComponent.class);
        blockItem.placedEntity = placedEntity;

        return entity;
    }
    
}
