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

package org.terasology.entitySystem.stubs;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.DoNotAutoRegister;

/**
 * @author Immortius
 */
@DoNotAutoRegister
public class UnsupportedTypeComponent implements Component {
    public static class UnsupportedType {
        private UnsupportedType() {
        }

        ;
    }

    public abstract static class UnsupportedType2 {

    }

    public interface UnsupportedType3 {

    }

    public UnsupportedType value;
    public UnsupportedType2 value2;
    public UnsupportedType3 value3;
}
