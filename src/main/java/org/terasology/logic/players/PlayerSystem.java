package org.terasology.logic.players;

import com.google.common.collect.Lists;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.PlayerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.math.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

import javax.vecmath.Vector3f;
import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private NetworkSystem networkSystem;

    @In
    private InventoryManager inventoryManager;

    private ChunkProvider chunkProvider;

    private List<SpawnCachingInfo> ongoingSpawns = Lists.newArrayList();

    @Override
    public void initialise() {
        chunkProvider = worldRenderer.getChunkProvider();
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        Iterator<SpawnCachingInfo> i = ongoingSpawns.iterator();
        while (i.hasNext()) {
            SpawnCachingInfo spawning = i.next();
            if (chunkProvider.getChunk(Vector3i.zero()) != null && chunkProvider.getChunk(Vector3i.zero()).getChunkState() == Chunk.State.COMPLETE) {
                spawnPlayer(spawning.clientEntity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
                chunkProvider.removeRelevanceEntity(spawning.cachingEntity);
                spawning.cachingEntity.destroy();
                i.remove();
            }
        }
    }

    private void spawnPlayer(EntityRef clientEntity, Vector3i spawnPos) {
        while (worldRenderer.getWorldProvider().getBlock(spawnPos) == BlockManager.getAir() && spawnPos.y > 0) {
            spawnPos.y--;
        }

        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            PlayerFactory playerFactory = new PlayerFactory(entityManager, inventoryManager);
            EntityRef playerCharacter = playerFactory.newInstance(new Vector3f(spawnPos.x, spawnPos.y + 1.5f, spawnPos.z));
            NetworkComponent netComp = playerCharacter.getComponent(NetworkComponent.class);
            if (netComp != null) {
                netComp.owner = clientEntity;
                playerCharacter.saveComponent(netComp);
            }
            Client clientListener = networkSystem.getOwner(clientEntity);
            worldRenderer.getChunkProvider().addRelevanceEntity(playerCharacter, clientListener.getViewDistance(), clientListener);

            client.character = playerCharacter;
            clientEntity.saveComponent(client);
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onConnect(ConnectedEvent connected, EntityRef entity) {
        Vector3i pos = Vector3i.zero();
        if (chunkProvider.getChunk(pos) != null && chunkProvider.getChunk(pos).getChunkState() == Chunk.State.COMPLETE) {
            spawnPlayer(entity, new Vector3i(Chunk.SIZE_X / 2, Chunk.SIZE_Y, Chunk.SIZE_Z / 2));
        } else {
            EntityRef spawnZoneEntity = entityManager.create();
            spawnZoneEntity.setPersisted(false);
            spawnZoneEntity.addComponent(new LocationComponent(new Vector3f(Chunk.SIZE_X / 2, Chunk.SIZE_Y / 2, Chunk.SIZE_Z / 2)));
            worldRenderer.getChunkProvider().addRelevanceEntity(spawnZoneEntity, 4);

            ongoingSpawns.add(new SpawnCachingInfo(spawnZoneEntity, entity));
        }
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onDisconnect(DisconnectedEvent connected, EntityRef entity) {
        entity.getComponent(ClientComponent.class).character.destroy();
    }

    /**
     * Information on the caching of an area of the world for character spawning
     */
    private static class SpawnCachingInfo {
        public EntityRef cachingEntity = EntityRef.NULL;
        public EntityRef clientEntity = EntityRef.NULL;

        public SpawnCachingInfo(EntityRef cachingEntity, EntityRef clientEntity) {
            this.cachingEntity = cachingEntity;
            this.clientEntity = clientEntity;
        }
    }
}
