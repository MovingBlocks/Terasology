/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.skeletalmesh;

import com.google.common.collect.Lists;

import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Collection;
import java.util.List;

/**
 */
public class Bone {
    private String name;
    private int index;
    private Vector3f relativePosition = new Vector3f();
    private Quat4f relativeRotation = new Quat4f(0, 0, 0, 1);
    private Matrix4f inverseBindMatrix = new Matrix4f(Matrix4f.IDENTITY);

    private Bone parent;
    private List<Bone> children = Lists.newArrayList();

    public Bone(int index, String name, Vector3f position, Quat4f rotation) {
        this.index = index;
        this.name = name;
        this.relativePosition.set(position);
        this.relativeRotation.set(rotation);
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

    public Vector3f getObjectPosition() {
        Vector3f pos = new Vector3f(relativePosition);
        if (parent != null) {
            pos.sub(parent.getObjectPosition());
            Quat4f inverseParentRot = new Quat4f();
            inverseParentRot.inverse(parent.getObjectRotation());
            inverseParentRot.rotate(pos, pos);
        }
        return pos;
    }

    public Vector3f getLocalPosition() {
        return relativePosition;
    }

    public Quat4f getObjectRotation() {
        Quat4f rot = new Quat4f(relativeRotation);
        if (parent != null) {
            Quat4f inverseParentRot = new Quat4f();
            inverseParentRot.inverse(parent.getObjectRotation());
            rot.mul(inverseParentRot, rot);
        }
        return rot;
    }

    public Quat4f getLocalRotation() {
        return relativeRotation;
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
