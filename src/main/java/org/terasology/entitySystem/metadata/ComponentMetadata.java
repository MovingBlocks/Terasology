/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.world.block.ForceBlockActive;

/**
 * @author Immortius
 */
public class ComponentMetadata<T extends Component> extends ClassMetadata<T> {

    private boolean replicated = false;
    private boolean replicatedFromOwner = false;
    private boolean referenceOwner = false;
    private boolean forceBlockActive = false;
    private boolean retainUnalteredOnBlockChange = false;

    public ComponentMetadata(Class<T> simpleClass, String... names) throws NoSuchMethodException {
        super(simpleClass, names);
        replicated = simpleClass.getAnnotation(Replicate.class) != null;
        ForceBlockActive forceBlockActiveAnnotation = simpleClass.getAnnotation(ForceBlockActive.class);
        if (forceBlockActiveAnnotation != null) {
            forceBlockActive = true;
            retainUnalteredOnBlockChange = forceBlockActiveAnnotation.retainUnalteredOnBlockChange();
        }
    }

    public void addField(FieldMetadata fieldInfo) {
        super.addField(fieldInfo);
        if (fieldInfo.isReplicated()) {
            replicated = true;
            if (fieldInfo.getReplicationInfo().value().isReplicateFromOwner()) {
                replicatedFromOwner = true;
            }
        }
        if (fieldInfo.isOwnedReference()) {
            referenceOwner = true;
        }
    }

    public T clone(Component component) {
        if (getType().isInstance(component)) {
            return super.clone(getType().cast(component));
        }
        return null;
    }

    public boolean isReferenceOwner() {
        return referenceOwner;
    }

    public boolean isReplicatedFromOwner() {
        return replicatedFromOwner;
    }

    public boolean isReplicated() {
        return replicated;
    }

    public boolean isForceBlockActive() {
        return forceBlockActive;
    }

    public boolean isRetainUnalteredOnBlockChange() {
        return retainUnalteredOnBlockChange;
    }
}
