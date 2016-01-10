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
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = DelayManager.class)
public class DelayedActionSystem extends BaseComponentSystem implements UpdateSubscriberSystem, DelayManager {
    @In
    private Time time;

    private SortedSetMultimap<Long, EntityRef> delayedOperationsSortedByTime = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());
    private SortedSetMultimap<Long, EntityRef> periodicOperationsSortedByTime = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());

    @Override
    public void update(float delta) {
        final long currentWorldTime = time.getGameTimeInMs();
        invokeDelayedOperations(currentWorldTime);
        invokePeriodicOperations(currentWorldTime);
    }

    private void invokeDelayedOperations(long currentWorldTime) {
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

        operationsToInvoke.stream().filter(EntityRef::exists).forEach(delayedEntity -> {
            final DelayedActionComponent delayedActions = delayedEntity.getComponent(DelayedActionComponent.class);

            final Set<String> actionIds = delayedActions.removeActionsUpTo(currentWorldTime);
            saveOrRemoveComponent(delayedEntity, delayedActions);

            if (!delayedActions.isEmpty()) {
                delayedOperationsSortedByTime.put(delayedActions.getLowestWakeUp(), delayedEntity);
            }

            for (String actionId : actionIds) {
                delayedEntity.send(new DelayedActionTriggeredEvent(actionId));
            }
        });
    }

    private void invokePeriodicOperations(long currentWorldTime) {
        List<EntityRef> operationsToInvoke = new LinkedList<>();
        Iterator<Long> scheduledOperationsIterator = periodicOperationsSortedByTime.keySet().iterator();
        long processedTime;
        while (scheduledOperationsIterator.hasNext()) {
            processedTime = scheduledOperationsIterator.next();
            if (processedTime > currentWorldTime) {
                break;
            }
            operationsToInvoke.addAll(periodicOperationsSortedByTime.get(processedTime));
            scheduledOperationsIterator.remove();
        }

        operationsToInvoke.stream().filter(EntityRef::exists).forEach(periodicEntity -> {
            final PeriodicActionComponent periodicActionComponent = periodicEntity.getComponent(PeriodicActionComponent.class);

            final Set<String> actionIds = periodicActionComponent.getTriggeredActionsAndReschedule(currentWorldTime);
            saveOrRemoveComponent(periodicEntity, periodicActionComponent);

            if (!periodicActionComponent.isEmpty()) {
                periodicOperationsSortedByTime.put(periodicActionComponent.getLowestWakeUp(), periodicEntity);
            }

            for (String actionId : actionIds) {
                periodicEntity.send(new PeriodicActionTriggeredEvent(actionId));
            }
        });
    }

    @ReceiveEvent
    public void delayedComponentActivated(OnActivatedComponent event, EntityRef entity, DelayedActionComponent delayedActionComponent) {
        delayedOperationsSortedByTime.put(delayedActionComponent.getLowestWakeUp(), entity);
    }

    @ReceiveEvent
    public void periodicComponentActivated(OnActivatedComponent event, EntityRef entity, PeriodicActionComponent periodicActionComponent) {
        periodicOperationsSortedByTime.put(periodicActionComponent.getLowestWakeUp(), entity);
    }

    @ReceiveEvent
    public void delayedComponentDeactivated(BeforeDeactivateComponent event, EntityRef entity, DelayedActionComponent delayedActionComponent) {
        delayedOperationsSortedByTime.remove(delayedActionComponent.getLowestWakeUp(), entity);
    }

    @ReceiveEvent
    public void periodicComponentDeactivated(BeforeDeactivateComponent event, EntityRef entity, PeriodicActionComponent periodicActionComponent) {
        delayedOperationsSortedByTime.remove(periodicActionComponent.getLowestWakeUp(), entity);
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
    public void addPeriodicAction(EntityRef entity, String actionId, long initialDelay, long period) {
        long scheduleTime = time.getGameTimeInMs() + initialDelay;

        PeriodicActionComponent periodicActionComponent = entity.getComponent(PeriodicActionComponent.class);
        if (periodicActionComponent != null) {
            final long oldWakeUp = periodicActionComponent.getLowestWakeUp();
            periodicActionComponent.addScheduledActionId(actionId, scheduleTime, period);
            entity.saveComponent(periodicActionComponent);
            final long newWakeUp = periodicActionComponent.getLowestWakeUp();
            if (oldWakeUp < newWakeUp) {
                periodicOperationsSortedByTime.remove(oldWakeUp, entity);
                periodicOperationsSortedByTime.put(newWakeUp, entity);
            }
        } else {
            periodicActionComponent = new PeriodicActionComponent();
            periodicActionComponent.addScheduledActionId(actionId, scheduleTime, period);
            entity.addComponent(periodicActionComponent);
        }
    }

    @Override
    public void cancelDelayedAction(EntityRef entity, String actionId) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        long oldWakeUp = delayedComponent.getLowestWakeUp();
        delayedComponent.removeActionId(actionId);
        long newWakeUp = delayedComponent.getLowestWakeUp();
        if (!delayedComponent.isEmpty() && oldWakeUp < newWakeUp) {
            delayedOperationsSortedByTime.remove(oldWakeUp, entity);
            delayedOperationsSortedByTime.put(newWakeUp, entity);
        } else if (delayedComponent.isEmpty()) {
            delayedOperationsSortedByTime.remove(oldWakeUp, entity);
        }
        saveOrRemoveComponent(entity, delayedComponent);
    }

    @Override
    public void cancelPeriodicAction(EntityRef entity, String actionId) {
        PeriodicActionComponent periodicActionComponent = entity.getComponent(PeriodicActionComponent.class);
        long oldWakeUp = periodicActionComponent.getLowestWakeUp();
        periodicActionComponent.removeScheduledActionId(actionId);
        long newWakeUp = periodicActionComponent.getLowestWakeUp();
        if (!periodicActionComponent.isEmpty() && oldWakeUp < newWakeUp) {
            periodicOperationsSortedByTime.remove(oldWakeUp, entity);
            periodicOperationsSortedByTime.put(newWakeUp, entity);
        } else if (periodicActionComponent.isEmpty()) {
            periodicOperationsSortedByTime.remove(oldWakeUp, entity);
        }
        saveOrRemoveComponent(entity, periodicActionComponent);
    }

    @Override
    public boolean hasDelayedAction(EntityRef entity, String actionId) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        return delayedComponent != null && delayedComponent.containsActionId(actionId);
    }

    @Override
    public boolean hasPeriodicAction(EntityRef entity, String actionId) {
        PeriodicActionComponent periodicActionComponent = entity.getComponent(PeriodicActionComponent.class);
        return periodicActionComponent != null && periodicActionComponent.containsActionId(actionId);
    }

    private void saveOrRemoveComponent(EntityRef delayedEntity, DelayedActionComponent delayedActionComponent) {
        if (delayedActionComponent.isEmpty()) {
            delayedEntity.removeComponent(DelayedActionComponent.class);
        } else {
            delayedEntity.saveComponent(delayedActionComponent);
        }
    }

    private void saveOrRemoveComponent(EntityRef periodicEntity, PeriodicActionComponent periodicActionComponent) {
        if (periodicActionComponent.isEmpty()) {
            periodicEntity.removeComponent(PeriodicActionComponent.class);
        } else {
            periodicEntity.saveComponent(periodicActionComponent);
        }
    }

    // Deprecated methods
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
}
