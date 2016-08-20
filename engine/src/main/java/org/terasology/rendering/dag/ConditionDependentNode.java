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

import com.google.api.client.util.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

/**
 * TODO: Add javadocs
 */
public abstract class ConditionDependentNode extends AbstractNode implements PropertyChangeListener {
    private List<Supplier<Boolean>> conditions = Lists.newArrayList();

    protected void requiresCondition(Supplier<Boolean> condition) {
        conditions.add(condition);
        checkConditions(); // TODO: better to remove this in near feature
    }

    private void checkConditions() {
        boolean areSatisfied = true;
        for (Supplier<Boolean> condition : conditions) {
            areSatisfied = areSatisfied && condition.get();
        }
        setEnabled(areSatisfied);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        checkConditions();
        refreshTaskList();
    }
}
