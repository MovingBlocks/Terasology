package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;
import org.terasology.network.NoReplicate;

/**
 * @author Immortius
 */
public class ComponentMetadata<T extends Component> extends ClassMetadata<T> {

    private boolean noReplicateExistance = false;
    private boolean replicated = false;
    private boolean replicatedFromOwner = false;


    public ComponentMetadata(Class<T> simpleClass, String... names) throws NoSuchMethodException {
        super(simpleClass, names);
        noReplicateExistance = simpleClass.getAnnotation(NoReplicate.class) != null;
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

    public boolean isNoReplicateExistence() {
        return noReplicateExistance;
    }

    public boolean isReplicatedFromOwner() {
        return replicatedFromOwner;
    }

    public boolean isReplicated() {
        return replicated;
    }
}
