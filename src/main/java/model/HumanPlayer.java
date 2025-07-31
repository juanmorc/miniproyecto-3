package model;

/**
 * Represents a human player in the naval battle game.
 * 
 * <p>Human players interact with the game through the user interface
 * for ship placement and shot selection. The actual input handling
 * is delegated to the UI controllers rather than being implemented
 * directly in this class.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Player
 * @see MachinePlayer
 */
public class HumanPlayer extends Player {
    
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new human player with the specified name.
     * 
     * @param name the name of the human player
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Handles ship placement for the human player.
     * 
     * <p>For human players, ship placement is typically handled through
     * the user interface. This method provides a placeholder
     * implementation that can be overridden or extended as needed.</p>
     */
    @Override
    public void placeShips(){
        System.out.println(name + " está colocando sus barcos.");
    }

    /**
     * Gets the next shot coordinates for the human player.
     * 
     * <p>Human player shots are handled through the user interface,
     * so this method throws an UnsupportedOperationException to
     * indicate that the UI should handle shot input instead.</p>
     * 
     * @param opponentBoard the opponent's board (not used for human players)
     * @return this method never returns normally
     * @throws UnsupportedOperationException always, as human shots are UI-handled
     */
    @Override
    public int[] getNextShot(Board opponentBoard){
        throw new UnsupportedOperationException("El disparo del jugador humano es manejado por la interfaz.");
    }
}
