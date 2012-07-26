package org.terasology.entityFactory;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.components.*;
import org.terasology.components.world.BlockItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlayerFactory {

    private EntityManager entityManager;
    private BlockItemFactory blockFactory;

    public PlayerFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
        blockFactory = new BlockItemFactory(entityManager, CoreRegistry.get(PrefabManager.class));
    }

    public EntityRef newInstance(Vector3f spawnPosition) {
        EntityRef player = entityManager.create();

        player.addComponent(new LocationComponent(spawnPosition));
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.spawnPosition.set(spawnPosition);
        player.addComponent(playerComponent);
        HealthComponent healthComponent = new HealthComponent(255, 5, 2f);
        healthComponent.excessSpeedDamageMultiplier = 20f;
        healthComponent.fallingDamageSpeedThreshold = 15f;
        player.addComponent(healthComponent);

        AABBCollisionComponent collision = player.addComponent(new AABBCollisionComponent());
        collision.setExtents(new Vector3f(.3f, 0.8f, .3f));

        CharacterMovementComponent movementComp = player.addComponent(new CharacterMovementComponent());
        movementComp.groundFriction = 16f;
        movementComp.maxGroundSpeed = 3f;
        movementComp.distanceBetweenFootsteps = 1.5f;
        CharacterSoundComponent sounds = player.addComponent(new CharacterSoundComponent());
        sounds.footstepSounds.add(AssetManager.load(new AssetUri(AssetType.SOUND, "engine:FootGrass1"), Sound.class));
        sounds.footstepSounds.add(AssetManager.load(new AssetUri(AssetType.SOUND, "engine:FootGrass2"), Sound.class));
        sounds.footstepSounds.add(AssetManager.load(new AssetUri(AssetType.SOUND, "engine:FootGrass3"), Sound.class));
        sounds.footstepSounds.add(AssetManager.load(new AssetUri(AssetType.SOUND, "engine:FootGrass4"), Sound.class));
        sounds.footstepSounds.add(AssetManager.load(new AssetUri(AssetType.SOUND, "engine:FootGrass5"), Sound.class));
        player.addComponent(new LocalPlayerComponent());
        player.addComponent(new InventoryComponent(36));
        player.addComponent(new MinionBarComponent(9));
        player.addComponent(new MinionControllerComponent());

        // Goodie chest
        EntityRef chest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Chest"));
        BlockItemComponent blockItem = chest.getComponent(BlockItemComponent.class);
        EntityRef chestContents = blockItem.placedEntity;

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Companion"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("BrickStair"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Tnt"), 42)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Bookcase"), 1)));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:purplepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:greenpotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:orangepotion")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redpotion")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:book")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:bluebook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:redbook")));
        chestContents.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));

        chestContents.send(new ReceiveItemEvent(entityManager.create("core:mrbarsack")));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Cobaltite"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("NativeGoldOre"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Microcline"), 99)));

        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Brick"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Ice"), 99)));
        chestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Plank"), 99)));

        // Inner goodie chest
        EntityRef innerChest = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Chest"));
        BlockItemComponent innerBlockItem = innerChest.getComponent(BlockItemComponent.class);
        EntityRef innerChestContents = innerBlockItem.placedEntity;

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Alabaster"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Basalt"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Gabbro"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Hornblende"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("OrangeSandStone"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Phyllite"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Schist"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Cinnabar"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Lava"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Water"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Rutile"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Kaolinite"), 42)));

        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Iris"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Dandelion"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Tulip"), 42)));
        innerChestContents.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("YellowFlower"), 42)));

        // Place inner chest into outer chest
        chestContents.send(new ReceiveItemEvent(innerChest));

        player.send(new ReceiveItemEvent(entityManager.create("core:pickaxe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:axe")));
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Torch"), 99)));
        player.send(new ReceiveItemEvent(entityManager.create("core:explodeTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:miniaturizer")));
        player.send(new ReceiveItemEvent(chest));

        return player;
    }

}
