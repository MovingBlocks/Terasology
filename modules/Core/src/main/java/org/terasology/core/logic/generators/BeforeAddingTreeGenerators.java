package org.terasology.core.logic.generators;

import org.terasology.core.world.generator.chunkGenerators.ForestGenerator;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class BeforeAddingTreeGenerators extends AbstractConsumableEvent {
    private ForestGenerator forestGenerator;

    public BeforeAddingTreeGenerators(ForestGenerator forestGenerator) {
        this.forestGenerator = forestGenerator;
    }

    public ForestGenerator getForestGenerator() {
        return forestGenerator;
    }
}
