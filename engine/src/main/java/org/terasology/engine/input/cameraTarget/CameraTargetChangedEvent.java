/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.input.cameraTarget;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 */
public class CameraTargetChangedEvent implements Event {
    private EntityRef oldTarget;
    private EntityRef newTarget;

    public CameraTargetChangedEvent(EntityRef oldTarget, EntityRef newTarget) {
        this.oldTarget = oldTarget;
        this.newTarget = newTarget;
    }

    public EntityRef getOldTarget() {
        return oldTarget;
    }

    public EntityRef getNewTarget() {
        return newTarget;
    }
}
