package org.terasology.entityFactory;

import java.util.Arrays;
import java.util.Collection;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.audio.Sound;
import org.terasology.components.*;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.RadarComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.Component;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import java.util.ArrayList;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;


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
   
       
        ArrayList<Class<? extends org.terasology.entitySystem.Component>> detect =  new ArrayList(Arrays.asList(SimpleAIComponent.class));
        
      //  RadarComponent radar = new RadarComponent(new Vector3d(spawnPosition));
        RadarComponent radar = new RadarComponent();

        player.addComponent(radar);
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
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Companion"), 16)));
        player.send(new ReceiveItemEvent(blockFactory.newInstance(BlockManager.getInstance().getBlockFamily("Torch"), 99)));
        player.send(new ReceiveItemEvent(entityManager.create("core:axe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:pickaxe")));
        player.send(new ReceiveItemEvent(entityManager.create("core:sword")));
        player.send(new ReceiveItemEvent(entityManager.create("core:explodeTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:railgunTool")));
        player.send(new ReceiveItemEvent(entityManager.create("core:miniaturizer")));

        return player;
    }

}
