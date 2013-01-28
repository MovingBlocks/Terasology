package org.terasology.logic.characters;

import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius
 */
public interface CharacterMovementSystem {
    /**
     * Steps the state of a character
     * @param initial The initial state to start from
     * @param input The input driving the movement change
     * @param entity The character
     * @return The new state of the character
     */
    CharacterStateEvent step(CharacterStateEvent initial, CharacterMoveInputEvent input, EntityRef entity);

}
