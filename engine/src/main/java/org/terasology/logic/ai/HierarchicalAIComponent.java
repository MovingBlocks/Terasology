/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.ai;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.Vector3f;
import org.terasology.utilities.random.Random;

/**
 * @author Esa-Petri Tirkkonen
 */
public final class HierarchicalAIComponent implements Component {

    //how often updates are progressed, handle whit care
    private int updateFrequency;
    private long lastProgressedUpdateAt;

    private Vector3f movementTarget = new Vector3f();

    private long lastChangeOfDirectionAt;
    private long lastChangeOfMovementAt;
    private long lastChangeOfidlingtAt;
    private long lastChangeOfDangerAt;

    //how long ai move
    private int moveUpdateTime = 600;
    // how long ai move to one direction
    private int directionUpdateTime = 300;
    // how long ai idles
    private int idlingUpdateTime = 500;
    // how often danger direction is checked
    private int dangerUpdateTime = 100;

    private boolean dieIfPlayerFar = true;
    private int dieDistance = 2000;

    //define type of AI 
    private boolean hunter;
    private boolean aggressive;
    private boolean wild;
    private boolean flying;

    //AI properties
    // if flying maximum altitude
    private int maxAltitude = 200;
    //AI moves more whit higher values
    private int hectic = 2;
    //AI runs more straight lines whit higher values
    private int straightLined = 2;
    //accurate how accurate AI kills you, values from 0 to up. Do not give negative values something will turn oposite
    private float forgiving = 5f;

    //how well this AI finds player when hunter
    private int playerSense = 30;
    //how close AI comes when hunter
    private int playerdistance = 3;
    //does damage if nearer that this when aggressive
    private int attackDistance = 1;
    //runs if player nearer than this when wild
    private int runDistance = 30;
    //start attack instead running when wild
    private int panicDistance = 10;

    //doing something
    private boolean inDanger;


    //TODO remove this when fight system is ready!!!
    private int damage = 50;
    private int damageFrequency = 500;

    /**
     * determines whether or not the AI should be updated
     *
     * @param tempTime
     */
    public boolean needsUpdate(long tempTime) {
    	return tempTime - this.lastProgressedUpdateAt >= this.updateFrequency;
	}

    /**
     * sets the last progressed updated time
     * 
     * @param timeInMs
     */
	public void setLastProgressedUpdated(long timeInMs) 
	{
		this.lastProgressedUpdateAt = timeInMs;
	}
	
	/**
	 * sets the last change of danger time
	 * 
	 * @param timeInMs
	 */
	public void setLastChangeOfDanger(long timeInMs) 
	{
		this.lastChangeOfDangerAt = timeInMs;
	}

	/**
	 * sets the last change of idiling time
	 * 
	 * @param timeInMs
	 */
	public void setLastChangeOfIdiling(long timeInMs) 
	{
		this.lastChangeOfidlingtAt = timeInMs;
	}
	
	/**
	 * sets the last change of movement time
	 * 
	 * @param timeInMs
	 */
	public void setLastChangeOfMovement(long timeInMs) 
	{
		this.lastChangeOfMovementAt = timeInMs;
	}
	
	/**
	 * sets the last change of direction time
	 * 
	 * @param timeInMs
	 */
	public void setLastChangeOfDirection(long timeInMs) 
	{
		this.lastChangeOfDirectionAt = timeInMs;
	}


