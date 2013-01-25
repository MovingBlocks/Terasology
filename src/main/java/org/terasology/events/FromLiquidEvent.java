/*
 * Copyright 2013 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.events;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.world.block.Block;

/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public class FromLiquidEvent extends AbstractEvent {
	private Block Liquid;
	private Vector3f pos;

	public FromLiquidEvent(Block liquidBlock, Vector3f position){
		Liquid = liquidBlock;
		pos= position;
	}

	/**
	 * @return the liquid
	 */
	public Block getLiquid() {
		return Liquid;
	}

	/**
	 * @return the Position
	 */
	public Vector3f getPosition() {
		return pos;
	}

}
