/*
 * Copyright 2018 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.input.events.InputEvent;
import org.terasology.input.events.KeyUpEvent;
import org.terasology.input.events.KeyRepeatEvent;
import org.terasology.input.events.KeyDownEvent;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.logic.characters.CharacterMoveInputEvent;
import org.terasology.logic.characters.GetMaxSpeedEvent;
import org.terasology.logic.characters.events.AttackEvent;

import java.lang.reflect.InvocationTargetException;

/**
 * Responsible for making deep copies of the events to be recorded. It is necessary to record copies instead of the
 * events themselves since the events can change during the game.
 */
class EventCopier {

    private static final Logger logger = LoggerFactory.getLogger(EventCopier.class);


    public EventCopier() {

    }

    public Event copyEvent(Event toBeCopied) {
        if (toBeCopied instanceof PlaySoundEvent) {
            return toBeCopied;
        } else if (toBeCopied instanceof BindButtonEvent) {
            BindButtonEvent originalEvent = (BindButtonEvent) toBeCopied;
            BindButtonEvent newEvent = (BindButtonEvent) createNewBindEvent(originalEvent);
            newEvent.prepare(originalEvent.getId(), originalEvent.getState(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof KeyEvent) {
            KeyEvent originalEvent = (KeyEvent) toBeCopied;
            KeyEvent newEvent = createNewKeyEvent(originalEvent);
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof BindAxisEvent) {
            BindAxisEvent originalEvent = (BindAxisEvent) toBeCopied;
            BindAxisEvent newEvent = (BindAxisEvent) createNewBindEvent(originalEvent);
            newEvent.prepare(originalEvent.getId(), (float) originalEvent.getValue(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof MouseAxisEvent) {
            MouseAxisEvent originalEvent = (MouseAxisEvent) toBeCopied;
            MouseAxisEvent newEvent = createNewMouseAxisEvent(originalEvent);
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof CameraTargetChangedEvent) {
            CameraTargetChangedEvent originalEvent = (CameraTargetChangedEvent) toBeCopied;
            return new CameraTargetChangedEvent(originalEvent.getOldTarget(), originalEvent.getNewTarget());
        } else if (toBeCopied instanceof CharacterMoveInputEvent) {
            CharacterMoveInputEvent originalEvent = (CharacterMoveInputEvent) toBeCopied;
            return  new CharacterMoveInputEvent(originalEvent.getSequenceNumber(), originalEvent.getPitch(),
                    originalEvent.getYaw(), originalEvent.getMovementDirection(), originalEvent.isRunning(),
                    originalEvent.isCrouching(), originalEvent.isJumpRequested(), originalEvent.getDeltaMs());
        } else if (toBeCopied instanceof MouseButtonEvent) {
            MouseButtonEvent originalEvent = (MouseButtonEvent) toBeCopied;
            MouseButtonEvent newEvent = new MouseButtonEvent(originalEvent.getButton(), originalEvent.getState(), originalEvent.getDelta());
            newEvent.setMousePosition(originalEvent.getMousePosition());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof MouseWheelEvent) {
            MouseWheelEvent originalEvent = (MouseWheelEvent) toBeCopied;
            MouseWheelEvent newEvent = new MouseWheelEvent(originalEvent.getMousePosition(), originalEvent.getWheelTurns(), originalEvent.getDelta());
            inputEventSetup(newEvent, originalEvent);
            return newEvent;
        } else if (toBeCopied instanceof GetMaxSpeedEvent) {
            GetMaxSpeedEvent originalEvent = (GetMaxSpeedEvent) toBeCopied;
            GetMaxSpeedEvent newEvent = new GetMaxSpeedEvent(originalEvent.getBaseValue(), originalEvent.getMovementMode());
            newEvent.setModifiers(originalEvent.getModifiers());
            newEvent.setMultipliers(originalEvent.getMultipliers());
            newEvent.setPostModifiers(originalEvent.getPostModifiers());
            return newEvent;
        } else if (toBeCopied instanceof AttackEvent) {
            AttackEvent originalEvent = (AttackEvent) toBeCopied;
            AttackEvent  newEvent = new AttackEvent(originalEvent.getInstigator(), originalEvent.getDirectCause());
            return newEvent;
        } else {
            return null;
        }
    }

    private void inputEventSetup(InputEvent newEvent, InputEvent originalEvent) {
        newEvent.setTargetInfo(originalEvent.getTarget(),
                originalEvent.getTargetBlockPosition(),
                originalEvent.getHitPosition(),
                originalEvent.getHitNormal());
    }


    private InputEvent createNewBindEvent(InputEvent originalEvent) {
        try {
            return originalEvent.getClass()
                    .getConstructor()
                    .newInstance();

        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException
                | InstantiationException
                exception) {
            logger.error("ERROR!!! Event not Identified: " + originalEvent.toString(), exception);
        }
        return null;
    }

    private KeyEvent createNewKeyEvent(KeyEvent originalEvent) {
        KeyEvent newEvent = null;
        Class eventClass = originalEvent.getClass();

        if (eventClass.equals(KeyDownEvent.class)) {
            newEvent = KeyDownEvent.createCopy((KeyDownEvent) originalEvent);
        } else if (eventClass.equals(KeyRepeatEvent.class)) {
            newEvent = KeyRepeatEvent.createCopy((KeyRepeatEvent) originalEvent);
        } else if (eventClass.equals(KeyUpEvent.class)) {
            newEvent = KeyUpEvent.createCopy((KeyUpEvent) originalEvent);
        } else {
            logger.error("ERROR!!! Event not Identified");
        }
        return newEvent;
    }

    private MouseAxisEvent createNewMouseAxisEvent(MouseAxisEvent originalEvent) {
        return MouseAxisEvent.createCopy(originalEvent);
    }

}
