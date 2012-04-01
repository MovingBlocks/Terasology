package org.terasology.entitySystem.pojo;

import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.common.NullEntityRef;
import org.terasology.entitySystem.common.NullIterator;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.entitySystem.pojo.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.pojo.persistence.SerializationInfo;
import org.terasology.entitySystem.pojo.persistence.core.*;
import org.terasology.entitySystem.pojo.persistence.FieldInfo;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.entitySystem.pojo.persistence.extension.EntityRefTypeHandler;
import org.terasology.protobuf.EntityData;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prototype entity manager. Not intended for final use, but a stand in for experimentation.
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManager implements EntityManager {
    public static final int NULL_ID = 0;
    private static final int MAX_SERIALIZATION_DEPTH = 1;

    private static Logger logger = Logger.getLogger(PojoEntityManager.class.getName());

    // TODO: Need to recycle freed ids.
    private int nextEntityId = 1;
    ComponentTable store = new ComponentTable();
    private EventSystem eventSystem;
    // TODO: Weak hash map cache of entityRefs
    // TODO: Invalidate entity ref when destroyed

    private Map<Class<? extends Component>, SerializationInfo> componentSerializationLookup = Maps.newHashMap();
    private Map<String, Class<? extends Component>> componentTypeLookup = Maps.newHashMap();
    private Map<Class<?>, TypeHandler<?>> typeHandlers = Maps.newHashMap();

    // Temporary list of valid ids used during loading.
    private TIntSet validIds;

    public PojoEntityManager() {
        typeHandlers.put(Boolean.class, new BooleanTypeHandler());
        typeHandlers.put(Boolean.TYPE, new BooleanTypeHandler());
        typeHandlers.put(Byte.class, new ByteTypeHandler());
        typeHandlers.put(Byte.TYPE, new ByteTypeHandler());
        typeHandlers.put(Double.class, new DoubleTypeHandler());
        typeHandlers.put(Double.TYPE, new DoubleTypeHandler());
        typeHandlers.put(Float.class, new FloatTypeHandler());
        typeHandlers.put(Float.TYPE, new FloatTypeHandler());
        typeHandlers.put(Integer.class, new IntTypeHandler());
        typeHandlers.put(Integer.TYPE, new IntTypeHandler());
        typeHandlers.put(Long.class, new LongTypeHandler());
        typeHandlers.put(Long.TYPE, new LongTypeHandler());
        typeHandlers.put(String.class, new StringTypeHandler());
        typeHandlers.put(EntityRef.class, new EntityRefTypeHandler(this));
    }

    public <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler) {
        typeHandlers.put(forClass, handler);
    }

    // TODO: Check for empty constructors
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
                info.addField(new FieldInfo(field, typeHandler));
            }
        }
        componentSerializationLookup.put(componentClass, info);
        componentTypeLookup.put(getComponentClassName(componentClass), componentClass);
    }

    // TODO: Implement for loading
    public EntityRef getEntityRef(int id) {
        if (validIds.contains(id)) {
            return new PojoEntityRef(this, id);
        }
        return EntityRef.NULL;
    }

    public void save(File file, SaveFormat format) throws IOException {
        EntityData.World.Builder world = EntityData.World.newBuilder();
        world.setNextEntityId(nextEntityId);

        TIntIterator idIterator = store.entityIdIterator();
        while (idIterator.hasNext()) {
            int id = idIterator.next();
            EntityData.Entity.Builder entity = EntityData.Entity.newBuilder();
            entity.setId(id);
            for (Component component : iterateComponents(id)) {
                SerializationInfo serializationInfo = componentSerializationLookup.get(component.getClass());
                if (serializationInfo != null) {
                    entity.addComponent(serializationInfo.serialize(component));
                } else {
                    logger.log(Level.SEVERE, "Unregistered component type: " + component.getClass());
                }
            }
            world.addEntity(entity.build());
        }

        FileOutputStream out = new FileOutputStream(file);
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(out));
        try {
            switch (format) {
                case Binary:
                    // TODO: Do we need to buffer this output stream, or is it buffered in google's code?
                    world.build().writeTo(out);
                    out.flush();
                    break;
                case Text:
                    TextFormat.print(world.build(), bufferedWriter);
                    bufferedWriter.flush();
                    break;
                case JSON:
                    EntityDataJSONFormat.write(bufferedWriter, world.build());
                    bufferedWriter.flush();
                    break;
            }
        }
        finally {
            try {
                out.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }
    }

    public void load(File file, SaveFormat format) throws IOException {
        clear();

        FileInputStream in = new FileInputStream(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        EntityData.World world = null;
        try {
            switch (format) {
                case Binary:
                    world = EntityData.World.parseFrom(in);
                    break;
                case Text:
                    EntityData.World.Builder builder = EntityData.World.newBuilder();
                    TextFormat.merge(bufferedReader, builder);
                    world = builder.build();
                    break;
                case JSON:
                    throw new IOException("Reading JSON format not supported yet");
            }
        }
        finally {
            try {
                in.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to close file", e);
            }
        }

        if (world != null) {
            nextEntityId = world.getNextEntityId();
            // TODO: handle more stuff

            // TODO: just persist a list of used ids?
            validIds = new TIntHashSet(world.getEntityCount());
            for (EntityData.Entity entityData : world.getEntityList()) {
                validIds.add(entityData.getId());
            }

            for (EntityData.Entity entityData : world.getEntityList()) {
                int entityId = entityData.getId();
                for (EntityData.Component componentData : entityData.getComponentList()) {
                    Class<? extends Component> componentClass = componentTypeLookup.get(componentData.getType().toLowerCase(Locale.ENGLISH));
                    if (componentClass != null) {
                        SerializationInfo serializationInfo = componentSerializationLookup.get(componentClass);
                        Component component = serializationInfo.deserialize(componentData);
                        store.put(entityId, component);
                    }
                }
            }

            validIds = null;
        }
    }

    public void clear() {
        store.clear();
        nextEntityId = 1;
    }

    public EntityRef create() {
        if (nextEntityId == NULL_ID) nextEntityId++;
        return new PojoEntityRef(this, nextEntityId++);
    }

    // TODO: Clean up this + getEntityRef + other POJOEntityRef instantiation
    private EntityRef get(int entityId) {
        if (entityId == NULL_ID) {
            return EntityRef.NULL;
        }
        return new PojoEntityRef(this, entityId);
    }

    public int getComponentCount(Class<? extends Component> componentClass) {
        return store.getComponentCount(componentClass);
    }

    public EventSystem getEventSystem() {
        return eventSystem;
    }

    public void setEventSystem(EventSystem eventSystem) {
        this.eventSystem = eventSystem;
    }

    public <T extends Component> Iterable<Map.Entry<EntityRef,T>> iterateComponents(Class<T> componentClass) {
        TIntObjectIterator<T> iterator = store.componentIterator(componentClass);
        if (iterator != null) {
            List<Map.Entry<EntityRef, T>> list = new ArrayList<Map.Entry<EntityRef, T>>();
            while (iterator.hasNext()) {
                iterator.advance();
                list.add(new EntityEntry<T>(new PojoEntityRef(this, iterator.key()), iterator.value()));
            }
            return list;
        }
        return NullIterator.newInstance();
    }

    public Iterable<EntityRef> iteratorEntities(Class<? extends Component>... componentClasses) {
        if (componentClasses.length == 0) {
            return NullIterator.newInstance();
        }
        TIntList idList = new TIntArrayList();
        TIntObjectIterator<? extends Component> primeIterator = store.componentIterator(componentClasses[0]);
        if (primeIterator == null) {
            return NullIterator.newInstance();
        }

        while (primeIterator.hasNext()) {
            primeIterator.advance();
            int id = primeIterator.key();
            boolean discard = false;
            for (int i = 1; i < componentClasses.length; ++i) {
                if (store.get(id, componentClasses[i]) == null) {
                    discard = true;
                    break;
                }
            }
            if (!discard) {
                idList.add(primeIterator.key());
            }
        }
        return new EntityIterable(idList);
    }

    boolean hasComponent(int entityId, Class<? extends Component> componentClass) {
        return store.get(entityId, componentClass) != null;
    }

    Iterable<Component> iterateComponents(int entityId) {
        return store.iterateComponents(entityId);
    }

    void destroy(int entityId) {
        if (eventSystem != null) {
            eventSystem.send(new PojoEntityRef(this, entityId), RemovedComponentEvent.newInstance());
        }
        store.remove(entityId);
    }

    <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
        return store.get(entityId, componentClass);
    }

    <T extends Component> T addComponent(int entityId, T component) {
        Component oldComponent = store.put(entityId, component);
        if (eventSystem != null) {
            if (oldComponent == null) {
                eventSystem.send(new PojoEntityRef(this, entityId), AddComponentEvent.newInstance(), component);
            } else {
                eventSystem.send(new PojoEntityRef(this, entityId), ChangedComponentEvent.newInstance(), component);
            }
        }
        return component;
    }

    void removeComponent(int entityId, Class<? extends Component> componentClass) {
        Component component = store.get(entityId, componentClass);
        if (component != null) {
            if (eventSystem != null) {
                eventSystem.send(new PojoEntityRef(this, entityId), RemovedComponentEvent.newInstance(), component);
            }
            store.remove(entityId, componentClass);
        }
    }

    void saveComponent(int entityId, Component component) {
        if (eventSystem != null) {
            eventSystem.send(new PojoEntityRef(this, entityId), ChangedComponentEvent.newInstance(), component);
        }
    }


    private static class EntityEntry<T> implements Map.Entry<EntityRef, T>
    {
        private EntityRef key;
        private T value;
        
        public EntityEntry(EntityRef ref, T value) {
            this.key = ref;
            this.value = value;
        }

        public EntityRef getKey() {
            return key;
        }

        public T getValue() {
            return value;
        }

        public T setValue(T value) {
            throw new UnsupportedOperationException();
        }
    }
    
    private class EntityIterable implements Iterable<EntityRef>
    {
        private TIntList list;

        public EntityIterable(TIntList list) {
            this.list = list;
        }

        public Iterator<EntityRef> iterator() {
            return new EntityIterator(list.iterator());
        }
    }
    
    private class EntityIterator implements Iterator<EntityRef>
    {
        private TIntIterator idIterator;
        
        public EntityIterator(TIntIterator idIterator) {
            this.idIterator = idIterator;        
        }

        public boolean hasNext() {
            return idIterator.hasNext();
        }

        public EntityRef next() {
            return get(idIterator.next());
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
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
        // For lists, get the handler for the contained type and wrap in a list type handler
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
        // For Maps, get the handler for the value type (and maybe key too?)
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
                    mappedHandler.addField(new FieldInfo(field, handler));
                }
            }
            return mappedHandler;
        }

        return null;
    }

    private String getComponentClassName(Class<? extends Component> componentClass) {
        String name = componentClass.getSimpleName().toLowerCase(Locale.ENGLISH);
        int index = name.lastIndexOf("component");
        if (index != -1) {
            return name.substring(0, index);
        }
        return name;
    }
}
