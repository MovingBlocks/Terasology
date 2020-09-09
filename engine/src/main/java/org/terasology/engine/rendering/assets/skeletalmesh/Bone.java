// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.skeletalmesh;

import com.google.common.collect.Lists;

import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 */
public class Bone {
    private final String name;
    private final int index;
    private Vector3f objectSpacePos = new Vector3f();
    private Quat4f rotation = new Quat4f(0, 0, 0, 1);

    private Bone parent;
    private final List<Bone> children = Lists.newArrayList();

    public Bone(int index, String name, Vector3f position, Quat4f rotation) {
        this.index = index;
        this.name = name;
        this.objectSpacePos.set(position);
        this.rotation.set(rotation);
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public Vector3f getObjectPosition() {
        return objectSpacePos;
    }

    public void setObjectPos(Vector3f newObjectSpacePos) {
        this.objectSpacePos = newObjectSpacePos;
    }

    public Vector3f getLocalPosition() {
        Vector3f pos = new Vector3f(objectSpacePos);
        if (parent != null) {
            pos.sub(parent.getObjectPosition());
            Quat4f inverseParentRot = new Quat4f();
            inverseParentRot.inverse(parent.getObjectRotation());
            inverseParentRot.rotate(pos, pos);
        }
        return pos;
    }

    public Quat4f getObjectRotation() {
        return rotation;
    }

    public void setObjectRotation(Quat4f newRotation) {
        this.rotation = newRotation;
    }

    public Quat4f getLocalRotation() {
        Quat4f rot = new Quat4f(rotation);
        if (parent != null) {
            Quat4f inverseParentRot = new Quat4f();
            inverseParentRot.inverse(parent.getObjectRotation());
            rot.mul(inverseParentRot, rot);
        }
        return rot;
    }

    public Bone getParent() {
        return parent;
    }

    public int getParentIndex() {
        return parent != null ? parent.getIndex() : -1;
    }

    public void addChild(Bone child) {
        if (child.parent != null) {
            child.parent.children.remove(child);
        }
        child.parent = this;
        children.add(child);
    }

    public Collection<Bone> getChildren() {
        return children;
    }
}
