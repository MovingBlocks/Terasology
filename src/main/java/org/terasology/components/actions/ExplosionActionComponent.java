package org.terasology.components.actions;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ExplosionActionComponent implements Component {
    public ActionTarget relativeTo = ActionTarget.Instigator;
}
