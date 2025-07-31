package model;

/**
 * Enumeration representing the possible states of a cell on the game board.
 * 
 * <p>Each cell can be in one of several states that indicate its current
 * condition in the naval battle game. The state changes as the game
 * progresses through ship placement and combat phases.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Cell
 * @see Board
 */
public enum CellState {
    
    /**
     * The cell is empty and has not been shot at.
     */
    EMPTY,
    
    /**
     * The cell contains part of a ship that has not been hit.
     */
    SHIP_PART,
    
    /**
     * The cell has been shot at but contained no ship (missed shot).
     */
    WATER,
    
    /**
     * The cell contains part of a ship that has been hit but not sunk.
     */
    HIT_SHIP_PART,
    
    /**
     * The cell contains part of a ship that has been completely sunk.
     */
    SUNK_SHIP_PART
}
