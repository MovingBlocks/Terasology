package org.terasology.componentSystem.controllers;

import javax.vecmath.Vector3f;

import org.terasology.client.StaticEvent;

public abstract class MovementControlEvent extends StaticEvent {
	abstract public Vector3f getMovementInput();
}

