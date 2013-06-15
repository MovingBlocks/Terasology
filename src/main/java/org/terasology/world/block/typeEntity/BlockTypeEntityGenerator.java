package org.terasology.world.block.typeEntity;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockRegistrationListener;

/**
 * @author Immortius
 */
public class BlockTypeEntityGenerator implements BlockRegistrationListener {

    private EntityManager entityManager;
    private BlockManager blockManager;
    private Prefab blockTypePrefab;

    public BlockTypeEntityGenerator(EntityManager entityManager, BlockManager blockManager) {
        this.entityManager = entityManager;
        this.blockManager = blockManager;
        blockTypePrefab = entityManager.getPrefabManager().getPrefab("engine:blockType");

        connectExistingEntities();
        generateForExistingBlocks();
    }

    private void connectExistingEntities() {
        for (EntityRef entity : entityManager.getEntitiesWith(BlockTypeComponent.class)) {
            BlockTypeComponent blockTypeComp = entity.getComponent(BlockTypeComponent.class);
            blockTypeComp.block.setEntity(entity);
        }
    }

    private void generateForExistingBlocks() {
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            for (Block block : blockFamily.getBlocks()) {
                if (!block.getEntity().exists()) {
                    generateBlockTypeEntity(block);
                }
            }
        }
    }

    @Override
    public void onBlockFamilyRegistered(BlockFamily family) {
        for (Block block : family.getBlocks()) {
            generateBlockTypeEntity(block);
        }
    }

    private void generateBlockTypeEntity(Block block) {
        EntityBuilder builder = entityManager.newBuilder(blockTypePrefab);
        builder.getComponent(BlockTypeComponent.class).block = block;
        // TODO: Copy across settings as necessary
        Prefab prefab = entityManager.getPrefabManager().getPrefab(block.getPrefab());
        if (prefab != null) {
            for (Component comp : prefab.iterateComponents()) {
                if (!(comp instanceof NetworkComponent)) {
                    ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                    builder.addComponent(metadata.clone(comp));
                }
            }
        }
        block.setEntity(builder.build());
    }
}
