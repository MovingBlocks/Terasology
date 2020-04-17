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
import org.joml.Quaternionf;
import org.joml.Vector3f;


import java.util.Collection;
import java.util.List;

/**
 */
public class Bone {
    private String name;
    private int index;
    private Vector3f objectSpacePos = new Vector3f();
    private Quaternionf rotation = new Quaternionf(0, 0, 0, 1);

    private Bone parent;
    private List<Bone> children = Lists.newArrayList();

    public Bone(int index, String name, Vector3f position, Quaternionf rotation) {
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
            Quaternionf inverseParentRot = new Quaternionf();
            parent.getObjectRotation().invert(inverseParentRot);
            inverseParentRot.transform(pos, pos);
        }
        return pos;
    }

    public Quaternionf getObjectRotation() {
        return rotation;
    }

    public void setObjectRotation(Quaternionf newRotation) {
        this.rotation = newRotation;
    }

    public Quaternionf getLocalRotation() {
        Quaternionf rot = new Quaternionf(rotation);
        if (parent != null) {
            Quaternionf inverseParentRot = new Quaternionf();
            parent.getObjectRotation().invert(inverseParentRot);
            rot.premul(inverseParentRot);
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
