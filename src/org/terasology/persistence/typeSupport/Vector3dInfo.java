/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.persistence.typeSupport;

import org.terasology.persistence.interfaces.LevelReader;
import org.terasology.persistence.interfaces.LevelWriter;

import javax.vecmath.Vector3d;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3dInfo extends AbstractTypeInfo<Vector3d> {
    
    public Vector3dInfo() {
        super(Vector3d.class, (byte)(8 * 3));
    }

    public void write(DataOutputStream out, Vector3d value, LevelWriter writer) throws Exception {
        out.writeDouble(value.x);
        out.writeDouble(value.y);
        out.writeDouble(value.z);
    }

    public Vector3d read(DataInputStream in, LevelReader reader) throws Exception {
        double x = in.readDouble();
        double y = in.readDouble();
        double z = in.readDouble();
        return new Vector3d(x,y,z);
    }
}
