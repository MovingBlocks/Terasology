/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.characters;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.OnScaleEvent;
import org.terasology.logic.characters.events.ScaleByRequest;
import org.terasology.logic.characters.events.ScaleToRequest;

/**
 * Authority system to finally "accept" scaling requests.
 * <p>
 * Consumes scaling requests (either {@link ScaleByRequest} or {@link ScaleToRequest}) and sends out notifications about
 * the scaling event being "accepted" in form of {@link OnScaleEvent}.
 * Systems should listen to that notification event to act on character scaling.
 *
 * @see ScaleByRequest
 * @see ScaleToRequest
 * @see OnScaleEvent
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CharacterScalingSystem extends BaseComponentSystem {

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onScaleByRequest(ScaleByRequest request, EntityRef entity, CharacterMovementComponent movementComponent) {
        OnScaleEvent scaleEvent = new OnScaleEvent(movementComponent.height, movementComponent.height * request.getFactor());
        entity.send(scaleEvent);
        request.consume();
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onScaleToRequest(ScaleToRequest request, EntityRef entity, CharacterMovementComponent movementComponent) {
        OnScaleEvent scaleEvent = new OnScaleEvent(movementComponent.height, request.getTargetValue());
        entity.send(scaleEvent);
        request.consume();
    }
}
