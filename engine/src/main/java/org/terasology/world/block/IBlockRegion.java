// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import org.joml.AABBf;
import org.joml.LineSegmentf;
import org.joml.Matrix4fc;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Iterator;
import java.util.Optional;

public interface IBlockRegion extends Iterable<Vector3ic> {
    public static BlockRegion invalid();

    // -- CONSTRUCTORS ----------------------------------------------------------------------------------------------//
    BlockRegion();
    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    public BlockRegion(Vector3ic min, Vector3ic max);
    public BlockRegion(int x, int y, int z);
    public BlockRegion(Vector3ic block);
    public BlockRegion(BlockRegion source)

    // -- ITERABLE -------------------------------------------------------------------------------------------------//
    public Iterator<Vector3ic> iterator();

    // -- GETTERS & SETTERS -----------------------------------------------------------------------------------------//
    public BlockRegion set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);
    public BlockRegion set(Vector3ic min, Vector3ic max);
    public BlockRegion set(BlockRegion source);

    // -- min -------------------------------------------------------------------------------------------------------//
    public int minX();
    public BlockRegion minX(int x);

    public int minY();
    public BlockRegion minY(int y);

    public int minZ();
    public BlockRegion minZ(int z);

    public Vector3i getMin(Vector3i dest);

    public BlockRegion setMin(int x, int y, int z);
    public BlockRegion setMin(Vector3ic min);
    public BlockRegion translateMin(int dx, int dy, int dz);
    public BlockRegion translateMin(Vector3ic dmin);

    // -- max -------------------------------------------------------------------------------------------------------//
    public int maxX();
    public BlockRegion maxX(int x);

    public int maxY();
    public BlockRegion maxY(int y);

    public int maxZ();
    public BlockRegion maxZ(int z);

    public Vector3i getMax(Vector3i dest);

    public BlockRegion setMax(int x, int y, int z);
    public BlockRegion setMax(Vector3ic max);
    public BlockRegion translateMax(int dx, int dy, int dz);
    public BlockRegion translateMax(Vector3ic dmax);

    // -- size ------------------------------------------------------------------------------------------------------//
    public int getSizeX();
    public int getSizeY();
    public int getSizeY();
    public Vector3i getSize(Vector3i dest);

    public BlockRegion setSize(int sizeX, int sizeY, int sizeZ);
    public BlockRegion setSize(Vector3ic size);

    public int size();                                              // what to call this? size, volume, blockCount, ...

    // -- world -----------------------------------------------------------------------------------------------------//
    public AABBf getBounds(AABBf dest);
    public Vector3f center(Vector3f dest);

    // -- IN-PLACE MUTATION -----------------------------------------------------------------------------------------//
    // -- union -----------------------------------------------------------------------------------------------------//
    public BlockRegion union(int x, int y, int z);
    public BlockRegion union(Vector3ic pos);
    public BlockRegion union(BlockRegion other);

    // -- intersect -------------------------------------------------------------------------------------------------//
    public Optional<BlockRegion> intersect(BlockRegion other);      // this can leave the region in invalid state!

    // ---------------------------------------------------------------------------------------------------------------//
    public BlockRegion translate(int dx, int dy, int dz);
    public BlockRegion translate(Vector3ic vec);

    // -- expand -----------------------------------------------------------------------------------------------------//
    public BlockRegion expand(int dx, int dy, int dz);
    public BlockRegion expand(Vector3ic vec);

    public BlockRegion expand(float dx, float dy, float dz);        // why do we offer float variants here?
    public BlockRegion expand(Vector3fc vec);

    // -- transform --------------------------------------------------------------------------------------------------//
    public BlockRegion transform(Matrix4fc m);

    // -- CHECKS -----------------------------------------------------------------------------------------------------//
    public boolean isValid();

    // -- contains ---------------------------------------------------------------------------------------------------//
    public boolean contains(int x, int y, int z);
    public boolean contains(Vector3ic pos);

    public boolean contains(Vector3fc point);
    public boolean contains(float x, float y, float z);

    public boolean contains(BlockRegion other);

    // -- intersects -------------------------------------------------------------------------------------------------//

    public boolean intersectsAABB(AABBf other);
    public boolean intersectsBlockRegion(BlockRegion other);
    public boolean intersectsPlane(float a, float b, float c, float d);
    public boolean intersectsPlane(Planef plane);
    public boolean intersectsRay(float originX, float originY, float originZ, float dirX, float dirY, float dirZ);
    public boolean intersectsRay(Rayf ray);
    public boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared);
    public boolean intersectsSphere(Spheref sphere);

    public int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z, Vector2f result);
    public int intersectLineSegment(LineSegmentf lineSegment, Vector2f result);

    // ---------------------------------------------------------------------------------------------------------------//
    public boolean equals(Object obj);
    public int hashCode();
    public String toString();
}
