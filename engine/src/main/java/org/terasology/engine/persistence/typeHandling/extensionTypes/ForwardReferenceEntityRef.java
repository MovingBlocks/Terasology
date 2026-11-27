// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.metadata.ComponentFieldMetadata;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An {@link EntityRef} for an id not registered yet. Retries the lookup on every delegated call until the
 * target, deserialized later in the same batch, exists.
 */
public class ForwardReferenceEntityRef extends EntityRef {
    private static final int MAX_TRAVERSAL_DEPTH = 32;
    private final EngineEntityManager entityManager;
    private final long id;
    private EntityRef resolved;

    public ForwardReferenceEntityRef(EngineEntityManager entityManager, long id) {
        this.entityManager = entityManager;
        this.id = id;
    }

    private EntityRef resolve() {
        if (resolved == null || !resolved.exists()) {
            EntityRef candidate = entityManager.getEntity(id);
            if (candidate.exists()) {
                resolved = candidate;
            } else {
                return candidate;
            }
        }
        return resolved;
    }

    @Override
    public EntityRef copy() {
        EntityRef target = resolve();
        if (target.exists()) {
            return target.copy();
        }
        // Not resolvable yet - the copy has to stay a forward reference too, or it freezes into
        // "doesn't exist" permanently the moment something copies this ref before its target loads.
        return new ForwardReferenceEntityRef(entityManager, id);
    }

    @Override
    public boolean exists() {
        return resolve().exists();
    }

    @Override
    public boolean isActive() {
        return resolve().isActive();
    }

    @Override
    public void destroy() {
        resolve().destroy();
    }

