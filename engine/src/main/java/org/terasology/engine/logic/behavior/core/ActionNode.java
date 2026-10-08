// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.nui.properties.OneOfProviderFactory;
import org.terasology.nui.properties.PropertyProvider;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.InjectionHelper;

/**
 * An action node uses a associated Action to control the result state.
 * It is a leaf node - it cannot have any children.
 */
public class ActionNode implements BehaviorNode {
    private static final Logger logger = LoggerFactory.getLogger(ActionNode.class);
    protected Action action;

    public ActionNode() {
    }

    public ActionNode(Action action) {
        this.action = action;
    }

    @Override
    public PropertyProvider getProperties() {
        // TODO: The CoreRegistry usage came from the previous code.
        PropertyProvider provider = new PropertyProvider(CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(OneOfProviderFactory.class));
        provider.createProperties(action);
        return provider;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public String getName() {
        return action != null ? action.getName() : "action";
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    @Override
    public void construct(Actor actor) {
        if (action != null) {
            try {
                action.construct(actor);
            } catch (Exception e) {
                logger.debug("Exception while running construct() of action {} from entity {}: ", action, actor.getEntity(), e); //NOPMD
            }
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        if (action != null) {
            try {
                return action.modify(actor, BehaviorState.UNDEFINED);
            } catch (Exception e) {
                logger.debug("Exception while running action {} from entity {}: ", action, actor.getEntity(), e); //NOPMD
                // TODO maybe returning UNDEFINED would be more fitting?
                return BehaviorState.FAILURE;
            }

        }
        return BehaviorState.UNDEFINED;
    }

    @Override
    public void destruct(Actor actor) {
        if (action != null) {
            action.destruct(actor);
        }
    }

    @Override
    public BehaviorNode deepCopy() {
        reinjectAction();
        return new ActionNode(action);
    }

    /**
     * Re-runs {@code @In} field injection on the shared {@link #action} instance.
     * <p>
     * {@link BehaviorTreeBuilder} already injects {@link #action} once, when the tree's JSON is first
     * deserialized - but that can happen as early as {@code LoadPrefabs}, which runs before
     * {@code RegisterSystems} has shared anything into {@link CoreRegistry} yet, leaving {@code @In}
     * fields silently null (see #5004). {@code deepCopy()} instead runs once an {@link Actor} actually
     * exists to attach the tree to, which is always well after the engine has finished loading, so
     * re-injecting here is a safe, cheap way to guarantee valid values without reordering the load
     * sequence itself. {@link InjectionHelper#inject(Object)} only overwrites a field when it finds a
     * non-null value, so repeating this on every copy is idempotent.
     */
    protected void reinjectAction() {
        if (action != null) {
            InjectionHelper.inject(action);
        }
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("ActionNodes can't have any children");
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("ActionNodes can't have any children");
    }

    @Override
    public BehaviorNode removeChild(int index) {
        throw new IllegalArgumentException("ActionNodes can't have any children");
    }

    @Override
    public BehaviorNode getChild(int index) {
        throw new IllegalArgumentException("ActionNodes can't have any children");
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }

}
