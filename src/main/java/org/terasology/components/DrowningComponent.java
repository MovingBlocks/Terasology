package org.terasology.components;

import org.terasology.entitySystem.Component;

/**
 * Created with IntelliJ IDEA.
 * User: ancaplinger
 * Date: 7/10/13
 * Time: 9:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class DrowningComponent implements Component{

    // Constants
    public final int DEFAULT_BREATH_DURATION = 15000; // 15 seconds

    // These can be safely configured
    public int timeBeforeDrown = DEFAULT_BREATH_DURATION;
    public int timeBetweenDamageTicks = 1000; // 1 second

    // These track time and state
    public long timeEnteredLiquid = 0;
    public long timeLastDrownTick = 0;
    public boolean underWater = false;
}
