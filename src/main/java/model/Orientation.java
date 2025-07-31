package model;

/**
 * Enumeration representing the possible orientations for ship placement.
 * 
 * <p>Ships can be placed on the board in two orientations: horizontally
 * (extending left to right) or vertically (extending top to bottom).
 * This affects how the ship occupies consecutive cells on the board.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see Board
 */
public enum Orientation {
    
    /**
     * Ship extends horizontally from left to right.
     */
    HORIZONTAL,
    
    /**
     * Ship extends vertically from top to bottom.
     */
    VERTICAL
}
