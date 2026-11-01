// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * This package provides physics support for Terasology -
 * ray tracing, character movement, collision detection and rigid bodies.
 * <br><br>
 * The core of the physics support is the PhysicsEngine class. This
 * interface should be available from the CoreRegistry, using
 * CoreRegistry.get(PhysicsEngine.class). From there most of the physics
 * behaviour can be accessed.
 * <br><br>
 * Besides the PhysicsEngine and the interfaces and classes returned by it or
 * required by it, there are three other groups of classes.
 * <ul>
 * <li>The first groups contains the Components used by the
 * PhysicsEngine. This groups can be found in the "components" sub-package. The
 * Physics engine requires that entities have certain types of components for
 * certain behaviour. For example, to create a rigid body, you must provide the
 * engine with an entity that has a RigidBodyComponent and LocationComponent.</li>
 * <li>The second group contains the physics related events and the
 * PhysicsSystem class. This group can be found in the "events" sub-package.
 * The PhysicsSystem class is a bridge between the event system of Terasology and the PhysicsEngine
 * interface. It catches of any events that should alter the physics engine and
 * converts them into calls to the PhysicsEngine interface.</li>
 * <li>The last group is the implementation of the various interfaces. At the
 * moment of writing this documentation only one implementation exists, the
 * bullet physics engine implementation that uses TeraBullet - a customised version of JBullet.
 * This implementation can be found in the "bullet" sub-package.</li>
 * </ul>
 */
@API package org.terasology.engine.physics;

import org.terasology.context.annotation.API;
