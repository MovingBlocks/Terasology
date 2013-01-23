package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

/**
 * @author Immortius
 */
public class ComponentMetadata<T extends Component> extends ClassMetadata<T> {

    private boolean replicated = false;
    private boolean replicatedFromOwner = false;


    public ComponentMetadata(Class<T> simpleClass, String... names) throws NoSuchMethodException {
        super(simpleClass, names);
        replicated = simpleClass.getAnnotation(Replicate.class) != null;
    }

    public void addField(FieldMetadata fieldInfo) {
        super.addField(fieldInfo);
        if (fieldInfo.isReplicated()) {
            replicated = true;
            if (fieldInfo.getReplicationInfo().value().isReplicateFromOwner()) {
                replicatedFromOwner = true;
            }
        }
    }

    public boolean isReplicatedFromOwner() {
        return replicatedFromOwner;
    }

    public boolean isReplicated() {
        return replicated;
    }
}
