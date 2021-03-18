// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import com.google.common.collect.Lists;
import org.terasology.engine.context.Context;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.naming.Name;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

/**
 * TODO: Add javadocs
 */
public abstract class ConditionDependentNode extends AbstractNode implements PropertyChangeListener {
    protected WorldRenderer worldRenderer;

    private List<Supplier<Boolean>> conditions = Lists.newArrayList();

    protected ConditionDependentNode(String nodeUri, Name providingModule, Context context) {
        super(nodeUri, providingModule, context);

        worldRenderer = context.get(WorldRenderer.class);
    }

    protected void requiresCondition(Supplier<Boolean> condition) {
        conditions.add(condition);
    }

    private boolean checkConditions() {
        boolean conditionsAreSatisfied = true;
        for (Supplier<Boolean> condition : conditions) {
            conditionsAreSatisfied = conditionsAreSatisfied && condition.get();
        }
        return conditionsAreSatisfied;
    }

    @Override
    public boolean isEnabled() {
        return enabled && checkConditions();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        worldRenderer.requestTaskListRefresh();
    }
}
