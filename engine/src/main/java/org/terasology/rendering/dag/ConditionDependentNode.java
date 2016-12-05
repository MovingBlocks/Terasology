/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.dag;

import com.google.common.collect.Lists;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

/**
 * TODO: Add javadocs
 */
public abstract class ConditionDependentNode extends AbstractNode implements PropertyChangeListener {
    private List<Supplier<Boolean>> conditions = Lists.newArrayList();

    @In
    private WorldRenderer worldRenderer;

    protected void requiresCondition(Supplier<Boolean> condition) {
        conditions.add(condition);
        checkConditions(); // TODO: better to remove this in near feature
    }

    private boolean checkConditions() {
        boolean conditionsAreSatisfied = true;
        for (Supplier<Boolean> condition : conditions) {
            conditionsAreSatisfied = conditionsAreSatisfied && condition.get();
        }

        if (conditionsAreSatisfied != isEnabled()) {
            setEnabled(conditionsAreSatisfied);
            // enabling/disabling here means we cannot enable/disable nodes directly:
            // we must always go through the settings.
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean conditionsChanged = checkConditions();
        if (conditionsChanged) {
            worldRenderer.requestTaskListRefresh();
        }
    }
}
