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
package org.terasology.miniion.utilities;

import static org.lwjgl.opengl.GL11.glColorMask;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Vector3i;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

public class Zone {

	private WorldProvider worldProvider;

	private Vector3i minbounds = new Vector3i(Integer.MAX_VALUE,
			Integer.MAX_VALUE, Integer.MAX_VALUE);
	private Vector3i maxbounds = new Vector3i(Integer.MIN_VALUE,
			Integer.MIN_VALUE, Integer.MIN_VALUE);

	/* CONST */
	private static final int maxselectionbounds = 50;	
    private final Mesh mesh;
    
	private Vector3i startposition;
	private Vector3i endposition;
	private boolean terraformcomplete = false;
	//used to undo zones with unbreakable blocks
	//zone set to delete untill blocks are removed
	private boolean deleted = false;

	public String Name;
	public ZoneType zonetype;
	public int zoneheight;
	public int zonedepth;
	public int zonewidth;

	public Zone() {
		Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(0.0f, 0.0f, 1.0f, 0.25f), 1.005f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f);
        mesh = tessellator.generateMesh();
	}

	public Zone(Vector3i startposition, Vector3i endposition) {
		this();
		this.startposition = startposition;
		this.endposition = endposition;
		calcBounds(startposition);
		if(endposition != null){
			calcBounds(endposition);
		}
	}

	private void calcBounds(Vector3i gridPosition) {
		if (gridPosition.x < minbounds.x) {
			minbounds.x = gridPosition.x;
		}
		if (gridPosition.y < minbounds.y) {
			minbounds.y = gridPosition.y;
		}
		if (gridPosition.z < minbounds.z) {
			minbounds.z = gridPosition.z;
		}

		if (gridPosition.x > maxbounds.x) {
			maxbounds.x = gridPosition.x;
		}
		if (gridPosition.y > maxbounds.y) {
			maxbounds.y = gridPosition.y;
		}
		if (gridPosition.z > maxbounds.z) {
			maxbounds.z = gridPosition.z;
		}
	}

	public void setStartPosition(Vector3i startpos) {
		startposition = startpos;
		calcBounds(startposition);
	}

	public Vector3i getStartPosition() {
		return startposition;
	}

	public void setEndPosition(Vector3i endpos) {
		endposition = endpos;
		calcBounds(endposition);
	}

	public Vector3i getEndPosition() {
		return endposition;
	}

	public Vector3i getMinBounds() {
		return minbounds;
	}

	public Vector3i getMaxBounds() {
		return maxbounds;
	}
	
	public boolean isTerraformComplete(){
		return terraformcomplete;
	}
	
	public void setTerraformComplete(){
		terraformcomplete = true;
	}

	public boolean outofboundselection(){
		boolean retval = false;
		if(startposition != null && endposition != null){
			if(getAbsoluteDiff(minbounds.x, maxbounds.x) > maxselectionbounds){
				retval = true;
			}
			if(getAbsoluteDiff(minbounds.y, maxbounds.y) > maxselectionbounds){
				retval = true;
			}
			if(getAbsoluteDiff(minbounds.z, maxbounds.z) > maxselectionbounds){
				retval = true;
			}
		}
		return retval;
	}

	private int getAbsoluteDiff(int val1, int val2) {
		int width;
		if (val1 == val2) {
			width = 1;
		} else if (val1 < 0) {
			if (val2 < 0 && val2 < val1) {
				width = Math.abs(val2) - Math.abs(val1);
			} else if (val2 < 0 && val2 > val1) {
				width = Math.abs(val1) - Math.abs(val2);
			} else {
				width = Math.abs(val1) + val2;
			}
			width++;
		} else {
			if (val2 > -1 && val2 < val1) {
				width = val1 - val2;
			} else if (val2 > -1 && val2 > val1) {
				width = val2 - val1;
			} else {
				width = Math.abs(val2) + val1;
			}
			width++;
		}
		return width;
	}

	public void render() {
		if(MinionSystem.getNewZone() != null && this.equals(MinionSystem.getNewZone())){
	        ShaderManager.getInstance().enableDefault();
	        worldProvider = CoreRegistry.get(WorldProvider.class);

	        for (int i = 0; i < 2; i++) {
	            if (i == 0) {
	                glColorMask(false, false, false, false);
	            } else {
	                glColorMask(true, true, true, true);
	            }
	            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
	            int camx = (int)cameraPosition.x;
	            int camy = (int)cameraPosition.y;
	            int camz = (int)cameraPosition.z;
	            if(MinionSystem.getNewZone().startposition != null){
	            	Vector3i renderpos = MinionSystem.getNewZone().startposition;
	            	GL11.glPushMatrix();
	            	GL11.glTranslated(renderpos.x - cameraPosition.x, renderpos.y - cameraPosition.y, renderpos.z - cameraPosition.z);
	            	mesh.render();
	            	GL11.glPopMatrix();
	            	if(MinionSystem.getNewZone().endposition != null){
	            		renderpos = MinionSystem.getNewZone().endposition;
	            		GL11.glPushMatrix();
		            	GL11.glTranslated(renderpos.x - cameraPosition.x, renderpos.y - cameraPosition.y, renderpos.z - cameraPosition.z);
		            	mesh.render();
		            	GL11.glPopMatrix();
		            	if(!outofboundselection() && MinionSystem.isSelectionShown()){
			            	for (int x = getMinBounds().x; x <= getMaxBounds().x; x++) {
				    			for (int z = getMinBounds().z; z <= getMaxBounds().z; z++) {
				    				for (int y = getMaxBounds().y; y >= getMinBounds().y; y--) {
				    					Block tmpblock;
				    					if(worldProvider.getBlock(x - camx, y - camy, z- camz) == null){
				    						continue;
				    					}else{
				    						tmpblock = worldProvider.getBlock(x, y, z);
				    					} //!tmpblock.getBlockFamily().getURI().getFamily().matches("air")
				    					if (!tmpblock.isInvisible()) {
				    						if(x==minbounds.x || x == maxbounds.x){
				    							GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
				    						}else
				    						if(z==minbounds.z || z == maxbounds.z){
				    							GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
				    						}else{
					    						GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
					    						break;
				    						}
				    					}
				    				}
				    			}
				    		}
		            	}
	            	}
	            }	           
	        }
		}
    }
}