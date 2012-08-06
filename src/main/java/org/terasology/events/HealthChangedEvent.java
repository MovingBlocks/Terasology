package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 */
//TODO this class will be used instead of the FullHealth, NoHealth event.
public class HealthChangedEvent extends AbstractEvent {
	private EntityRef instigator;
	private int currentHealth;
	private int maxHealth;

	public HealthChangedEvent(EntityRef instigator, int currentHealth, int maxHealth) {
		this.instigator = instigator;
		this.currentHealth = currentHealth;
		this.maxHealth = maxHealth;
	}
	
    public EntityRef getInstigator() {
        return instigator;
    }
	
	public int getCurrentHealth() {
		return currentHealth;
	}

	public int getMaxHealth() {
		return maxHealth;
	}
}
