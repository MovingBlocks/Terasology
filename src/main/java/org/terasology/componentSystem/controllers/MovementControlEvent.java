package org.terasology.componentSystem.controllers;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.game.client.ReusableEvent;

public abstract class MovementControlEvent extends AbstractEvent {
	abstract public Vector3f getMovementInput();
}

