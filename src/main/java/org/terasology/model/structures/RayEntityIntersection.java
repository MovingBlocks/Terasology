/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.terasology.model.structures;

import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.model.structures.AABB;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Provides support for ray-AABB intersection tests. Based on
 * RayBlockIntersection Class. This Class is currently a placeholder for testing purposes. 
 * Future collisonchecks will be based on the Mesh/Bounding volumes of Entities.  
 * 
 * @author Andre Herber <andre.herber@yahoo.de>
 */
public class RayEntityIntersection {

	/**
	 * Checks if there is an Intersection between a ray with a given origin, direction and length and an AABB.
	 * 
	 * @param aabb
	 *            AABB to be raytraced
	 * @param origin
	 *            Origin of the intersection ray
	 * @param ray
	 *            Direction of the intersection ray
	 * @param distance
	 *            length of the ray
	 * @return Ray-face-intersection
	 */

	public static boolean intersections(AABB aabb, Vector3d origin,
			Vector3d direction, double distance) {

		boolean result = false;

		/*
		 * Fetch all vertices of the specified AABB.
		 */
		Vector3d[] vertices = aabb.getVertices();

		/*
		 * Test ecah Side of the AABB for an intersection.
		 */

		// Front
		result = executeAABBFaceIntersection(vertices[0], vertices[1], vertices[3], origin, direction, distance);
		if (result) {
			return result;
		}

		// Back
		result = executeAABBFaceIntersection(vertices[4], vertices[7], vertices[5], origin, direction, distance);
		if (result) {
			return result;
		}

		// Left
		result = executeAABBFaceIntersection(vertices[4], vertices[0], vertices[7], origin, direction, distance);
		if (result) {
			return result;
		}

		// Right
		result = executeAABBFaceIntersection(vertices[5], vertices[6], vertices[1], origin, direction, distance);
		if (result) {
			return result;
		}
 
		// Top
		result = executeAABBFaceIntersection(vertices[7], vertices[3], vertices[6], origin, direction, distance);
		if (result) {
			return result;
		}

		// Bottom
		result = executeAABBFaceIntersection(vertices[4], vertices[5], vertices[0], origin, direction, distance);
		if (result) {
			return result;
		}

		return result;
	}


	 /**
	  *  Calculates an intersection with the face of an AABB defined by three points and a given distance
	  * 
	  */

	private static boolean executeAABBFaceIntersection(Vector3d v0, Vector3d v1, Vector3d v2, Vector3d origin, Vector3d ray, double maxDistance) {

		boolean result = false;
		// Calculate the plane to intersect with
		Vector3d a = new Vector3d();
		a.sub(v1, v0);
		Vector3d b = new Vector3d();
		b.sub(v2, v0);
		Vector3d norm = new Vector3d();
		norm.cross(a, b);

		double d = -(norm.x * v0.x + norm.y * v0.y + norm.z * v0.z);

		/**
		 * Calculate the distance on the ray, where the intersection occurs.
		 */
		double t = -(norm.x * origin.x + norm.y * origin.y + norm.z * origin.z + d)
				/ ray.dot(norm);

		// No intersection possible
		if (t < 0 || t > maxDistance)
			return false;

		/**
		 * Calc. the point of intersection.
		 */
		Vector3d intersectPoint = new Vector3d(ray.x * t, ray.y * t, ray.z * t);
		intersectPoint.add(intersectPoint, origin);

		/**
		 * Check if the point lies on block's face.
		 */
		if (intersectPoint.x >= v0.x
				&& intersectPoint.x <= Math.max(v1.x, v2.x)
				&& intersectPoint.y >= v0.y
				&& intersectPoint.y <= Math.max(v1.y, v2.y)
				&& intersectPoint.z >= v0.z
				&& intersectPoint.z <= Math.max(v1.z, v2.z)) {
			System.out.println("point on block face");
			result = true;
			// new Intersection(blockPos, norm, d, t, origin, ray,
			// intersectPoint);
		}

		// Point of intersection was NOT lying on the block's face
		return result;
	}

	public static boolean intersects(EntityRef entity, Vector3d direction) {
		boolean result = false;
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		AABBCollisionComponent aabbCollisionComponent = entity
				.getComponent(AABBCollisionComponent.class);
		AABB aabb = new AABB(new Vector3d(location.getWorldPosition()),
				new Vector3d(aabbCollisionComponent.getExtents()));
		Vector3d hitPlane = aabb.getFirstHitPlane(direction,
				aabb.getPosition(), aabb.getDimensions(), true, true, true);

		if (hitPlane != null) {
			result = true;
		}
		return result;
	}
}
