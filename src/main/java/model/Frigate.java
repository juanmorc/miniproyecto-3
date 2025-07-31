package model;

/**
 * Represents a Frigate in the naval battle game.
 * 
 * <p>The Frigate is the smallest ship in the game, occupying only 1 cell
 * on the board. It inherits all behavior from the Ship class and only
 * needs to specify its type during construction.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see ShipType#FRIGATE
 */
public class Frigate extends Ship {
    
    /**
     * Constructs a new Frigate.
     * 
     * <p>Initializes the ship with the FRIGATE type,
     * which automatically sets its size to 1 cell.</p>
     */
    public Frigate() {
        super(ShipType.FRIGATE);
    }
}
