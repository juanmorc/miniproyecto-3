package model;

/**
 * Represents a Submarine in the naval battle game.
 * 
 * <p>The Submarine is a medium-large ship, occupying 3 cells on the board.
 * It inherits all behavior from the Ship class and only needs to specify
 * its type during construction.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see ShipType#SUBMARINE
 */
public class Submarine extends Ship {
    
    /**
     * Constructs a new Submarine.
     * 
     * <p>Initializes the ship with the SUBMARINE type,
     * which automatically sets its size to 3 cells.</p>
     */
    public Submarine() {
        super(ShipType.SUBMARINE);
    }
}
