package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import javax.vecmath.Color4f;

/**
 *
 * @author Immortius <immortius@gmail.com>
 */
public class MeshComponent implements Component {

    // TODO: Use some sort of mesh ref, that stores a direct reference to the mesh (flyweight pattern?)
    public String mesh;
    // TODO: Some sort of Texture + Shader type?
    //public String material;
    
    // This should be elsewhere I think, probably in the material
    public Color4f color = new Color4f(0,0,0,1);
    
    public void store(StorageWriter writer) {
        writer.write("mesh", mesh);
        //writer.write("material", material);
        writer.write("color", color);
    }

    public void retrieve(StorageReader reader) {
        mesh = reader.readString("mesh");
        //material = reader.readString("material");
        color = reader.read("color", Color4f.class, color);
    }
}
