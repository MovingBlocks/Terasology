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
import org.terasology.rendering.nui.databinding.Binding;

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
    
    public static List<Parameter> getFromObject(Object obj) {
        List<Parameter> result = Lists.newArrayList();
        
        Class<?> cls = obj.getClass();
        for (Field fld : cls.getFields()) {
            Parameter p;
            
            p = matchString(fld, obj);
            if (p != null) {
                result.add(p);
            }
            
            p = matchInt(fld, obj);
            if (p != null) {
                result.add(p);
            }
            
        }
        
        return result;
    }

    private static StringParameter matchString(final Field fld, final Object obj) {
        BoundStringParameter ann = fld.getAnnotation(BoundStringParameter.class);
        if (ann != null) {
            String label = ann.label();
            Binding<String> binding = new Binding<String>() {

                @Override
                public String get() {
                    try {
                        return String.valueOf(fld.get(obj));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(String value) {
                    try {
                        fld.set(obj, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.warn("Could not assign value", e);
                    }
                    
                }
                
            };
            
            return new StringParameter(label, binding);
        }
        
        return null;
    }
    
    private static IntParameter matchInt(final Field fld, final Object obj) {
        BoundIntParameter ann = fld.getAnnotation(BoundIntParameter.class);
        if (ann != null) {
            String label = ann.label();
            Binding<Integer> binding = new Binding<Integer>() {

                @Override
                public Integer get() {
                    try {
                        return fld.getInt(obj);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(Integer value) {
                    try {
                        fld.setInt(obj, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.warn("Could not assign value", e);
                    }
                    
                }
                
            };
            return new IntParameter(label, binding, ann.min(), ann.max());
        }
        
        return null;
    }
    
    private static FloatParameter matchFloat(final Field fld, final Object obj) {
        BoundFloatParameter ann = fld.getAnnotation(BoundFloatParameter.class);
        if (ann != null) {
            String label = ann.label();
            Binding<Float> binding = new Binding<Float>() {

                @Override
                public Float get() {
                    try {
                        return fld.getFloat(obj);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(Float value) {
                    try {
                        fld.setFloat(obj, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.warn("Could not assign value", e);
                    }
                    
                }
                
            };
            return new FloatParameter(label, binding, ann.min(), ann.max(), ann.step());
        }
        
        return null;
    }
    
    private static BooleanParameter matchBoolean(final Field fld, final Object obj) {
        BoundBooleanParameter ann = fld.getAnnotation(BoundBooleanParameter.class);
        if (ann != null) {
            String label = ann.label();
            Binding<Boolean> binding = new Binding<Boolean>() {

                @Override
                public Boolean get() {
                    try {
                        return fld.getBoolean(obj);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void set(Boolean value) {
                    try {
                        fld.setBoolean(obj, value);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        logger.warn("Could not assign value", e);
                    }
                    
                }
                
            };
            return new BooleanParameter(label, binding);
        }
        
        return null;
    }        
}
