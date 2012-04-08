package org.terasology.entitySystem.pojo.persistence;

import com.google.common.collect.Maps;
import gnu.trove.iterator.TIntIterator;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.pojo.persistence.core.*;
import org.terasology.protobuf.EntityData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityPersisterImpl implements EntityPersister {
    private static final int MAX_SERIALIZATION_DEPTH = 1;

    private Logger logger = Logger.getLogger(getClass().getName());
    private Map<Class<? extends Component>, SerializationInfo> componentSerializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends Component>> componentTypeLookup = Maps.newHashMap();
    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    private PrefabManager prefabManager;
    private PersistableEntityManager entityManager;

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
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
    }

    public void registerComponentClass(Class<? extends Component> componentClass) {
        try {
            // Check if constructor exists
            componentClass.getConstructor();
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, String.format("Unable to register component class %s: Default Constructor Required", componentClass.getSimpleName()));
            return;
        }

        SerializationInfo info = new SerializationInfo(componentClass);
        for (Field field : componentClass.getDeclaredFields()) {
            if (Modifier.isTransient(field.getModifiers()))
                continue;
            field.setAccessible(true);
            TypeHandler typeHandler = getHandlerFor(field.getGenericType(), 0);
            if (typeHandler == null) {
                logger.log(Level.SEVERE, "Unsupported field type in component type " + componentClass.getSimpleName() + ", " + field.getName() + " : " + field.getGenericType());
            } else {
                info.addField(new FieldInfo(field, componentClass, typeHandler));
            }
        }
        componentSerializationLookup.put(componentClass, info);
        componentTypeLookup.put(PersistenceUtil.getComponentClassName(componentClass).toLowerCase(Locale.ENGLISH), componentClass);
    }

    public EntityData.Entity serializeEntity(int id, EntityRef entityRef) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(id);
        EntityInfoComponent entityInfo = entityRef.getComponent(EntityInfoComponent.class);
        if (entityInfo != null) {
            if (entityInfo.parentPrefab != null && prefabManager.exists(entityInfo.parentPrefab)) {
                return serializeEntity(id, entityRef, prefabManager.getPrefab(entityInfo.parentPrefab));
            }
        }
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

    private EntityData.Entity serializeEntity(int id, EntityRef entityRef, Prefab prefab) {
        EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
        entity.setId(id);
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

    public EntityData.Component serializeComponent(Component component) {
        SerializationInfo serializationInfo = componentSerializationLookup.get(component.getClass());
        if (serializationInfo == null) {
            logger.log(Level.SEVERE, "Unregistered component type: " + component.getClass());
            registerComponentClass(component.getClass());
            serializationInfo = componentSerializationLookup.get(component.getClass());
        }
        if (serializationInfo != null) {
            return serializationInfo.serialize(component);
        }
        return null;
    }

    public EntityData.Component serializeComponent(Component base, Component delta) {
        SerializationInfo serializationInfo = componentSerializationLookup.get(base.getClass());
        if (serializationInfo == null) {
            logger.log(Level.SEVERE, "Unregistered component type: " + base.getClass());
            registerComponentClass(base.getClass());
            serializationInfo = componentSerializationLookup.get(base.getClass());
        }
        if (serializationInfo != null) {
            return serializationInfo.serialize(base, delta);
        }
        return null;
    }

    public Component deserializeComponent(EntityData.Component componentData) {
        Class<? extends Component> componentClass = componentTypeLookup.get(componentData.getType().toLowerCase(Locale.ENGLISH));
        if (componentClass != null) {
            SerializationInfo serializationInfo = componentSerializationLookup.get(componentClass);
            return serializationInfo.deserialize(componentData);
        } else {
            logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
        }
        return null;
    }

    public Component deserializeComponentOnto(Component component, EntityData.Component componentData) {
        Class<? extends Component> componentClass = componentTypeLookup.get(componentData.getType().toLowerCase(Locale.ENGLISH));
        if (componentClass != null) {
            SerializationInfo serializationInfo = componentSerializationLookup.get(componentClass);
            return serializationInfo.deserializeOnto(component, componentData);
        } else {
            logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
        }
        return null;
    }

    public EntityRef deserializeEntity(EntityData.Entity entityData) {
        EntityRef entity = entityManager.createEntityWithId(entityData.getId());
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
            Class<? extends Component> componentClass = componentTypeLookup.get(componentData.getType().toLowerCase(Locale.ENGLISH));
            if (componentClass == null) {
                logger.log(Level.WARNING, "Unable to deserialise unknown component type: " + componentData.getType());
                continue;
            }
            if (!entity.hasComponent(componentClass)) {
                entity.addComponent(deserializeComponent(componentData));
            } else {
                deserializeComponentOnto(entity.getComponent(componentClass), componentData);
            }
        }
        return entity;
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

    public Component copyComponent(Component component) {
        // TODO: Be more efficient
        SerializationInfo serializationInfo = componentSerializationLookup.get(component.getClass());
        if (serializationInfo == null) {
            logger.log(Level.SEVERE, "Unable to clone component: " + component.getClass() + ", not registered");
        } else {
            EntityData.Component data = serializationInfo.serialize(component);
            return serializationInfo.deserialize(data);
        }
        return null;
    }

    public void setPrefabManager(PrefabManager prefabManager) {
        this.prefabManager = prefabManager;
    }

    public void setPersistableEntityManager(PersistableEntityManager persistableEntityManager) {
        this.entityManager = persistableEntityManager;
    }

    // TODO: Refactor
    private TypeHandler getHandlerFor(Type type, int depth) {
        Class typeClass = null;
        if (type instanceof Class) {
            typeClass = (Class) type;
        } else if (type instanceof ParameterizedType) {
            typeClass = (Class) ((ParameterizedType) type).getRawType();
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
                    mappedHandler.addField(new FieldInfo(field, typeClass, handler));
                }
            }
            return mappedHandler;
        }

        return null;
    }

}
