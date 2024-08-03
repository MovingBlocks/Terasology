// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.location;

import com.google.common.collect.Lists;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Direction;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ReplicationCheck;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.TextField;
import org.terasology.reflection.metadata.FieldMetadata;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Component represent the location and facing of an entity in the world
 */
public final class LocationComponent implements Component<LocationComponent>, ReplicationCheck {

    public boolean replicateChanges = true;

    // Relative to
    @Replicate
    public EntityRef parent = EntityRef.NULL;
    @Replicate
    public List<EntityRef> children = Lists.newArrayList();
    // Standard position/rotation
    @Replicate
    @TextField
    public final Vector3f position = new Vector3f();
    @Replicate
    public Quaternionf rotation = new Quaternionf();
    @Replicate
    public float scale = 1.0f;
    @Replicate
    public Vector3f lastPosition = new Vector3f();
    @Replicate
    public Quaternionf lastRotation = new Quaternionf();
    public boolean isDirty = false;

    public LocationComponent() {
    }

    public LocationComponent(Vector3fc position) {
        setLocalPosition(position);
    }

    public LocationComponent(Vector3ic position) {
        this(new Vector3f(position));
    }

    private void dirty() {
        if (!isDirty && parent == EntityRef.NULL) {
            lastRotation.set(rotation);
            lastPosition.set(position);
            isDirty = true;
        }
    }

    boolean isDirty() {
        return this.isDirty;
    }

    void clearDirtyFlag() {
        isDirty = false;
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
        dirty();
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
        dirty();
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
     * local scale
     *
     * @return the scale
     */
    public float getLocalScale() {
        return scale;
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

    public void setWorldScale(float value) {
        this.scale = value;
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            this.scale /= parentLoc.getWorldScale();
        }
    }

    /**
     * set the world position of the {@link LocationComponent}
     *
     * @param pos position to set
     */
    public void setWorldPosition(Vector3fc pos) {
        dirty();
        position.set(pos);
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
        dirty();
        rotation.set(value);
        LocationComponent parentLoc = parent.getComponent(LocationComponent.class);
        if (parentLoc != null) {
            Quaternionf worldRot = parentLoc.getWorldRotation(new Quaternionf()).conjugate();
            this.rotation.premul(worldRot);
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


    @Override
    public void copyFrom(LocationComponent other) {
        this.replicateChanges = other.replicateChanges;
        this.isDirty = other.isDirty;
        this.parent = other.parent;
        this.children.clear();
        this.children.addAll(other.children);
        this.position.set(other.position);
        this.rotation.set(other.rotation);
        this.scale = other.scale;
        this.lastPosition.set(other.lastPosition);
        this.lastRotation.set(other.lastRotation);
    }
}
