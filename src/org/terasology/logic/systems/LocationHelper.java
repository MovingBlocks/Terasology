package org.terasology.logic.systems;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.components.LocationComponent;
import org.terasology.model.structures.AABB;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocationHelper {
    
    private LocationHelper() {}

    public static Vector3f localToWorldPos(LocationComponent location) {
        return localToWorldPos(location, location.position, new Vector3f());
    }

    public static Vector3f localToWorldPos(LocationComponent location, Vector3f position) {
        return localToWorldPos(location, position, new Vector3f());
    }

    public static Vector3f localToWorldPos(LocationComponent location, Vector3f position, Vector3f result) {
        result.set(position);
        while (location != null && location.parent != null) {
            location = location.parent.getComponent(LocationComponent.class);
            if (location != null) {

                result.scale(location.scale);
                result = QuaternionUtil.quatRotate(location.rotation, result, result);
                result.add(location.position);
            }
        }
        return result;
    }

    public static float totalScale(LocationComponent location) {
        float result = 1.0f;
        while (location != null) {
            result *= location.scale;
            if (location.parent != null) {
                location = location.parent.getComponent(LocationComponent.class);
            } else {
                location = null;
            }
        }
        return result;
    }
    
    public static Vector3f worldToLocalPos(LocationComponent location, Vector3f position) {
        return worldToLocalPos(location, position, new Vector3f());
    }

    public static Vector3f worldToLocalPos(LocationComponent location, Vector3f position, Vector3f result) {
        result.set(position);
        if (location.parent != null) {
            LocationComponent parent = location.parent.getComponent(LocationComponent.class);
            if (parent != null) {
                worldToLocalPosInternal(parent, result);
            }
        }
        return result;
    }
    
    private static Vector3f worldToLocalPosInternal(LocationComponent location, Vector3f result) {
        if (location.parent != null) {
            LocationComponent parent = location.parent.getComponent(LocationComponent.class);
            if (parent != null) {
                worldToLocalPosInternal(parent, result);
            }
        }
        result.sub(location.position);
        Quat4f inverse = new Quat4f(location.rotation);
        inverse.inverse();
        QuaternionUtil.quatRotate(inverse, result, result);
        result.scale(1.f/location.scale);
        return result;
    }

}
