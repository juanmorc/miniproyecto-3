package model;

/**
 * Represents an Aircraft Carrier in the naval battle game.
 * 
 * <p>The Aircraft Carrier is the largest ship in the game, occupying
 * 4 cells on the board. It inherits all behavior from the Ship class
 * and only needs to specify its type during construction.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Ship
 * @see ShipType#AIRCRAFT_CARRIER
 */
public class AircraftCarrier extends Ship {
    
    /**
     * Constructs a new Aircraft Carrier.
     * 
     * <p>Initializes the ship with the AIRCRAFT_CARRIER type,
     * which automatically sets its size to 4 cells.</p>
     */
    public AircraftCarrier() {
        super(ShipType.AIRCRAFT_CARRIER);
    }
}
