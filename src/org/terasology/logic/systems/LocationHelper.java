package org.terasology.logic.systems;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.components.LocationComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocationHelper {
    
    private LocationHelper() {}

    public static Vector3f localToWorldPos(LocationComponent location) {
        Vector3f result = new Vector3f(location.position);
        while (location != null && location.parent != null) {
            location = location.parent.getComponent(LocationComponent.class);
            if (location != null) {

                result = QuaternionUtil.quatRotate(location.rotation, result, new Vector3f());
                result.add(location.position);
            }
        }
        return result;
    }

    public static Vector3f localToWorldPos(LocationComponent location, Vector3f position) {
        Vector3f result = new Vector3f(position);
        while (location != null && location.parent != null) {
            location = location.parent.getComponent(LocationComponent.class);
            if (location != null) {

                Quat4f inverse = new Quat4f();
                QuaternionUtil.inverse(inverse, location.rotation);
                result = QuaternionUtil.quatRotate(inverse, result, new Vector3f());
                result.add(location.position);
            }
        }
        return result;
    }
    
    public static Vector3f worldToLocalPos(LocationComponent location, Vector3f position) {
        if (location.parent == null)
            return position;
        LocationComponent parent = location.parent.getComponent(LocationComponent.class);
        if (parent != null) {
            worldToLocalPosTransform(parent, new Vector3f(position));

        }
        return position;
    }
    
    private static Vector3f worldToLocalPosTransform(LocationComponent location, Vector3f result) {
        if (location.parent != null) {
            LocationComponent parent = location.parent.getComponent(LocationComponent.class);
            if (parent != null) {
                worldToLocalPosTransform(parent, result);
            }
        }
        result.sub(location.position);
        QuaternionUtil.quatRotate(location.rotation, result, result);
        return result;
    }

}
