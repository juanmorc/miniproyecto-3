package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a ship in the naval battle game.
 * 
 * <p>This class provides the common functionality for all types of ships
 * including size management, hit tracking, sinking mechanics, and cell
 * occupation. Specific ship types extend this class to define their
 * characteristics.</p>
 * 
 * <p>Ships can be placed on the game board in different orientations
 * (horizontal or vertical) and track their health through hit registration.
 * When a ship receives hits equal to its size, it becomes sunk and all
 * its occupied cells are marked accordingly.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see ShipType
 * @see Cell
 * @see Orientation
 * @see Board
 */
public abstract class Ship implements Serializable {
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The type of this ship (defines size and characteristics).
     */
    protected ShipType type;
    
    /**
     * The size of this ship (number of cells it occupies).
     */
    protected int size;
    
    /**
     * The number of hits this ship has received.
     */
    protected int hits;
    
    /**
     * Whether this ship has been sunk.
     */
    protected boolean sunk;
    
    /**
     * The list of cells this ship occupies on the board.
     */
    protected List<Cell> occupiedCells;
    
    /**
     * The orientation of this ship (horizontal or vertical).
     */
    protected Orientation orientation;

    /**
     * Constructs a new ship of the specified type.
     * 
     * <p>Initializes the ship with default values: zero hits, not sunk,
     * and an empty list of occupied cells. The ship size is determined
     * by the ship type.</p>
     * 
     * @param type the type of ship to create
     * @throws IllegalArgumentException if type is null
     */
    public Ship(ShipType type){
        if (type == null) {
            throw new IllegalArgumentException("Ship type cannot be null");
        }
        this.type = type;
        this.size = type.getSize();
        this.hits = 0;
        this.sunk = false;
        this.occupiedCells = new ArrayList<Cell>();
    }

    /**
     * Gets the type of this ship.
     * 
     * @return the ship type
     */
    public ShipType getType() {
        return type;
    }

    /**
     * Gets the size of this ship.
     * 
     * @return the number of cells this ship occupies
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the number of hits this ship has received.
     * 
     * @return the hit count
     */
    public int getHits() {
        return hits;
    }

    /**
     * Checks if this ship has been sunk.
     * 
     * @return true if the ship is sunk, false otherwise
     */
    public boolean isSunk() {
        return sunk;
    }

    /**
     * Gets the list of cells this ship occupies.
     * 
     * @return an unmodifiable view of the occupied cells
     */
    public List<Cell> getOccupiedCells() {
        return occupiedCells;
    }

    /**
     * Gets the orientation of this ship.
     * 
     * @return the ship orientation (horizontal or vertical)
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Sets the orientation of this ship.
     * 
     * @param orientation the new orientation for the ship
     */
    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    /**
     * Adds a cell to the list of cells occupied by this ship.
     * 
     * <p>This method establishes the relationship between the ship
     * and the cell by setting the cell's ship part reference and
     * updating the cell state to indicate it contains a ship part.</p>
     * 
     * @param cell the cell to be occupied by this ship
     * @throws IllegalArgumentException if cell is null
     */
    public void addOccupiedCell(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null");
        }
        this.occupiedCells.add(cell);
        cell.setShipPart(this);
        cell.setCellState(CellState.SHIP_PART);
    }

    /**
     * Registers a hit on this ship and checks if it becomes sunk.
     * 
     * <p>Increments the hit counter and determines if the ship is sunk
     * (hits equal to ship size). If the ship becomes sunk, all occupied
     * cells are marked as sunk ship parts.</p>
     * 
     * @return true if the ship became sunk as a result of this hit,
     *         false if the ship was already sunk or is still afloat
     */
    public boolean registerHit() {
        if(!sunk){
            hits++;
            if (hits >= size){
                sunk = true;
                for (Cell cell : occupiedCells){
                    cell.setCellState(CellState.SUNK_SHIP_PART);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Resets this ship to its initial state.
     * 
     * <p>Clears all hits and removes the sunk status, effectively
     * restoring the ship to full health. This is typically used
     * when starting a new game.</p>
     */
    public void reset(){
        this.hits = 0;
        this.sunk = false;
    }
}
