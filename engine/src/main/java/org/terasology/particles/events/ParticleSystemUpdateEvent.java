/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.particles.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.module.sandbox.API;

/**
 * Fired to notify the ParticleSystemManager that a system needs to be reconfigured.
 * For use when adding/removing Generators or Affectors.
 */
@API
public class ParticleSystemUpdateEvent implements Event {

}