	/**
	 * Getter
	 * 
	 * @return
	 */
	public long getMoveUpdateTime() {
		return this.moveUpdateTime; 
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public long getIdlingUpdateTime() 
	{
		return this.idlingUpdateTime;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public long getDangerUpdateTime() {
		return this.dangerUpdateTime;
	}

	/**
	 * Puts the AI in danger
	 */
	public void setInDanger() {
		this.inDanger = true;
	}

	/**
	 * Puts the AI in safe mode
	 */
	public void setSafe() {
		this.inDanger = false;
	}

	/**
	 * Determines whether or not the ai should die for being far from a point
	 * 
	 * @param distanceToPlayer
	 * @return
	 */
	public boolean shouldDieForDistance(double distanceToPlayer) 
	{
		return this.dieIfPlayerFar && distanceToPlayer > this.dieDistance;
	}

	/**
	 * Determines wheter or not the ai will atack
	 * 
	 * @param lastAttack 
	 * @param tempTime 
	 * @param distanceToPlayer 
	 * 
	 * @return
	 */
	public boolean willAtack(double distanceToPlayer, long tempTime, long lastAttack) {
		return this.aggressive && distanceToPlayer <= this.attackDistance && tempTime - lastAttack > this.damageFrequency;
	}
	
	/**
	 * Determines whether or not the ai should hurt for time
	 * 
	 * @param tempTime
	 * @param dangerChangeTime
	 * @return
	 */
	public boolean shouldHurt(long tempTime, long dangerChangeTime) {
		return tempTime - this.lastChangeOfDangerAt > dangerChangeTime;
	}

	/**
	 * Returns whether or not the ai should idle according to time
	 * 
	 * @param tempTime
	 * @param idleChangeTime
	 * @return
	 */
	public boolean shouldIdle(long tempTime, long idleChangeTime) 
	{
		return tempTime - this.lastChangeOfidlingtAt > idleChangeTime;
	}
	
	/**
	 * Returns whether or not the ai should move according to time
	 * 
	 * @param tempTime
	 * @param moveChangeTime
	 * @return
	 */
	public boolean shouldMove(long tempTime, long moveChangeTime) {
		return tempTime - this.lastChangeOfMovementAt > moveChangeTime;
	}
	
	/**
	 * Returns whether or not the ai should change its direction according to time
	 * 
	 * @param tempTime
	 * @param directionChangeTime
	 * @return
	 */
	public boolean shouldChangeDirection(long tempTime, long directionChangeTime) {
		return tempTime - this.lastChangeOfDirectionAt > directionChangeTime;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public int getDamage() {
		return this.damage;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public boolean isInDanger() {
		return this.inDanger;
	}

	public boolean isWild() {
		return this.wild;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public boolean isFlying() 
	{
		return flying;
	}

	/**
	 * Returns a new direction change time which depends on the update time and hectic of the current ai
	 * 
	 * @param random
	 * @return
	 */
	public long getNewDirectionChangeTime(Random random) {
		return newChangeTime(random);
	}
	
	/**
	 * Returns a new move change time which depends on the update time and hectic of the current ai
	 * 
	 * @param random
	 * @return
	 */
	public long getNewMoveChangeTime(Random random) {
		return newChangeTime(random);
	}
	
	/**
	 * Returns a new danger change time which depends on the update time and hectic of the current ai
	 * 
	 * @param random
	 * @return
	 */
	public long getNewDangerChangeTime(Random random) {
		return newChangeTime(random);
	}
	
	/**
	 * Returns a new idiling change time which depends on the update time and hectic of the current ai
	 * 
	 * @param random
	 * @return
	 */
	public long getNewIdilingChangeTime(Random random) 
	{
		return newChangeTime(random);
	}

	private long newChangeTime(Random random)
	{
		return (long) (this.idlingUpdateTime * random.nextDouble() * this.hectic);
	}

	/**
	 * Returns whether or not the ai senses the player
	 * 
	 * @param distanceToPlayer
	 * @return
	 */
	public boolean sensePlayer(double distanceToPlayer) 
	{
		return this.hunter && distanceToPlayer > this.playerdistance && distanceToPlayer < this.playerSense;
	}

	/**
	 * Returns whether or not the ai is in panic(for player's prescence) 
	 * 
	 * @param distanceToPlayer
	 * @return
	 */
	public boolean isInPanic(double distanceToPlayer) 
	{
		return this.wild && distanceToPlayer > this.panicDistance && distanceToPlayer < this.runDistance;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public float forgiving() {
		return this.forgiving;
	}

	
	/**
	 * Getter
	 * 
	 * @return
	 */
	public Vector3f getMovementTarget() {
		return this.movementTarget;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	public float getMaxAltitude() {
		return this.maxAltitude;
	}

}