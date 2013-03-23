package org.terasology.entitySystem.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * Provides the ability to serialize and deserialize prefabs to the EntityData.Prefab proto buffer format.
 * <p/>
 * As with the component serializer, a component id mapping can be provided to have components serialized against
 * ids rather than name strings.
 * <p/>
 * It is also possible to set whether entity ids will be handled or ignored - if ignored then deserialized entities will
 * be given new ids.
 *
 * @author Immortius
 */
public class PrefabSerializer {
    private static final Logger logger = LoggerFactory.getLogger(PrefabSerializer.class);

    private PrefabManager prefabManager;
    private ComponentSerializer componentSerializer;

    public PrefabSerializer(PrefabManager prefabManager, ComponentLibrary componentLibrary) {
        this.prefabManager = prefabManager;
        componentSerializer = new ComponentSerializer(componentLibrary);
    }

    /**
     * Sets the mapping between component classes and the ids that are used for serialization
     *
     * @param table
     */
    public void setComponentIdMapping(Map<Class<? extends Component>, Integer> table) {
        componentSerializer.setIdMapping(table);
    }

    /**
     * Clears the mapping between component classes and ids. This causes components to be serialized with their component
     * class name instead.
     */
    public void removeComponentIdMapping() {
        componentSerializer.removeIdMapping();
    }

    /**
     * @param prefab
     * @return The serialized prefab
     */
    public EntityData.Prefab serialize(Prefab prefab) {
        EntityData.Prefab.Builder prefabData = EntityData.Prefab.newBuilder();
        prefabData.setName(prefab.getName());
        for (Prefab parent : prefab.getParents()) {
            prefabData.addParentName(parent.getName());
        }
        prefabData.setPersisted(prefab.isPersisted());

        for (Component component : prefab.iterateOwnedComponents()) {
            EntityData.Component componentData = componentSerializer.serialize(component);
            if (componentData != null) {
                prefabData.addComponent(componentData);
            }
        }
        return prefabData.build();
    }

    /**
     * Deserializes a prefab, with a known uri. This uri will override any uri within the serialized prefab
     *
     * @param prefabData
     * @param uri
     * @return The deserialized prefab
     */
    public Prefab deserialize(EntityData.Prefab prefabData, AssetUri uri) {
        String name = uri.getSimpleString();

        Prefab prefab = prefabManager.createPrefab(name);
        prefab.setPersisted(prefabData.getPersisted());
        for (String parentName : prefabData.getParentNameList()) {
            int packageSplit = parentName.indexOf(':');
            if (packageSplit == -1) {
                parentName = uri.getPackage() + ":" + parentName;
            }
            Prefab parent = prefabManager.getPrefab(parentName);

            if (parent == null) {
                logger.error("Missing parent prefab (need to fix parent serialization)");
            } else {
                prefab.addParent(parent);
            }
        }

        for (EntityData.Component componentData : prefabData.getComponentList()) {
            Component component = componentSerializer.deserialize(componentData);
            if (component != null) {
                prefab.setComponent(component);
            }
        }

        return prefab;
    }

    /**
     * @param prefabData
     * @return The deserialized prefab, or null if deserialization failed.
     */
    public Prefab deserialize(EntityData.Prefab prefabData) {
        if (!prefabData.hasName() || prefabData.getName().isEmpty()) {
            logger.error("Prefab missing name");
            return null;
        }
        Prefab prefab = prefabManager.createPrefab(prefabData.getName());
        for (String parentName : prefabData.getParentNameList()) {
            Prefab parent = prefabManager.getPrefab(parentName);
            if (parent == null) {
                logger.error("Missing parent prefab (need to fix parent serialization)");
            } else {
                prefab.addParent(parent);
            }
        }
        for (EntityData.Component componentData : prefabData.getComponentList()) {
            Component component = componentSerializer.deserialize(componentData);
            if (component != null) {
                prefab.setComponent(component);
            }
        }
        return prefab;
    }

}
