package model;

import java.io.Serializable;

/**
 * Abstract base class representing a player in the Battle Naval Fury game.
 * 
 * <p>This class provides the common functionality and interface for both
 * human and AI players. Each player maintains their own board and implements
 * specific behaviors for ship placement and shooting strategies.</p>
 * 
 * <p>Subclasses must implement:</p>
 * <ul>
 *   <li>{@link #placeShips()} - Strategy for placing ships on the board</li>
 *   <li>{@link #getNextShot(Board)} - Strategy for selecting targets</li>
 * </ul>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see HumanPlayer
 * @see MachinePlayer
 * @see Board
 */
public abstract class Player implements Serializable {
    
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;
    
    /** The player's name/identifier */
    protected String name;
    
    /** The player's game board */
    protected Board board;

    /**
     * Creates a new player with the specified name.
     * 
     * <p>Initializes the player with an empty board ready for ship placement.</p>
     * 
     * @param name the player's name
     * @throws IllegalArgumentException if name is null or empty
     */
    public Player(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be null or empty");
        }
        this.name = name;
        this.board = new Board();
    }

    /**
     * Gets the player's name.
     * 
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's board.
     * 
     * @return the player's board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Places all ships on the player's board.
     * 
     * <p>This method must be implemented by subclasses to define
     * the specific ship placement strategy (manual for humans,
     * automatic for AI).</p>
     */
    public abstract void placeShips();

    /**
     * Determines the next shot coordinates for this player.
     * 
     * <p>This method must be implemented by subclasses to define
     * the targeting strategy. Human players typically get input
     * from the UI, while AI players use algorithmic selection.</p>
     * 
     * @param opponentBoard the opponent's board to target
     * @return array containing [row, col] coordinates for the next shot,
     *         or null if no valid shots are available
     */
    public abstract int[] getNextShot(Board opponentBoard);
}