    @Override
    public <T extends Event> T send(T event) {
        return resolve().send(event);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public boolean isPersistent() {
        return resolve().isPersistent();
    }

    @Override
    public boolean isAlwaysRelevant() {
        return resolve().isAlwaysRelevant();
    }

    @Override
    public void setAlwaysRelevant(boolean alwaysRelevant) {
        resolve().setAlwaysRelevant(alwaysRelevant);
    }

    @Override
    public EntityRef getOwner() {
        return resolve().getOwner();
    }

    @Override
    public void setOwner(EntityRef owner) {
        resolve().setOwner(owner);
    }

    @Override
    public void setScope(EntityScope scope) {
        resolve().setScope(scope);
    }

    @Override
    public void setSectorScope(long maxDelta) {
        resolve().setSectorScope(maxDelta);
    }

    @Override
    public void setSectorScope(long unloadedMaxDelta, long loadedMaxDelta) {
        resolve().setSectorScope(unloadedMaxDelta, loadedMaxDelta);
    }

    @Override
    public EntityScope getScope() {
        return resolve().getScope();
    }

    @Override
    public void invalidate() {
        resolve().invalidate();
    }

    @Override
    public Prefab getParentPrefab() {
        return resolve().getParentPrefab();
    }

    @Override
    public String toFullDescription() {
        return resolve().toFullDescription();
    }

    @Override
    public <T extends Component> T addComponent(T component) {
        return resolve().addComponent(component);
    }

    @Override
    public void removeComponent(Class<? extends Component> componentClass) {
        resolve().removeComponent(componentClass);
    }

    @Override
    public void saveComponent(Component component) {
        resolve().saveComponent(component);
    }

    @Override
    public boolean hasComponent(Class<? extends Component> component) {
        return resolve().hasComponent(component);
    }

    @Override
    public boolean hasAnyComponents(List<Class<? extends Component>> filterComponents) {
        return resolve().hasAnyComponents(filterComponents);
    }

    @Override
    public boolean hasAllComponents(List<Class<? extends Component>> filterComponents) {
        return resolve().hasAllComponents(filterComponents);
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return resolve().getComponent(componentClass);
    }

    @Override
    public Iterable<Component> iterateComponents() {
        return resolve().iterateComponents();
    }

    /**
     * Fix-up pass: after a batch of entities has been fully deserialized, replaces every
     * {@link ForwardReferenceEntityRef} stored in component fields (including collections, maps,
     * arrays, and nested objects) with the now-resolvable real {@link EntityRef}. This eliminates
     * the per-call {@code exists()} re-lookup overhead that the proxy would otherwise carry for the
     * lifetime of the reference, without firing component-changed events via {@code saveComponent}.
     *
     * <p>Call this once at the end of each batch-restore site (EntityRestorer, GlobalStoreLoader,
     * WorldSerializerImpl) after all entities in the session have been deserialized.</p>
     *
     * @param entityManager the entity manager whose component library is used for field inspection
     * @param entities      the set of entities created in this restore session
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void resolveForwardReferences(EngineEntityManager entityManager, Iterable<EntityRef> entities) {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EntityRef entity : entities) {
            if (!entity.exists()) {
                continue;
            }
            for (Component component : entity.iterateComponents()) {
                ComponentMetadata metadata = entityManager.getComponentLibrary().getMetadata(component);
                if (metadata == null) {
                    continue;
                }
                for (ComponentFieldMetadata<?, ?> field : (Iterable<ComponentFieldMetadata<?, ?>>) metadata.getFields()) {
                    Object value = field.getValue(component);
                    Object resolved = resolveValue(entityManager, value, visited, 0);
                    if (resolved != value) {
                        ((ComponentFieldMetadata) field).setValue(component, resolved);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object resolveValue(EngineEntityManager entityManager, Object value, Set<Object> visited,
                                       int depth) {
        if (value == null || depth > MAX_TRAVERSAL_DEPTH) {
            return value;
        }
        if (value instanceof ForwardReferenceEntityRef) {
            ForwardReferenceEntityRef fref = (ForwardReferenceEntityRef) value;
            EntityRef real = entityManager.getEntity(fref.id);
            return real.exists() ? real : value;
        }
        if (value instanceof EntityRef || isLeafType(value.getClass()) || !visited.add(value)) {
            return value;
        }
        int nextDepth = depth + 1;
        if (value instanceof List<?>) {
            resolveList(entityManager, (List<Object>) value, visited, nextDepth);
        } else if (value instanceof Collection<?>) {
            resolveCollection(entityManager, (Collection<Object>) value, visited, nextDepth);
        } else if (value instanceof Map<?, ?>) {
            resolveMap(entityManager, (Map<Object, Object>) value, visited, nextDepth);
        } else if (value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive()) {
            resolveArray(entityManager, (Object[]) value, visited, nextDepth);
        } else if (!isJdkOrExternalLeafPackage(value.getClass())) {
            resolveObjectFields(entityManager, value, visited, nextDepth);
        }
        return value;
    }

    private static void resolveList(EngineEntityManager entityManager, List<Object> list, Set<Object> visited,
                                    int depth) {
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            Object resolved = resolveValue(entityManager, item, visited, depth);
            if (resolved != item) {
                try {
                    list.set(i, resolved);
                } catch (UnsupportedOperationException ignored) {
                    return;
                }
            }
        }
    }

    private static void resolveCollection(EngineEntityManager entityManager, Collection<Object> collection,
                                          Set<Object> visited, int depth) {
        List<Object[]> replacements = null;
        for (Object item : collection) {
            Object resolved = resolveValue(entityManager, item, visited, depth);
            if (resolved != item) {
                if (replacements == null) {
                    replacements = new ArrayList<>();
                }
                replacements.add(new Object[]{item, resolved});
            }
        }
        if (replacements != null) {
            for (Object[] pair : replacements) {
                try {
                    collection.remove(pair[0]);
                    collection.add(pair[1]);
                } catch (UnsupportedOperationException ignored) {
                    return;
                }
            }
        }
    }

    private static void resolveMap(EngineEntityManager entityManager, Map<Object, Object> map, Set<Object> visited,
                                   int depth) {
        List<Object[]> keyReplacements = null;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            Object resolvedVal = resolveValue(entityManager, val, visited, depth);
            if (resolvedVal != val) {
                try {
                    entry.setValue(resolvedVal);
                } catch (UnsupportedOperationException ignored) {
                    return;
                }
            }
            Object resolvedKey = resolveValue(entityManager, key, visited, depth);
            if (resolvedKey != key) {
                if (keyReplacements == null) {
                    keyReplacements = new ArrayList<>();
                }
                keyReplacements.add(new Object[]{key, resolvedKey, resolvedVal});
            }
        }
        if (keyReplacements != null) {
            for (Object[] triple : keyReplacements) {
                try {
                    map.remove(triple[0]);
                    map.put(triple[1], triple[2]);
                } catch (UnsupportedOperationException ignored) {
                    return;
                }
            }
        }
    }

    private static void resolveArray(EngineEntityManager entityManager, Object[] array, Set<Object> visited,
                                     int depth) {
        for (int i = 0; i < array.length; i++) {
            Object resolved = resolveValue(entityManager, array[i], visited, depth);
            if (resolved != array[i]) {
                array[i] = resolved;
            }
        }
    }

    private static void resolveObjectFields(EngineEntityManager entityManager, Object target, Set<Object> visited,
                                            int depth) {
        for (Class<?> cls = target.getClass(); cls != null && cls != Object.class; cls = cls.getSuperclass()) {
            if (isJdkOrExternalLeafPackage(cls)) {
                break;
            }
            for (Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (!field.trySetAccessible()) {
                    continue;
                }
                try {
                    Object value = field.get(target);
                    Object resolved = resolveValue(entityManager, value, visited, depth);
                    if (resolved != value && !Modifier.isFinal(field.getModifiers())) {
                        field.set(target, resolved);
                    }
                } catch (IllegalAccessException ignored) {
                    // Skip inaccessible fields
                }
            }
        }
    }

    private static boolean isLeafType(Class<?> type) {
        if (type.isPrimitive() || type.isEnum()) {
            return true;
        }
        Package pkg = type.getPackage();
        if (pkg == null) {
            return false;
        }
        String name = pkg.getName();
        return name.startsWith("java.lang")
                || name.startsWith("java.time")
                || name.startsWith("java.math")
                || name.startsWith("org.joml");
    }

    private static boolean isJdkOrExternalLeafPackage(Class<?> type) {
        Package pkg = type.getPackage();
        if (pkg == null) {
            return false;
        }
        String name = pkg.getName();
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("sun.")
                || name.startsWith("jdk.")
                || name.startsWith("org.joml.");
    }
}
