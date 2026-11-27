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

import java.util.List;

/**
 * An {@link EntityRef} for an id not registered yet. Retries the lookup on every delegated call until the
 * target, deserialized later in the same batch, exists.
 */
public class ForwardReferenceEntityRef extends EntityRef {
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
     * {@link ForwardReferenceEntityRef} field still stored in a component with the now-resolvable
     * real {@link EntityRef}. This eliminates the per-call {@code exists()} re-lookup overhead
     * that the proxy would otherwise carry for the lifetime of the reference.
     *
     * <p>Call this once at the end of each batch-restore site (EntityRestorer, GlobalStoreLoader,
     * WorldSerializerImpl) after all entities in the session have been deserialized.</p>
     *
     * @param entityManager the entity manager whose component library is used for field inspection
     * @param entities      the set of entities created in this restore session
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void resolveForwardReferences(EngineEntityManager entityManager, Iterable<EntityRef> entities) {
        for (EntityRef entity : entities) {
            if (!entity.exists()) {
                continue;
            }
            for (Component component : entity.iterateComponents()) {
                ComponentMetadata metadata = entityManager.getComponentLibrary().getMetadata(component);
                if (metadata == null) {
                    continue;
                }
                boolean dirty = false;
                for (ComponentFieldMetadata<?, ?> field : (Iterable<ComponentFieldMetadata<?, ?>>) metadata.getFields()) {
                    if (!EntityRef.class.isAssignableFrom(field.getType())) {
                        continue;
                    }
                    Object value = field.getValue(component);
                    if (value instanceof ForwardReferenceEntityRef) {
                        ForwardReferenceEntityRef fref = (ForwardReferenceEntityRef) value;
                        EntityRef real = entityManager.getEntity(fref.id);
                        if (real.exists()) {
                            ((ComponentFieldMetadata) field).setValue(component, real);
                            dirty = true;
                        }
                    }
                }
                if (dirty) {
                    entity.saveComponent(component);
                }
            }
        }
    }
}
