// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.recording;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.cameraTarget.CameraTargetChangedEvent;
import org.terasology.engine.input.events.InputEvent;
import org.terasology.engine.input.events.KeyDownEvent;
import org.terasology.engine.input.events.KeyEvent;
import org.terasology.engine.input.events.KeyRepeatEvent;
import org.terasology.engine.input.events.KeyUpEvent;
import org.terasology.engine.input.events.MouseAxisEvent;
import org.terasology.engine.input.events.MouseButtonEvent;
import org.terasology.engine.input.events.MouseWheelEvent;
import org.terasology.engine.logic.characters.CharacterMoveInputEvent;
import org.terasology.engine.logic.characters.GetMaxSpeedEvent;
import org.terasology.engine.logic.characters.events.AttackEvent;
import org.terasology.gestalt.entitysystem.event.Event;

import java.lang.reflect.InvocationTargetException;

/**
 * Responsible for making deep copies of the events to be recorded. It is necessary to record copies instead of the
 * events themselves since the events can change during the game.
 */
class EventCopier {

    private static final Logger logger = LoggerFactory.getLogger(EventCopier.class);


    EventCopier() {

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
                    originalEvent.isCrouching(), originalEvent.isJumping(), originalEvent.getDeltaMs());
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
            return new AttackEvent(originalEvent.getInstigator(), originalEvent.getDirectCause());
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
            logger.error("ERROR!!! Event not Identified: {}", originalEvent, exception);
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
