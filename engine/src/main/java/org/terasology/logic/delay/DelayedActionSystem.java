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
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.registry.In;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class DelayedActionSystem implements UpdateSubscriberSystem {
    @In
    private Time time;

    private SortedSetMultimap<Long, DelayedOperation> delayedOperationsSortedByTime = TreeMultimap.create(Ordering.natural(), Ordering.arbitrary());

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        long currentWorldTime = time.getGameTimeInMs();
        List<DelayedOperation> operationsToInvoke = new LinkedList<>();
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

        for (DelayedOperation delayedOperation : operationsToInvoke) {
            if (delayedOperation.entityRef.exists()) {
                delayedOperation.entityRef.removeComponent(DelayedActionComponent.class);
                delayedOperation.entityRef.send(new DelayedActionTriggeredEvent(delayedOperation.operationId));
            }
        }
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void componentActivated(OnActivatedComponent event, EntityRef entity) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        delayedOperationsSortedByTime.put(delayedComponent.getWorldTime(), new DelayedOperation(entity, delayedComponent.getActionId()));
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void componentDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        delayedOperationsSortedByTime.remove(delayedComponent.getWorldTime(), new DelayedOperation(entity, delayedComponent.getActionId()));
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void getDelayedAction(GetDelayedActionEvent event, EntityRef entity) {
        DelayedActionComponent delayedComponent = entity.getComponent(DelayedActionComponent.class);
        event.setActionId(delayedComponent.getActionId());
    }

    @ReceiveEvent(components = {DelayedActionComponent.class})
    public void cancelDelayedAction(CancelDelayedActionEvent event, EntityRef entity) {
        entity.removeComponent(DelayedActionComponent.class);
    }

    @ReceiveEvent
    public void addDelayedAction(AddDelayedActionEvent event, EntityRef entity) {
        if (entity.hasComponent(DelayedActionComponent.class)) {
            throw new IllegalStateException("This component is already queued for delayed action");
        }
        long scheduleTime = time.getGameTimeInMs() + event.getDelay();
        DelayedActionComponent delayedComponent = new DelayedActionComponent(scheduleTime, event.getActionId());
        entity.saveComponent(delayedComponent);
    }

    private static final class DelayedOperation {
        private String operationId;
        private EntityRef entityRef;

        private DelayedOperation(EntityRef entityRef, String operationId) {
            this.operationId = operationId;
            this.entityRef = entityRef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof DelayedOperation) {
                DelayedOperation other = (DelayedOperation) o;
                return Objects.equals(other.entityRef, entityRef) && Objects.equals(other.operationId, operationId);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(operationId, entityRef);
        }
    }
}
