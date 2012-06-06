package org.terasology.entityFactory;

import org.terasology.components.BlockItemComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LightComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.model.blocks.BlockFamily;

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
        item.usage = ItemComponent.UsageType.ON_BLOCK;
        entity.addComponent(item);

        BlockItemComponent blockItem = new BlockItemComponent(blockFamily);

        if (!blockFamily.getArchetypeBlock().isEntityTemporary()) {
            if (placedEntity == null || !placedEntity.exists()) {
                placedEntity = entityManager.create(blockFamily.getArchetypeBlock().getEntityPrefab());
            }
            blockItem.placedEntity = placedEntity;
        }

        entity.addComponent(blockItem);

        return entity;
    }

}
