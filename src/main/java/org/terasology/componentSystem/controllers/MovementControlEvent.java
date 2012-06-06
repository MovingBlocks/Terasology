package org.terasology.componentSystem.controllers;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;

public abstract class MovementControlEvent extends AbstractEvent {
	abstract public Vector3f getMovementInput();
}

