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
package org.terasology.components.world;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.entitySystem.metadata.core.ListTypeHandler;

import java.util.Collection;
import java.util.List;

/**
 * Component represent the location and facing of an entity in the world
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class LocationComponent implements Component {
    // Standard position/rotation
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f(0, 0, 0, 1);
    private float scale = 1.0f;

    // Relative to
    private EntityRef parent = EntityRef.NULL;
    private List<EntityRef> children = Lists.newArrayList();

    public LocationComponent() {
    }

    public LocationComponent(Vector3f position) {
        this.position.set(position);
    }

    /**
     * @return The position of this component relative to any parent. Can be directly modified to update the component
     */
    public Vector3f getLocalPosition() {
        return position;
    }

    public void setLocalPosition(Vector3f newPos) {
        position.set(newPos);
    }

    public Quat4f getLocalRotation() {
        return rotation;
    }

    public void setLocalRotation(Quat4f newQuat) {
        rotation.set(newQuat);
    }

    public void setLocalScale(float scale) {
        this.scale = scale;
    }

    public float getLocalScale() {
        return scale;
    }

    /**
     * Creates a new vector and puts the world location in it.
     * @return a newly allocated vector with the world location.
     */
    public Vector3f getWorldPosition() {
        return getWorldPosition(new Vector3f());
    }

    public Vector3f getWorldPosition(Vector3f output) {
        output.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.scale(parentLoc.scale);
            QuaternionUtil.quatRotate(parentLoc.getLocalRotation(), output, output);
            output.add(parentLoc.position);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public Quat4f getWorldRotation() {
        return getWorldRotation(new Quat4f(0, 0, 0, 1));
    }

    public Quat4f getWorldRotation(Quat4f output) {
        output.set(rotation);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.mul(parentLoc.rotation, output);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public float getWorldScale() {
        float result = scale;
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            result *= parentLoc.getLocalScale();
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return result;
    }

    public void setWorldPosition(Vector3f position) {
        this.position.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition());
            this.position.scale(1f / parentLoc.getWorldScale());
            Quat4f rot = new Quat4f(0, 0, 0, 1);
            rot.inverse(parentLoc.getWorldRotation());
            QuaternionUtil.quatRotate(rot, this.position, this.position);
        }
    }

    public void setWorldRotation(Quat4f rotation) {
        this.rotation.set(rotation);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quat4f worldRot = parentLoc.getWorldRotation();
            worldRot.inverse();
            this.rotation.mul(worldRot, this.rotation);
        }
    }

    public void setWorldScale(float scale) {
        this.scale = scale;
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.scale /= parentLoc.getWorldScale();
        }
    }

    public EntityRef getParent() {
        return parent;
    }

    public Collection<EntityRef> getChildren() {
        return children;
    }

    public void addChild(EntityRef child, EntityRef self) {
        LocationComponent childLoc = child.getComponent(LocationComponent.class);
        if (childLoc != null && !childLoc.getParent().equals(self)) {
            LocationComponent oldParentLoc = childLoc.getParent().getComponent(LocationComponent.class);
            if (oldParentLoc != null) {
                oldParentLoc.children.remove(child);
                childLoc.getParent().saveComponent(oldParentLoc);
            }
            childLoc.parent = self;
            child.saveComponent(childLoc);
            children.add(child);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationComponent component = (LocationComponent) o;

        if (Float.compare(component.scale, scale) != 0) return false;
        if (parent != null ? !parent.equals(component.parent) : component.parent != null) return false;
        if (position != null ? !position.equals(component.position) : component.position != null) return false;
        if (rotation != null ? !rotation.equals(component.rotation) : component.rotation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = position != null ? position.hashCode() : 0;
        result = 31 * result + (rotation != null ? rotation.hashCode() : 0);
        result = 31 * result + (scale != +0.0f ? Float.floatToIntBits(scale) : 0);
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
