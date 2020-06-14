/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.location;

import com.google.common.collect.Lists;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.rendering.nui.properties.TextField;
import org.terasology.world.block.Block;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Component represent the location and facing of an entity in the world
 *
 */
public final class LocationComponent implements Component, ReplicationCheck {

    public boolean replicateChanges = true;

    // Relative to
    @Replicate
    EntityRef parent = EntityRef.NULL;

    @Replicate
    List<EntityRef> children = Lists.newArrayList();

    // Standard position/rotation
    @Replicate
    @TextField
    Vector3f position = new Vector3f();
    @Replicate
    Quat4f rotation = new Quat4f(0, 0, 0, 1);
    @Replicate
    float scale = 1.0f;
    @Replicate
    Vector3f lastPosition = new Vector3f();
    @Replicate
    Quat4f lastRotation = new Quat4f(0,0,0,1);

    public LocationComponent() {
    }

    public LocationComponent(Vector3f position) {
        setLocalPosition(position);
    }

    /**
     * @return The position of this component relative to any parent. Can be directly modified to update the component
     * TODO: make this readonly Vector3fc -- Michael Pollind
     */
    public Vector3f getLocalPosition() {
        return position;
    }

    /**
     *
     * @param newPos
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #setLocalPosition(Vector3fc)}.
     */
    public void setLocalPosition(Vector3f newPos) {
        lastPosition.set(position);
        position.set(newPos);
    }

    /**
     * the local position of this location component
     * @param newPos position to set
     */
    public void setLocalPosition(Vector3fc newPos) {
        lastPosition.set(position);
        position.set(JomlUtil.from(newPos));
    }

    public Vector3f getLocalDirection() {
        Vector3f result = Direction.FORWARD.getVector3f();
        getLocalRotation().rotate(result, result);
        return result;
    }

    public org.joml.Vector3f getLocalDirection(org.joml.Vector3f dest) {
        return dest.set(Direction.FORWARD.asVector3i()).rotate(JomlUtil.from(getLocalRotation()));
    }

    // TODO: make this readonly Quaternionfc -- Michael Pollind
    public Quat4f getLocalRotation() {
        return rotation;
    }

    public void setLocalRotation(Quat4f newQuat) {
        lastRotation.set(rotation);
        rotation.set(newQuat);
    }

    public void setLocalScale(float value) {
        this.scale = value;
    }

    public float getLocalScale() {
        return scale;
    }

    /**
     * @return A new vector containing the world location.
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getWorldPosition(org.joml.Vector3f)}.
     */
    public Vector3f getWorldPosition() {
        return getWorldPosition(new Vector3f());
    }

    /**
     *
     * @param output
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getWorldPosition(org.joml.Vector3f)}.
     */
    public Vector3f getWorldPosition(Vector3f output) {
        output.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.scale(parentLoc.scale);
            parentLoc.getLocalRotation().rotate(output, output);
            output.add(parentLoc.position);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public org.joml.Vector3f getWorldPosition(org.joml.Vector3f dest) {
        dest.set(JomlUtil.from(position));
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            dest.mul(parentLoc.scale);
            dest.rotate(JomlUtil.from(parentLoc.getLocalRotation()));
            dest.add(JomlUtil.from(parentLoc.position));
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return dest;
    }

    /**
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getWorldDirection(org.joml.Vector3f)}.
     */
    public Vector3f getWorldDirection() {
        Vector3f result = Direction.FORWARD.getVector3f();
        getWorldRotation().rotate(result, result);
        return result;
    }


    public org.joml.Vector3f getWorldDirection(org.joml.Vector3f dest) {
        return dest.set(Direction.FORWARD.asVector3i()).rotate(JomlUtil.from(getWorldRotation()));
    }

    public Quat4f getWorldRotation() {
        return getWorldRotation(new Quat4f(0, 0, 0, 1));
    }

    /**
     * @deprecated This is scheduled for removal in an upcoming version
     *             method will be replaced with JOML implementation {@link #getWorldRotation(Quaternionf)}.
     */
    public Quat4f getWorldRotation(Quat4f output) {
        output.set(rotation);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.mul(parentLoc.rotation, output);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public Quaternionf getWorldRotation(Quaternionf dest) {
        dest.set(JomlUtil.from(rotation));
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            dest.premul(JomlUtil.from(parentLoc.rotation));
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return dest;
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

    public void setWorldPosition(Vector3f value) {
        setLocalPosition(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition());
            this.position.scale(1f / parentLoc.getWorldScale());
            Quat4f rot = new Quat4f(0, 0, 0, 1);
            rot.inverse(parentLoc.getWorldRotation());
            rot.rotate(this.position, this.position);
        }
    }

    public void setWorldRotation(Quat4f value) {
        setLocalRotation(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quat4f worldRot = parentLoc.getWorldRotation();
            worldRot.inverse();
            this.rotation.mul(worldRot, this.rotation);
        }
    }

    public void setWorldScale(float value) {
        this.scale = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof LocationComponent) {
            LocationComponent other = (LocationComponent) o;
            return other.scale == scale && Objects.equals(parent, other.parent) && Objects.equals(position, other.position) && Objects.equals(rotation, other.rotation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, rotation, scale, parent);
    }

    @Override
    public boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner) {
        return initial || replicateChanges;
    }
}
