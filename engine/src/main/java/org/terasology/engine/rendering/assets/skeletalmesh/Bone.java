// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.skeletalmesh;

import com.google.common.collect.Lists;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class Bone {
    private String name;
    private int index;
    private Matrix4f relativeTransform = new Matrix4f();
    private Matrix4f inverseBindMatrix = new Matrix4f();

    private Bone parent;
    private List<Bone> children = Lists.newArrayList();

    public Bone(int index, String name, Matrix4f transform) {
        this.index = index;
        this.name = name;
        this.relativeTransform.set(transform);
    }

    public String getName() {
        return name;
    }

    public Matrix4f getInverseBindMatrix() {
        return inverseBindMatrix;
    }

    public void setInverseBindMatrix(Matrix4f mat) {
        this.inverseBindMatrix.set(mat);
    }

    public int getIndex() {
        return index;
    }

    public Matrix4f getObjectTransform() {
        Matrix4f result = new Matrix4f(relativeTransform);
        if (parent != null) {
            result.mul(parent.getObjectTransform());
        }
        return result;
    }

    public Matrix4f getLocalTransform() {
        return relativeTransform;
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

    public Vector3f getLocalPosition() {
        Vector3f result = new Vector3f();
        relativeTransform.transformPosition(result);
        return result;
    }

    public Quaternionf getLocalRotation() {
        return relativeTransform.getNormalizedRotation(new Quaternionf());
    }

    public Vector3f getLocalScale() {
        return relativeTransform.getScale(new Vector3f());

    }
}
