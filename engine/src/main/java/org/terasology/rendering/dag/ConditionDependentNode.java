/*
 * Copyright 2017 MovingBlocks
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

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

/**
 * TODO: Add javadocs
 */
public abstract class ConditionDependentNode extends AbstractNode {
    private List<Supplier<Boolean>> conditions = Lists.newArrayList();

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
}
