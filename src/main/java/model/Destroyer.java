package model;

/**
 * Represents a Destroyer in the naval battle game.
 * 
 * <p>The Destroyer is a medium-sized ship, occupying 2 cells on the board.
 * It inherits all behavior from the Ship class and only needs to specify
 * its type during construction.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see ShipType#DESTROYER
 */
public class Destroyer extends Ship {
    
    /**
     * Constructs a new Destroyer.
     * 
     * <p>Initializes the ship with the DESTROYER type,
     * which automatically sets its size to 2 cells.</p>
     */
    public Destroyer() {
        super(ShipType.DESTROYER);
    }
}
