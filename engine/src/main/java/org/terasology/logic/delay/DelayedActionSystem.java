/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.delay;

import com.google.common.collect.Ordering;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = DelayManager.class)
public class DelayedActionSystem extends BaseComponentSystem implements UpdateSubscriberSystem, DelayManager {
    @In
    private Time time;

    private SortedSetMultimap<Long, EntityRef> delayedOperationsSortedByTime = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());

    @Override
    public void update(float delta) {
        final long currentWorldTime = time.getGameTimeInMs();
        List<EntityRef> operationsToInvoke = new LinkedList<>();
        Iterator<Long> scheduledOperationsIterator = delayedOperationsSortedByTime.keySet().iterator();
        long processedTime;
        while (scheduledOperationsIterator.hasNext()) {
            processedTime = scheduledOperationsIterator.next();
            if (processedTime > currentWorldTime) {
                break;
            }
            operationsToInvoke.addAll(delayedOperationsSortedByTime.get(processedTime));
            scheduledOperationsIterator.remove();
        }

        for (EntityRef delayedEntity : operationsToInvoke) {
            if (delayedEntity.exists()) {
                final DelayedActionComponent delayedActions = delayedEntity.getComponent(DelayedActionComponent.class);

                final Set<String> actionIds = delayedActions.removeActionsUpTo(currentWorldTime);
                saveOrRemoveComponent(delayedEntity, delayedActions);

                if (!delayedActions.isEmpty()) {
                    delayedOperationsSortedByTime.put(delayedActions.getLowestWakeUp(), delayedEntity);
                }

                for (String actionId : actionIds) {
                    delayedEntity.send(new DelayedActionTriggeredEvent(actionId));
                }
            }
        }
    }

    private void saveOrRemoveComponent(EntityRef delayedEntity, DelayedActionComponent delayedActionComponent) {
        if (delayedActionComponent.isEmpty()) {
            delayedEntity.removeComponent(DelayedActionComponent.class);
        } else {
            delayedEntity.saveComponent(delayedActionComponent);
        }
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void componentActivated(OnActivatedComponent event, EntityRef entity) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        delayedOperationsSortedByTime.put(delayedComponent.getLowestWakeUp(), entity);
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void componentDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        delayedOperationsSortedByTime.remove(delayedComponent.getLowestWakeUp(), entity);
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void getDelayedAction(HasDelayedActionEvent event, EntityRef entity) {
        event.setResult(hasDelayedAction(entity, event.getActionId()));
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void cancelDelayedAction(CancelDelayedActionEvent event, EntityRef entity) {
        cancelDelayedAction(entity, event.getActionId());
    }

    @ReceiveEvent
    public void addDelayedAction(AddDelayedActionEvent event, EntityRef entity) {
        addDelayedAction(entity, event.getActionId(), event.getDelay());
    }

    @Override
    public void addDelayedAction(EntityRef entity, String actionId, long delay) {
        long scheduleTime = time.getGameTimeInMs() + delay;

        DelayedActionComponent delayedActionComponent = entity.getComponent(DelayedActionComponent.class);
        if (delayedActionComponent != null) {
            final long oldWakeUp = delayedActionComponent.getLowestWakeUp();
            delayedActionComponent.addActionId(actionId, scheduleTime);
            entity.saveComponent(delayedActionComponent);
            final long newWakeUp = delayedActionComponent.getLowestWakeUp();
            if (oldWakeUp < newWakeUp) {
                delayedOperationsSortedByTime.remove(oldWakeUp, entity);
                delayedOperationsSortedByTime.put(newWakeUp, entity);
            }
        } else {
            delayedActionComponent = new DelayedActionComponent();
            delayedActionComponent.addActionId(actionId, scheduleTime);
            entity.addComponent(delayedActionComponent);
        }
    }

    @Override
    public void cancelDelayedAction(EntityRef entity, String actionId) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        delayedComponent.removeActionId(actionId);
        saveOrRemoveComponent(entity, delayedComponent);
    }

    @Override
    public boolean hasDelayedAction(EntityRef entity, String actionId) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        if (delayedComponent != null) {
            return delayedComponent.getActionIdsWakeUp().containsKey(actionId);
        } else {
            return false;
        }
    }

}
