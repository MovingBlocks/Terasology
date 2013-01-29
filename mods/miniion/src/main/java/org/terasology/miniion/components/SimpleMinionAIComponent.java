/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.miniion.components;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.Component;

/**
 * @author Overdhose copied from SimpleAIComponent, only movementtarget is
 *         really used
 */
public final class SimpleMinionAIComponent implements Component {

	public long lastChangeOfDirectionAt = 0;
	public Vector3f lastPosition = null;
	public double previousdistanceToTarget = Double.NEGATIVE_INFINITY;

	public long lastAttacktime = 0;
	public long lastDistancecheck = 0;
	public long lastPathtime = 0;
	public long lastHungerCheck = 0;
	public int patrolCounter = 0;
	public int craftprogress = 0;

	public Vector3f movementTarget = new Vector3f();
	public Vector3f previousTarget = new Vector3f();

	public List<Vector3f> movementTargets = new ArrayList<Vector3f>();
	public List<Vector3f> gatherTargets = new ArrayList<Vector3f>();
	public List<Vector3f> patrolTargets = new ArrayList<Vector3f>();
	public List<Vector3f> pathTargets = new ArrayList<Vector3f>();

	public boolean followingPlayer = true;
	public boolean locked = false;

	public void ClearCommands() {
		movementTargets.removeAll(movementTargets);
		gatherTargets.removeAll(gatherTargets);
		patrolTargets.removeAll(patrolTargets);
	}

}
