package model;

/**
 * Enumeration representing the possible results of a shot attempt.
 * 
 * <p>When a player takes a shot at the opponent's board, the result
 * can be one of several outcomes that affect the game state and
 * provide feedback to the player.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Game
 * @see Board
 */
public enum ShotResult {
    
    /**
     * The shot missed - no ship was hit.
     */
    WATER,
    
    /**
     * The shot hit a ship but didn't sink it.
     */
    TOUCH,
    
    /**
     * The shot hit and sunk a ship completely.
     */
    SUNK,
    
    /**
     * The target cell has already been shot at.
     */
    ALREADY_SHOT,
    
    /**
     * The shot coordinates are invalid (out of bounds).
     */
    INVALID_SHOT
}
