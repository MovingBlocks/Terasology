/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generator.params;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Utility methods
 * @author Martin Steiger
 */
public final class Parameters {

    private static final Logger logger = LoggerFactory.getLogger(Parameters.class);
    
    private Parameters() {
        // empty
    }
    
    public static List<Parameter> getFromObject(Class<?> cls) {
        List<Parameter> result = Lists.newArrayList();
        
        for (Field fld : cls.getFields()) {
            Label ann = fld.getAnnotation(Label.class);
            if (ann != null) {
//                Parameter p = createParameterfld.getType()
            }
            
        }
        
        return result;
    }
}
