// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * Delayed action allows you to schedule a one-off event being sent to this entity after the specified delay time
 * passes. The event sent for delayed action to this entity is DelayedActionTriggeredEvent. Upon receiving this event
 * the method should check that it has the expected actionId, as multiple different systems might register a delayed
 * action on the same entity. Therefore, it is crucial that actionIds are unique to the specific operation you want
 * to perform on the entity.
 *
 * Periodic action allows you to schedule an event being sent to this entity periodically at the specified interval.
 * The event sent for periodic action to this entity is PeriodicActionTriggeredEvent. Upon receiving this event
 * the method should check that it has the expected actionId, as multiple different systems might register periodic
 * action on the same entity. Therefore, it is crucial that actionIds are unique to the specific operation you want
 * to perform on the entity.
 *
 * For periodic action, the period starts counting from the invocation of the last PeriodicActionTriggeredEvent, so if
 * there is a delay on the system, there will be larger gaps between invocations.
 *
 */
public interface DelayManager {
    /**
     * Schedules a delayed action (event sent) on the specified entity after a minimum of a delay specified.
     * @param entity Entity that will receive an event.
     * @param actionId ActionId that will be specified in the DelayedActionTriggeredEvent.
     * @param delay Delay after which this entity should be sent an event.
     */
    void addDelayedAction(EntityRef entity, String actionId, long delay);

    /**
     * Checks if the specified entity has an actionId scheduled for it.
     * @param entity Entity that you query about.
     * @param actionId ActionId you want to check existence of on the entity.
     * @return If this entity has the actionId scheduled for it.
     */
    boolean hasDelayedAction(EntityRef entity, String actionId);

    /**
     * Removes the actionId delayed action from the specified entity.
     * @param entity Entity to remove the actionId from.
     * @param actionId ActionId to remove from this entity.
     */
    void cancelDelayedAction(EntityRef entity, String actionId);


    /**
     * Schedules a periodic action (event sent) on the specified entity. First invocation of the event will be called
     * after initialDelay, any subsequent after the specified period.
     * @param entity Entity that will receive an event.
     * @param actionId ActionId that will be specified in the PeriodicActionTriggeredEvent.
     * @param initialDelay Time after which a first invocation of the event will be made.
     * @param period Period after which any subsequent event will be sent, starting from the time last
     *               PeriodicActionTriggeredEvent was sent.
     */
    void addPeriodicAction(EntityRef entity, String actionId, long initialDelay, long period);

    /**
     * Checks if the specified entity has a periodic actionId scheduled for it.
     * @param entity Entity that you query about.
     * @param actionId ActionId you want to check existence of on the entity.
     * @return If this entity has the periodic actionId scheduled for it.
     */
    boolean hasPeriodicAction(EntityRef entity, String actionId);

    /**
     * Removes the actionId periodic action from this specified entity.
     * @param entity Entity to remove the actionId from.
     * @param actionId ActionId to remove from this entity.
     */
    void cancelPeriodicAction(EntityRef entity, String actionId);
}
