/*
 * Copyright 2012 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.components;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.Component;

/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
public final class HierarchicalAIComponent implements Component {
	
	//how often updates are progressed, handle whit care
    public int updateFrequency=0;
	public long lastProgressedUpdateAt = 0;
	
	public Vector3f movementTarget = new Vector3f();
	
    public long lastChangeOfDirectionAt = 0;
    public long lastChangeOfMovementAt = 0;
    public long lastChangeOfidlingtAt = 0;
    public long lastChangeOfDangerAt = 0;
        
    //how long ai move
    public int moveUpdateTime=600;
    // how long ai move to one direction
    public int directionUpdateTime=300;
    // how long ai idles
    public int idlingUpdateTime=500;
    // how often danger direction is checked
    public int dangerUpdateTime=100;
    
    public boolean dieIfPlayerFar=true;
    public int dieDistance=2000;

    //define type of AI 
    public boolean hunter = false;
    public boolean aggressive = false;
    public boolean wild = false;
    public boolean flying=false;
    
    //AI properties
    // if flying maximum altitude
    public int maxAltitude=200;
    //AI moves more whit higher values
    public int hectic=2;
    //AI runs more straight lines whit higher values
    public int straightLined=2;
    //accurate how accurate AI kills you, values from 0 to up. Do not give negative values something will turn oposite
    public float forgiving=5f;

    //how well this AI finds player when hunter
    public int playerSense = 30;
    //how close AI comes when hunter
    public int playerdistance = 3;
    //does damage if nearer that this when aggressive
    public int attackDistance =1;
    //runs if player nearer than this when wild
    public int runDistance=30;
    //start attack instead running when wild
    public int panicDistance=10;
    
    //doing something
    public boolean inDanger = false;
    

    
    //TODO remove this when fight system is ready!!!
    public int damage=50;
    public int damageFrequency=500;
    
    
    
}
