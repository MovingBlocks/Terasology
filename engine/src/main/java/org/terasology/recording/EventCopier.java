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
package org.terasology.recording;

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.binds.movement.JumpButton;

public class EventCopier {


    public static Event copyEvent (Event e) {
        if(e instanceof PlaySoundEvent) {
            return e;
        } else if ( e instanceof JumpButton) {
            JumpButton originalEvent = (JumpButton) e;
            printJumpEventData(originalEvent);
            JumpButton newEvent = new JumpButton();
            newEvent.prepare(originalEvent.getId(), originalEvent.getState(), originalEvent.getDelta());
            newEvent.setTargetInfo(originalEvent.getTarget(),
                    originalEvent.getTargetBlockPosition(),
                    originalEvent.getHitPosition(),
                    originalEvent.getHitNormal());
            return newEvent;
        } else {
            return null;
        }
    }

    private static void printJumpEventData(JumpButton e) {
        System.out.println("Jump data:");
        System.out.println("Delta: " + e.getDelta());
        System.out.println("HitNormal: " + e.getHitNormal());
        System.out.println("HitPosition: " + e.getHitPosition());
        System.out.println("Id: " + e.getId());
        System.out.println("State: " + e.getState());
        System.out.println("Target: " + e.getTarget());
        System.out.println("TargetBlockPosition: " + e.getTargetBlockPosition());
    }
}
