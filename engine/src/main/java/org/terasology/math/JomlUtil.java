// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.math;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.geom.BaseMatrix3f;
import org.terasology.math.geom.BaseMatrix4f;
import org.terasology.math.geom.BaseQuat4f;
import org.terasology.math.geom.BaseVector2f;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.BaseVector3f;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.BaseVector4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.world.block.Block;

import java.util.Map;
import java.util.stream.Collectors;

public final class JomlUtil {
    private JomlUtil() {

    }
    public static org.terasology.math.geom.Matrix4f from(Matrix4fc mat) {
        if (mat == null) {
            return null;
        }
        return new org.terasology.math.geom.Matrix4f(mat.m00(), mat.m01(), mat.m02(), mat.m03(),
            mat.m10(), mat.m11(), mat.m12(), mat.m13(),
            mat.m20(), mat.m21(), mat.m22(), mat.m23(),
            mat.m30(), mat.m31(), mat.m32(), mat.m33());
    }

    public static org.terasology.math.geom.Matrix3f from(Matrix3fc mat) {
        if (mat == null) {
            return null;
        }
        return new org.terasology.math.geom.Matrix3f(mat.m00(), mat.m01(), mat.m02(),
            mat.m10(), mat.m11(), mat.m12(),
            mat.m20(), mat.m21(), mat.m22());
    }

    public static org.joml.Matrix4f from(BaseMatrix4f mat) {
        if (mat == null) {
            return null;
        }
        return new org.joml.Matrix4f(mat.getM00(), mat.getM01(), mat.getM02(), mat.getM03(),
            mat.getM10(), mat.getM11(), mat.getM12(), mat.getM13(),
            mat.getM20(), mat.getM21(), mat.getM22(), mat.getM23(),
            mat.getM30(), mat.getM31(), mat.getM32(), mat.getM33());
    }


    public static Matrix3f from(BaseMatrix3f mat) {
        if (mat == null) {
            return null;
        }
        return new Matrix3f(mat.getM00(), mat.getM01(), mat.getM02(),
            mat.getM10(), mat.getM11(), mat.getM12(),
            mat.getM20(), mat.getM21(), mat.getM22());
    }

    public static org.terasology.math.geom.Vector4f from(Vector4fc vec) {
        if (vec == null) {
            return null;
        }
        return new org.terasology.math.geom.Vector4f(vec.x(), vec.y(), vec.z(), vec.w());
    }

    public static org.terasology.math.geom.Vector3f from(Vector3fc vec) {
        if (vec == null) {
            return null;
        }
        return new org.terasology.math.geom.Vector3f(vec.x(), vec.y(), vec.z());
    }

    public static org.terasology.math.geom.Vector2f from(Vector2fc vec) {
        if (vec == null) {
            return null;
        }
        return new org.terasology.math.geom.Vector2f(vec.x(), vec.y());
    }

    public static org.terasology.math.geom.Vector3i from(Vector3ic vec) {
        if (vec == null) {
            return null;
        }
        return new org.terasology.math.geom.Vector3i(vec.x(), vec.y(), vec.z());
    }

    public static org.terasology.math.geom.Vector2i from(Vector2ic vec) {
        if (vec == null) {
            return null;
        }
        return new org.terasology.math.geom.Vector2i(vec.x(), vec.y());
    }

    public static org.joml.Vector4f from(BaseVector4f vec) {
        if (vec == null) {
            return null;
        }
        return new Vector4f(vec.x(), vec.y(), vec.z(), vec.w());
    }

    public static org.joml.Vector3f from(BaseVector3f vec) {
        if (vec == null) {
            return null;
        }
        return new org.joml.Vector3f(vec.x(), vec.y(), vec.z());
    }

    public static org.joml.Vector2f from(BaseVector2f vec) {
        if (vec == null) {
            return null;
        }
        return new org.joml.Vector2f(vec.x(), vec.y());
    }

    public static org.joml.Vector2i from(BaseVector2i vec) {
        if (vec == null) {
            return null;
        }
        return new org.joml.Vector2i(vec.x(), vec.y());
    }

    public static org.joml.Vector3i from(BaseVector3i vec) {
        if (vec == null) {
            return null;
        }
        return new org.joml.Vector3i(vec.x(), vec.y(), vec.z());
    }

    public static Quat4f from(Quaternionfc quat) {
        if (quat == null) {
            return null;
        }
        return new Quat4f(quat.x(), quat.y(), quat.z(), quat.w());
    }

    public static Quaternionf from(BaseQuat4f quat) {
        if (quat == null) {
            return null;
        }
        return new Quaternionf(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public static Rectanglei rectangleiFromMinAndSize(int minX, int minY, int width, int height) {
        return new Rectanglei(minX, minY, minX + width, minY + height);
    }

    public static Rectanglef rectanglefFromMinAndSize(float minX, float minY, float width, float height) {
        return new Rectanglef(minX, minY, minX + width, minY + height);
    }

    public static Map<org.terasology.math.geom.Vector3i, Block> blockMap(Map<Vector3i, Block> maps) {
        return maps.entrySet().stream().collect(Collectors.toMap(k -> JomlUtil.from(k.getKey()), Map.Entry::getValue));
    }

    public static Map<Vector3ic, Block> toBlockMap(Map<org.terasology.math.geom.Vector3i, Block> maps) {
        return maps.entrySet().stream().collect(Collectors.toMap(k -> JomlUtil.from(k.getKey()), Map.Entry::getValue));
    }
}
