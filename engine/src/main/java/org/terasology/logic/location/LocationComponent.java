// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.location;

import com.google.common.collect.Lists;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Direction;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;
import org.terasology.nui.properties.TextField;
import org.terasology.reflection.metadata.FieldMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Component represent the location and facing of an entity in the world
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
    @Replicate
    Vector3f lastPosition = new Vector3f();
    @Replicate
    Quaternionf lastRotation = new Quaternionf();

    public LocationComponent() {
    }

    public LocationComponent(Vector3fc position) {
        setLocalPosition(position);
    }

    /**
     * @return local rotation of location component
     */
    public Quaternionfc getLocalRotation() {
        return rotation;
    }

    /**
     * set the current local rotation of the component
     *
     * @param rot local rotation
     */
    public void setLocalRotation(Quaternionfc rot) {
        this.setLocalRotation(rot.x(), rot.y(), rot.z(), rot.w());
    }

    public void setLocalRotation(float x, float y, float z, float w) {
        lastRotation.set(rotation);
        rotation.set(x, y, z, w);
    }

    /**
     * @return The position of this component relative to any parent. Can be directly modified to update the component
     */
    public Vector3fc getLocalPosition() {
        return position;
    }

    /**
     * the local position of this location component
     *
     * @param pos position to set
     */
    public void setLocalPosition(Vector3fc pos) {
        setLocalPosition(pos.x(), pos.y(), pos.z());
    }

    public void setLocalPosition(float x, float y, float z) {
        lastPosition.set(position);
        position.set(x, y, z);
    }

    /**
     * gets the local direction of the given entity in
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getLocalDirection(Vector3f dest) {
        return dest.set(Direction.FORWARD.asVector3i()).rotate(getLocalRotation());
    }

    /**
     * set the local scale
     *
     * @param value the scale
     */
    public void setLocalScale(float value) {
        this.scale = value;
    }

    /**
     * local scale
     *
     * @return the scale
     */
    public float getLocalScale() {
        return scale;
    }

    /**
     * get the world position
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getWorldPosition(Vector3f dest) {
        dest.set(position);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            dest.mul(parentLoc.scale)
                    .rotate(parentLoc.getLocalRotation())
                    .add(parentLoc.position);
            parentLoc = parentLoc.parent.getComponent(LocationComponent.class);
        }
        return dest;
    }

    /**
     * Populates out with the transform of this entity relative to the given entity, or the world transform if entity is
     * not in this entity's parent hierarchy
     *
     * @param out
     * @param entity
     */
    public void getRelativeTransform(Matrix4f out, EntityRef entity) {
        if (!(entity.equals(parent))) {
            LocationComponent loc = parent.getComponent(LocationComponent.class);
            if (loc != null) {
                loc.getRelativeTransform(out, entity);
            }
        }
        out.mul(new Matrix4f().translationRotateScale(position, rotation, scale));
    }

    public Vector3f getWorldDirection(Vector3f dest) {
        return dest.set(Direction.FORWARD.asVector3f()).rotate(getWorldRotation(new Quaternionf()));
    }

    /**
     * get the current world rotation of the location component
     *
     * @param dest will hold the result
     * @return dest
     */
    public Quaternionf getWorldRotation(Quaternionf dest) {
        dest.set(rotation);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        while (parentLoc != null) {
            dest.premul(parentLoc.rotation);
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

    /**
     * set the world position of the {@link LocationComponent}
     *
     * @param pos position to set
     */
    public void setWorldPosition(Vector3fc pos) {
        setLocalPosition(pos);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.position.sub(parentLoc.getWorldPosition(new Vector3f()));
            this.position.div(parentLoc.getWorldScale());
            this.position.rotate(parentLoc.getWorldRotation(new Quaternionf()).conjugate());
        }
    }

    /**
     * set the world rotation of the {@link LocationComponent}
     *
     * @param value position to set
     */
    public void setWorldRotation(Quaternionfc value) {
        setLocalRotation(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quaternionf worldRot = parentLoc.getWorldRotation(new Quaternionf()).conjugate();
            this.rotation.mul(worldRot);
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
            return other.scale == scale && Objects.equals(parent, other.parent) && Objects.equals(position,
                    other.position) && Objects.equals(rotation, other.rotation);
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
