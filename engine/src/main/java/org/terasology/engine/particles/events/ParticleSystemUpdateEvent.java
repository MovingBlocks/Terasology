// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.particles.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.module.sandbox.API;

/**
 * Fired to notify the ParticleSystemManager that a system needs to be reconfigured.
 * For use when adding/removing Generators or Affectors.
 */
@API
public class ParticleSystemUpdateEvent implements Event {

}
