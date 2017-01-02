/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine;

import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;

import java.util.Objects;

/**
 * A URI to identify a field of a component in terasology.
 * These URIs are always in the form: {@literal <module-name>:<object-name>.<fieldName>}. They are case-insensitive (using
 * English casing), and have a "normalized" form that is lower case.
 *
 */
@API
public final class ComponentFieldUri implements Uri {

    public static final String FIELD_SEPARATOR = ".";  //The character used to separate the URI into different pieces of information
    private final SimpleUri componentUri;  //A SimpleUri object used to identify the field of a component
    private final String fieldName; //The name of the field which the componentUri will be describing
    
    /**
     * The Constructor for the ComponentFieldUri Object
     * 
     * @param componentUri  A SimpleUri object used to identify the field of a component
     * @param fieldName     The name of the field which the componentUri is describing      
     */
    public ComponentFieldUri(SimpleUri componentUri, String fieldName) {
        this.componentUri = componentUri;
        this.fieldName = fieldName;
    }
    
    /**
     * The Constructor for the ComponentFieldUri
     * 
     * @param textVersion  The String version of the Uri
     */
    public ComponentFieldUri(String textVersion) {
        int seperatorIndex = textVersion.indexOf(FIELD_SEPARATOR, 2);

        if (seperatorIndex != -1) {
            componentUri = new SimpleUri(textVersion.substring(0, seperatorIndex));
            fieldName = textVersion.substring(seperatorIndex + 1);
        } else {
            // create invalid uri
            fieldName = null;
            componentUri = new SimpleUri();
        }
    }
    
    /**
     * Returns a Name object which is the object name of the componentUri
     * 
     * @return a Name object which contains the object name of the componentUri
     */
    public Name getObjectName() {
        return componentUri.getObjectName();
    }
    
    /**
     * Returns a boolean with the value 'True' if the ComponentFieldUri is valid
     * 
     * @return a Boolean  with the value 'True' if the ComponentFieldUri is valid
     */
    @Override
    public boolean isValid() {
        return componentUri.isValid() && fieldName != null;
    }
    
    /**
     * Returns a Name object which is the module name of the componentUri
     * 
     * @return a Name object which contains the module name of the componentUri
     */
    @Override
    public Name getModuleName() {
        return componentUri.getModuleName();
    }
    
    /**
     * Returns the componentUri of the ComponentFieldUri object
     * 
     * @return a SimpleUri which is the componentUri of the ComponentFieldUri object
     */
    public SimpleUri getComponentUri() {
        return componentUri;
    }
    
    /**
     * Returns the fieldName of the ComponentFieldUri object
     * 
     * @return a String which contains the fieldName of the ComponentFieldUri object
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Returns a boolean with the value of True if the other object is equal to itself
     * 
     * @param o   The object which is being compared to itself
     * @return    a Boolean with the value of True if the other object is equal to itself
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentFieldUri that = (ComponentFieldUri) o;

        if (!componentUri.equals(that.componentUri)) {
            return false;
        }
        return Objects.equals(fieldName, that.fieldName);
    }
    
    /**
     * Calculates and returns an int which is the hash value of the ComponentFieldUri object
     * 
     * @return an int which is equal to the hash value of the ComponentFieldUri object
     */
    @Override
    public int hashCode() {
        int result = componentUri.hashCode();
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }
    
    /**
     * Returns a String representation of the ComponentFieldUri object.
     * 
     * @return a String representation of the ComponentFieldUri object. Will return an empty String if ComponentFieldUri is invalid
     */
    @Override
    public String toString() {
        if (!isValid()) {
            return "";
        }
        return componentUri + FIELD_SEPARATOR + fieldName;
    }
}
