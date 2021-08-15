// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.systems;

import org.terasology.engine.logic.delay.DelayManager;

/**
 * Interface for component systems that needs to be updated every time the engine is updated.
 * <br><br>
 * <p><b>Note:</b> Usage of the UpdateSubscriberSystem interface is discouraged unless truly needed.
 * For most systems receiving the update call on every engine frame is overkill.
 * In most cases it will be sufficient to:
 *   <ul>
 *     <li>
 *       use {@link org.terasology.engine.entitySystem.event.ReceiveEvent}
 *       to update the system on specific events ({@link org.terasology.engine.entitySystem.event.Event}),
 *     </li>
 *     <li>
 *       use {@link DelayManager} to update entities or to call a
 *       system update function at a specific times.
 *     </li>
 *   </ul>
 */
public interface UpdateSubscriberSystem extends ComponentSystem {

    /**
     * Update function for the Component System, which is called each
     * time the engine is updated.
     * @param delta The time (in seconds) since the last engine update.
     */
    void update(float delta);

}
