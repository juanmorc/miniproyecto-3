package model;

/**
 * Enumeration representing the possible states of the game.
 * 
 * <p>The game progresses through different states from initialization
 * to completion. Each state determines what actions are valid and
 * what the game should be doing next.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Game
 */
public enum GameState {
    
    /**
     * Game is being initialized and set up.
     */
    INITIALIZING,
    
    /**
     * Players are placing their ships on the board.
     */
    SHIP_PLACEMENT,
    
    /**
     * It's the human player's turn to make a move.
     */
    PLAYER_TURN,
    
    /**
     * It's the machine player's turn to make a move.
     */
    MACHINE_TURN,
    
    /**
     * Game has ended with human player victory.
     */
    GAME_OVER_HUMAN_WINS,
    
    /**
     * Game has ended with machine player victory.
     */
    GAME_OVER_MACHINE_WINS
}
