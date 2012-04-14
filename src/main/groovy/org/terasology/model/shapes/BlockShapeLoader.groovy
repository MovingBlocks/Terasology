package org.terasology.model.shapes

import gnu.trove.list.TIntList
import gnu.trove.list.array.TIntArrayList
import groovy.util.logging.Log
import org.terasology.math.Side
import org.terasology.model.structures.AABB
import org.terasology.utilities.ClasspathResourceLoader

import javax.vecmath.Vector2f
import javax.vecmath.Vector3d
import javax.vecmath.Vector3f

/**
 * @author Immortius <immortius@gmail.com>
 */
@Log
final class BlockShapeLoader {
    private static BlockShapeManager _bm
    private ClasspathResourceLoader _resourceLoader;

    // Empty default constructor for child classes
    public BlockShapeLoader() {}

    public BlockShapeLoader(BlockShapeManager bm) {
        _bm = bm
    }

    /**
     * For now, just loading BlockShapes, which will be baked into into Blocks that are loaded (after any transformation).
     * Later, blocks reload their shapes if they are still available, so they remain up-to-date with the shape mesh.
     */
    public loadShapes() throws Exception {
        _resourceLoader = new ClasspathResourceLoader("org/terasology/data/shapes")
        _resourceLoader.getClassesAt("").each { c ->
            try {
                // log.info("Loading shape: " + c)

                // Prepare to load properties from the Groovy definition via ConfigSlurper
                ConfigObject shapeConfig = new ConfigSlurper().parse((Class) c)
                shapeConfig.put("name", c.getSimpleName())

                BlockShape shape = loadShape(shapeConfig.shape, c.getSimpleName())
                if (shape != null) {
                    _bm.addBlockShape(shape)
                }

            } catch (Exception e) {
                org.terasology.model.shapes.BlockShapeLoader.log.severe(String.format("Failed to load shape: %s\n%s", c, e));
            }
        }
    }
    /**
     * @return the Shape described in shapeConfig, or null if it could not be loaded
     */
    private loadShape(ConfigObject shapeConfig, String shapeName) {
        // Construct the class - this loads the Block-level defaults
        BlockShape shape = new BlockShape(shapeName)

        if (shapeConfig.Center != [:]) {
            shape.setCenterMesh(loadMeshPart(shapeConfig.Center));
        }
        if (shapeConfig.Top != [:]) {
            shape.setSideMesh(Side.TOP, loadMeshPart(shapeConfig.Top));
            shape.setBlockingSide(Side.TOP, shapeConfig.Top.fullSide);
        }
        if (shapeConfig.Bottom != [:]) {
            shape.setSideMesh(Side.BOTTOM, loadMeshPart(shapeConfig.Bottom));
            shape.setBlockingSide(Side.BOTTOM, shapeConfig.Bottom.fullSide);
        }
        if (shapeConfig.Left != [:]) {
            shape.setSideMesh(Side.LEFT, loadMeshPart(shapeConfig.Left));
            shape.setBlockingSide(Side.LEFT, shapeConfig.Left.fullSide);
        }
        if (shapeConfig.Right != [:]) {
            shape.setSideMesh(Side.RIGHT, loadMeshPart(shapeConfig.Right));
            shape.setBlockingSide(Side.RIGHT, shapeConfig.Right.fullSide);
        }
        if (shapeConfig.Front != [:]) {
            shape.setSideMesh(Side.FRONT, loadMeshPart(shapeConfig.Front));
            shape.setBlockingSide(Side.FRONT, shapeConfig.Front.fullSide);
        }
        if (shapeConfig.Back != [:]) {
            shape.setSideMesh(Side.BACK, loadMeshPart(shapeConfig.Back));
            shape.setBlockingSide(Side.BACK, shapeConfig.Back.fullSide);
        }
        if (shapeConfig.Colliders != [:]) {
            List<AABB> colliders = new ArrayList<AABB>();
            shapeConfig.Colliders.each() { collider -> 
                colliders.add(new AABB(new Vector3d(collider.position[0], collider.position[1], collider.position[2]), new Vector3d(collider.extents[0], collider.extents[1], collider.extents[2])))
            }
            shape.setColliders(colliders);
        } else {
            shape.setColliders([new AABB(new Vector3d(), new Vector3d(0.5,0.5,0.5))]);
        }
        return shape
    }

    private loadMeshPart(ConfigObject meshConfig) {
        Vector3f[] vertices = new Vector3f[meshConfig.vertices.size()];
        meshConfig.vertices.eachWithIndex() { v, index ->
            vertices[index] = new Vector3f(v[0], v[1], v[2]);
        }
        Vector3f[] normals = new Vector3f[meshConfig.vertices.size()];
        meshConfig.normals.eachWithIndex() { n, index ->
            normals[index] = new Vector3f(n[0], n[1], n[2]);
            normals[index].normalize();
        }
        Vector2f[] texCoords = new Vector2f[meshConfig.texcoords.size()];
        meshConfig.texcoords.eachWithIndex() { uv, index ->
            texCoords[index] = new Vector2f(uv[0], uv[1]);
        }

        if (vertices.size() != normals.size() || vertices.size() != texCoords.size()) {
            throw new IllegalFormatException("vertices, normals and texCoords must have the same length");
        }

        TIntList indices = new TIntArrayList();
        meshConfig.faces.each { face ->
            for (int tri = 0; tri < face.size() - 2; tri++) {
                indices.add(face[0]);
                indices.add(face[tri + 1]);
                indices.add(face[tri + 2]);
            }
        }

        return new BlockMeshPart(vertices, normals, texCoords, indices.toArray());
    }

}