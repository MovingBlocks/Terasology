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
import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Direction;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.rendering.nui.properties.TextField;

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
    Quaternionf rotation = new Quaternionf();
    @Replicate
    float scale = 1.0f;

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

    public Vector3f getLocalDirection() {
        Vector3f result = Direction.FORWARD.getVector3f().rotate(getLocalRotation());
        return result;
    }

    public Quaternionf getLocalRotation() {
        return rotation;
    }

    public void setLocalRotation(Quaternionf newQuat) {
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
     */
    public Vector3f getWorldPosition() {
        return getWorldPosition(new Vector3f());
    }

    public Vector3f getWorldPosition(Vector3f output) {
        output.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            output.mul(parentLoc.scale)
                    .rotate(parentLoc.getLocalRotation())
                    .add(parentLoc.position);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return output;
    }

    public Vector3f getWorldDirection() {
        return Direction.FORWARD.getVector3f()
                .rotate(getWorldRotation());
    }

    public Quaternionf getWorldRotation() {
        return getWorldRotation(new Quaternionf());
    }

    public Quaternionf getWorldRotation(Quaternionf output) {
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

    public void setWorldPosition(Vector3f value) {
        this.position.set(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition());
            this.position.mul(1f / parentLoc.getWorldScale());
            this.position.rotate(new Quaternionf(parentLoc.getWorldRotation()).conjugate());
        }
    }

    public void setWorldRotation(Quaternionf value) {
        this.rotation.set(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quaternionf worldRot = parentLoc.getWorldRotation().conjugate();
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
