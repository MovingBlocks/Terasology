package org.terasology.entitySystem.pojo.persistence;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import gnu.trove.procedure.TIntProcedure;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.pojo.persistence.core.*;
import org.terasology.entitySystem.pojo.persistence.extension.EntityRefTypeHandler;
import org.terasology.protobuf.EntityData;

import java.lang.reflect.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityPersisterImpl implements EntityPersister {
    private static final int MAX_SERIALIZATION_DEPTH = 1;

    private Logger logger = Logger.getLogger(getClass().getName());
    private Map<Class<? extends Component>, ComponentMetadata> componentSerializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends Component>> componentTypeLookup = Maps.newHashMap();
    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    private PrefabManager prefabManager;
    private PersistableEntityManager entityManager;

    private EntityRefTypeHandler entityRefTypeHandler;

    private boolean useLookupTables = false;

    BiMap<Integer, Class<? extends Component>> componentIdTable = HashBiMap.create();

    public EntityPersisterImpl() {
        registerTypeHandler(Boolean.class, new BooleanTypeHandler());
        registerTypeHandler(Boolean.TYPE, new BooleanTypeHandler());
        registerTypeHandler(Byte.class, new ByteTypeHandler());
        registerTypeHandler(Byte.TYPE, new ByteTypeHandler());
        registerTypeHandler(Double.class, new DoubleTypeHandler());
        registerTypeHandler(Double.TYPE, new DoubleTypeHandler());
        registerTypeHandler(Float.class, new FloatTypeHandler());
        registerTypeHandler(Float.TYPE, new FloatTypeHandler());
        registerTypeHandler(Integer.class, new IntTypeHandler());
        registerTypeHandler(Integer.TYPE, new IntTypeHandler());
        registerTypeHandler(Long.class, new LongTypeHandler());
        registerTypeHandler(Long.TYPE, new LongTypeHandler());
        registerTypeHandler(String.class, new StringTypeHandler());

        entityRefTypeHandler = new EntityRefTypeHandler(entityManager);
        registerTypeHandler(EntityRef.class, entityRefTypeHandler);
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
    }

    public <T extends Component> void registerComponentClass(Class<T> componentClass) {
        try {
            // Check if constructor exists
            componentClass.getConstructor();
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, String.format("Unable to register component class %s: Default Constructor Required", componentClass.getSimpleName()));
            return;
        }

        ComponentMetadata<T> info = new ComponentMetadata<T>(componentClass);
        for (Field field : componentClass.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.log(Level.SEVERE, "Unsupported field type in component type " + componentClass.getSimpleName() + ", " + field.getName() + " : " + field.getGenericType());
            } else {
                info.addField(new FieldMetadata(field, componentClass, typeHandler));
            }
        }
        componentSerializationLookup.put(componentClass, info);
        componentTypeLookup.put(PersistenceUtil.getComponentClassName(componentClass).toLowerCase(Locale.ENGLISH), componentClass);
    }

    public EntityData.World serializeWorld() {
        final EntityData.World.Builder world = EntityData.World.newBuilder();
        writeIdInfo(world);

        if (isUsingLookupTables()) {
            writeComponentTypeTable(world);
        }

        for (Prefab prefab : prefabManager.listPrefabs()) {
            world.addPrefab(serializePrefab(prefab));
        }

        for (EntityRef entity : entityManager.iteratorEntities()) {
            world.addEntity(serializeEntity(entity));
        }
        return world.build();
    }

    public EntityData.Entity serializeEntity(EntityRef entityRef) {
        EntityInfoComponent entityInfo = entityRef.getComponent(EntityInfoComponent.class);
        if (entityInfo != null) {
            if (entityInfo.parentPrefab != null && prefabManager.exists(entityInfo.parentPrefab)) {
                return serializeEntityDelta(entityRef, prefabManager.getPrefab(entityInfo.parentPrefab));
            }
        }
        return serializeEntityFull(entityRef);
    }

    public EntityData.Prefab serializePrefab(Prefab prefab) {
        EntityData.Prefab.Builder prefabData = EntityData.Prefab.newBuilder();
        prefabData.setName(prefab.getName());
        for (Prefab parent : prefab.getParents()) {
            prefabData.addParentName(parent.getName());
        }

        for (Component component : prefab.listOwnComponents()) {
            EntityData.Component componentData = serializeComponent(component);
            if (componentData != null) {
                prefabData.addComponent(componentData);
            }
        }
        return prefabData.build();
    }

    public EntityData.Component serializeComponent(Component component) {
        ComponentMetadata<?> componentMetadata = componentSerializationLookup.get(component.getClass());
        if (componentMetadata == null) {
            logger.log(Level.SEVERE, "Unregistered component type: " + component.getClass());
            return null;
        }
        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        if (useLookupTables) {
            componentMessage.setTypeIndex(componentIdTable.inverse().get(component.getClass()));
        } else {
            componentMessage.setType(component.getName());
        }

        for (FieldMetadata field : componentMetadata.iterateFields()) {
            try {
                Object rawValue = field.getValue(component);
                if (rawValue == null) continue;

                EntityData.Value value = field.serialize(rawValue);
                if (value == null) continue;

                componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build());
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Exception during serializing component type: " + component.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Exception during serializing component type: " + component.getClass(), e);
            }
        }

        return componentMessage.build();
    }

    public void deserializeWorld(EntityData.World world) {
        entityManager.setNextId(world.getNextEntityId());
        for (Integer deadId : world.getFreedEntityIdList()) {
            entityManager.getFreedIds().add(deadId);
        }

        for (EntityData.Prefab prefabData : world.getPrefabList()) {
            if (!prefabManager.exists(prefabData.getName())) {
                deserializePrefab(prefabData);
            }
        }

        for (int index = 0; index < world.getComponentClassCount(); ++index) {
            Class<? extends Component> componentClass = componentTypeLookup.get(world.getComponentClass(index));
            if (componentClass != null) {
                componentIdTable.put(index, componentClass);
            }
        }

        // Gather valid ids
        for (EntityData.Entity entityData : world.getEntityList()) {
            entityRefTypeHandler.getValidIds().add(entityData.getId());
        }

        for (EntityData.Entity entityData : world.getEntityList()) {
            deserializeEntity(entityData);
        }

        entityRefTypeHandler.getValidIds().clear();
    }

    public EntityRef deserializeEntity(EntityData.Entity entityData) {
        EntityRef entity = entityManager.createEntityRefWithId(entityData.getId());
        if (entityData.hasParentPrefab() && !entityData.getParentPrefab().isEmpty() && prefabManager.exists(entityData.getParentPrefab())) {
            Prefab prefab = prefabManager.getPrefab(entityData.getParentPrefab());
            for (Component component : prefab.listComponents()) {
                String componentName = PersistenceUtil.getComponentClassName(component.getClass());
                if (!containsIgnoreCase(componentName, entityData.getRemovedComponentList())) {
                    entity.addComponent(copyComponent(component));
                }
            }
        }
        for (EntityData.Component componentData : entityData.getComponentList()) {
            Class<? extends Component> componentClass = getComponentClass(componentData);
            if (componentClass == null) continue;

            if (!entity.hasComponent(componentClass)) {
                entity.addComponent(deserializeComponent(componentData));
            } else {
                deserializeComponentOnto(entity.getComponent(componentClass), componentData);
            }
        }
        return entity;
    }

    public Prefab deserializePrefab(EntityData.Prefab prefabData) {
        Prefab prefab = prefabManager.createPrefab(prefabData.getName());
        for (String parentName : prefabData.getParentNameList()) {
            Prefab parent = prefabManager.getPrefab(parentName);
            if (parent == null) {
                logger.log(Level.SEVERE, "Missing parent prefab (need to fix parent serialization)");
            } else {
                prefab.addParent(parent);
            }
        }
        for (EntityData.Component componentData : prefabData.getComponentList()) {
            Component component = deserializeComponent(componentData);
            if (component != null) {
                prefab.setComponent(component);
            }
        }
        return prefab;
    }

    public Component deserializeComponent(EntityData.Component componentData) {
        Class<? extends Component> componentClass = getComponentClass(componentData);
        if (componentClass != null) {
            ComponentMetadata componentMetadata = componentSerializationLookup.get(componentClass);
            Component component = componentMetadata.newInstance();
            return deserializeOnto(component, componentData, componentMetadata);
        } else {
            logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
        }
        return null;
    }

    public <T extends Component> T copyComponent(T component) {
        @SuppressWarnings("unchecked") ComponentMetadata<T> componentMetadata = (ComponentMetadata<T>) componentSerializationLookup.get(component.getClass());
        if (componentMetadata == null) {
            logger.log(Level.SEVERE, "Unable to clone component: " + component.getClass() + ", not registered");
        } else {
            return componentMetadata.clone(component);
        }
        return null;
    }

    public void setEntityManager(PersistableEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PersistableEntityManager getEntityManager() {
        return entityManager;
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    public void setPersistableEntityManager(PersistableEntityManager persistableEntityManager) {
        this.entityManager = persistableEntityManager;
        this.entityRefTypeHandler.setEntityManager(persistableEntityManager);
    }

    public boolean isUsingLookupTables() {
        return useLookupTables;
    }

    public void setUsingLookupTables(boolean enabled) {
        useLookupTables = enabled;
    }

    public void setComponentTypeIdTable(Map<Integer, Class<? extends Component>> componentIdTable) {
        this.componentIdTable.clear();
        this.componentIdTable.putAll(componentIdTable);
    }

    public void clearComponentTypeIdTable() {
        this.componentIdTable.clear();
    }

    // TODO: Refactor
    private TypeHandler getHandlerFor(Type type, int depth) {
        Class typeClass;
        if (type instanceof Class) {
            typeClass = (Class) type;
        } else if (type instanceof ParameterizedType) {
            typeClass = (Class) ((ParameterizedType) type).getRawType();
        } else {
            logger.log(Level.SEVERE, "Cannot obtain class for type " + type);
            return null;
        }

        if (Enum.class.isAssignableFrom(typeClass)) {
            return new EnumTypeHandler(typeClass);
        }
        // For lists, createEntityRef the handler for the contained type and wrap in a list type handler
        else if (List.class.isAssignableFrom(typeClass)) {
            // TODO - Improve parameter lookup
            if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length > 0)
            {
                TypeHandler innerHandler = getHandlerFor(((ParameterizedType)type).getActualTypeArguments()[0], depth);
                if (innerHandler != null) {
                    return new ListTypeHandler(innerHandler);
                }
            }
            logger.log(Level.SEVERE, "List field is not parameterized, or holds unsupported type");
            return null;
        }
        // For Maps, createEntityRef the handler for the value type (and maybe key too?)
        else if (Map.class.isAssignableFrom(typeClass)) {
            if (type instanceof ParameterizedType) {
                // TODO - Improve parameter lookup
                Type[] types = ((ParameterizedType)type).getActualTypeArguments();
                if (types.length > 1 && String.class.equals(types[0])) {
                    TypeHandler valueHandler = getHandlerFor(types[1], depth);
                    if (valueHandler != null) {
                        return new StringMapTypeHandler(valueHandler);
                    }
                }
            }
            logger.log(Level.SEVERE, "Map field is not parameterized, does not have a String key, or holds unsupported values");
        }
        // For know types, just use the handler
        else if (typeHandlers.containsKey(typeClass)) {
            return typeHandlers.get(typeClass);
        }
        // For unknown types of a limited depth, assume they are data holders and use them
        else if (depth <= MAX_SERIALIZATION_DEPTH && !typeClass.isLocalClass() && !(typeClass.isMemberClass() && !Modifier.isStatic(typeClass.getModifiers()))) {
            logger.log(Level.WARNING, "Handling serialization of type " + typeClass + " via MappedContainer");
            MappedContainerTypeHandler mappedHandler = new MappedContainerTypeHandler(typeClass);
            for (Field field : typeClass.getDeclaredFields()) {
                if (Modifier.isTransient(field.getModifiers()))
                    continue;

                field.setAccessible(true);
                TypeHandler handler = getHandlerFor(field.getGenericType(), depth + 1);
                if (handler == null) {
                    logger.log(Level.SEVERE, "Unsupported field type in component type " + typeClass.getSimpleName() + ", " + field.getName() + " : " + field.getGenericType());
                } else {
                    mappedHandler.addField(new FieldMetadata(field, typeClass, handler));
                }
            }
            return mappedHandler;
        }

        return null;
    }

    private void writeComponentTypeTable(EntityData.World.Builder world) {
        for (Class<? extends Component> componentClass : componentSerializationLookup.keySet()) {
            int index = componentIdTable.size();
            componentIdTable.put(index, componentClass);
            world.addComponentClass(PersistenceUtil.getComponentClassName(componentClass));
        }
    }

    private void writeIdInfo(final EntityData.World.Builder world) {
        world.setNextEntityId(entityManager.getNextId());
        entityManager.getFreedIds().forEach(new TIntProcedure() {
            public boolean execute(int i) {
                world.addFreedEntityId(i);
                return true;
            }
        });
    }

    private EntityData.Entity serializeEntityFull(EntityRef entityRef) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(entityRef.getId());
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            EntityData.Component componentData = serializeComponent(component);
            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        return entity.build();
    }

    private EntityData.Entity serializeEntityDelta(EntityRef entityRef, Prefab prefab) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(entityRef.getId());
        entity.setParentPrefab(prefab.getName());
        for (Component component : entityRef.iterateComponents()) {
            if (component.getClass().equals(EntityInfoComponent.class))
                continue;

            Component prefabComponent = prefab.getComponent(component.getClass());
            EntityData.Component componentData;
            if (prefabComponent == null) {
                componentData = serializeComponent(component);
            } else {
                componentData = serializeComponent(prefabComponent, component);
            }

            if (componentData != null) {
                entity.addComponent(componentData);
            }
        }
        for (Component prefabComponent : prefab.listComponents()) {
            if (!entityRef.hasComponent(prefabComponent.getClass())) {
                entity.addRemovedComponent(PersistenceUtil.getComponentClassName(prefabComponent.getClass()));
            }
        }
        return entity.build();
    }

    private EntityData.Component serializeComponent(Component base, Component delta) {
        ComponentMetadata<?> componentMetadata = componentSerializationLookup.get(base.getClass());
        if (componentMetadata == null) {
            logger.log(Level.SEVERE, "Unregistered component type: " + base.getClass());
            return null;
        }

        EntityData.Component.Builder componentMessage = EntityData.Component.newBuilder();
        if (useLookupTables) {
            componentMessage.setTypeIndex(componentIdTable.inverse().get(base.getClass()));
        } else {
            componentMessage.setType(delta.getName());
        }

        boolean changed = false;
        for (FieldMetadata field : componentMetadata.iterateFields()) {
            try {
                Object origValue = field.getValue(base);
                Object deltaValue = field.getValue(delta);

                if (!Objects.equal(origValue, deltaValue)) {
                    EntityData.Value value = field.serialize(deltaValue);
                    componentMessage.addField(EntityData.NameValue.newBuilder().setName(field.getName()).setValue(value).build());
                    changed = true;
                }
            } catch (IllegalAccessException e) {
                logger.log(Level.SEVERE, "Exception during serializing component type: " + base.getClass(), e);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Exception during serializing component type: " + base.getClass(), e);
            }
        }
        if (changed) {
            return componentMessage.build();
        }
        return null;
    }

    private Class<? extends Component> getComponentClass(EntityData.Component componentData) {
        Class<? extends Component> result = null;
        if (componentData.hasTypeIndex()) {
            result = componentIdTable.get(componentData.getTypeIndex());
            if (result == null) {
                logger.log(Level.WARNING, "Unable to deserialise unknown component with id: " + componentData.getTypeIndex());
            }
            return result;
        } else if (componentData.hasType()) {
            result = componentTypeLookup.get(componentData.getType().toLowerCase(Locale.ENGLISH));
            if (result == null) {
                logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
            }
            return result;
        }
        logger.log(Level.WARNING, "Unable to deserialise component, no type provided.");

        return result;
    }

    private Component deserializeComponentOnto(Component component, EntityData.Component componentData) {
        Class<? extends Component> componentClass = getComponentClass(componentData);
        if (componentClass != null) {
            ComponentMetadata componentMetadata = componentSerializationLookup.get(componentClass);
            return deserializeOnto(component, componentData, componentMetadata);
        } else {
            logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
        }
        return null;
    }

    private Component deserializeOnto(Component component, EntityData.Component componentData, ComponentMetadata componentMetadata) {
        try {
            for (EntityData.NameValue field : componentData.getFieldList()) {
                FieldMetadata fieldInfo = componentMetadata.getField(field.getName());
                if (fieldInfo == null)
                    continue;

                Object value = fieldInfo.deserialize(field.getValue());
                if (value == null)
                    continue;
                fieldInfo.setValue(component, value);
            }
            return component;
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE, "Exception during serializing component type: " + component.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Exception during serializing component type: " + component.getClass(), e);
        }
        return null;
    }

    private boolean containsIgnoreCase(String componentName, List<String> removedComponentList) {
        String lowerCaseName = componentName.toLowerCase(Locale.ENGLISH);
        for (String removed : removedComponentList) {
            if (lowerCaseName.equals(removed.toLowerCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }


}
