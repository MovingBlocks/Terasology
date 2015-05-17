package org.terasology.physics.bullet;

import java.util.HashMap;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CapsuleShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.CylinderShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.util.ObjectArrayList;

/**
 * Flyweight factory (for shapes used in BulletPhysics' getShapeFor method).
 *
 * @author FrancoPinoC
 */


public class ShapeFactory {
	private HashMap<Vector3f, BoxShape> boxPool = new HashMap<Vector3f, BoxShape>();
	private HashMap<Float, SphereShape> spherePool = new HashMap<Float, SphereShape>();
	private HashMap<Vector2f, CapsuleShape> capsulePool = new HashMap<Vector2f, CapsuleShape>();
	private HashMap<Vector3f, CylinderShape> cylinderPool = new HashMap<Vector3f, CylinderShape>();
	private HashMap<ObjectArrayList<Vector3f>, ConvexHullShape> convexHullPool =
			new HashMap<ObjectArrayList<Vector3f>, ConvexHullShape>();
	
	public BoxShape getBoxShape(Vector3f scaleVector){
		BoxShape ret = boxPool.get(scaleVector);
		if(ret==null){
			ret = new BoxShape(scaleVector);
			boxPool.put(scaleVector, ret);
		}
		return ret;
	}
	
	public SphereShape getSphereShape(float radius){
		SphereShape ret = spherePool.get(radius);
		if(ret==null){
			ret = new SphereShape(radius);
			spherePool.put(radius, ret);
		}
		return ret;
	}
	
	public CapsuleShape getCapsuleShape(float radius, float height){
		Vector2f key = new Vector2f(radius, height);
		CapsuleShape ret = capsulePool.get(key);
		if(ret==null){
			ret = new CapsuleShape(radius, height);
			capsulePool.put(key, ret);
		}
		return ret;
	}
	
	public CylinderShape getCylinderShape(Vector3f scalesVector){
		CylinderShape ret = cylinderPool.get(scalesVector);
		if(ret==null){
			ret = new CylinderShape(scalesVector);
			cylinderPool.put(scalesVector, ret);
		}
		return ret;
	}
	
	public ConvexHullShape getConvexHullShape(ObjectArrayList<Vector3f> verts){
		ConvexHullShape ret = convexHullPool.get(verts);
		if(ret==null){
			ret = new ConvexHullShape(verts);
			convexHullPool.put(verts, ret);
		}
		return ret;
	}
}
