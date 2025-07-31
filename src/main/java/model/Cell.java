package model;

import java.io.Serializable;

/**
 * Represents a single cell on the game board in the naval battle game.
 * 
 * <p>Each cell has a position defined by row and column coordinates,
 * a state that indicates its current condition (empty, contains ship,
 * hit, etc.), and an optional reference to a ship part if the cell
 * is occupied by a ship.</p>
 * 
 * <p>Cells track whether they have been shot at and maintain the
 * relationship with ships that occupy them. The cell state changes
 * as the game progresses through shooting and ship sinking.</p>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see CellState
 * @see Ship
 * @see Board
 */
public class Cell implements Serializable {
    /**
     * Serial version UID for serialization compatibility.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The row position of this cell on the board (0-based).
     */
    private int row;
    
    /**
     * The column position of this cell on the board (0-based).
     */
    private int col;
    
    /**
     * The current state of this cell.
     */
    private CellState cellState;
    
    /**
     * Reference to the ship part occupying this cell, null if empty.
     */
    private Ship shipPart;

    /**
     * Constructs a new cell at the specified position.
     * 
     * <p>Initializes the cell as empty with no ship part.</p>
     * 
     * @param row the row position (0-based)
     * @param col the column position (0-based)
     * @throws IllegalArgumentException if row or col is negative
     */
    public Cell(int row, int col) {
        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Row and column must be non-negative");
        }
        this.row = row;
        this.col = col;
        this.cellState = CellState.EMPTY;
        this.shipPart = null;
    }

    /**
     * Gets the row position of this cell.
     * 
     * @return the row coordinate (0-based)
     */
    public int getRow() {
        return row;
    }

    /**
     * Gets the column position of this cell.
     * 
     * @return the column coordinate (0-based)
     */
    public int getCol() {
        return col;
    }

    /**
     * Gets the current state of this cell.
     * 
     * @return the cell state
     */
    public CellState getCellState() {
        return cellState;
    }

    /**
     * Gets the ship part occupying this cell.
     * 
     * @return the ship occupying this cell, or null if the cell is empty
     */
    public Ship getShipPart() {
        return shipPart;
    }

    /**
     * Sets the ship part that occupies this cell.
     * 
     * @param shipPart the ship to occupy this cell, or null to clear
     */
    public void setShipPart(Ship shipPart) {
        this.shipPart = shipPart;
    }

    /**
     * Sets the state of this cell.
     * 
     * @param cellState the new state for this cell
     * @throws IllegalArgumentException if cellState is null
     */
    public void setCellState(CellState cellState) {
        if (cellState == null) {
            throw new IllegalArgumentException("Cell state cannot be null");
        }
        this.cellState = cellState;
    }

    /**
     * Checks if this cell has been shot at.
     * 
     * <p>A cell is considered shot if its state is WATER (missed shot),
     * HIT_SHIP_PART (successful hit), or SUNK_SHIP_PART (part of sunk ship).</p>
     * 
     * @return true if the cell has been shot at, false otherwise
     */
    boolean isShot(){
        return cellState == CellState.WATER || cellState == CellState.HIT_SHIP_PART || cellState == CellState.SUNK_SHIP_PART;
    }

    /**
     * Returns a string representation of this cell.
     * 
     * <p>Includes the cell coordinates, state, and ship information
     * if a ship occupies this cell.</p>
     * 
     * @return a formatted string describing this cell
     */
    @Override
    public String toString() {
        return "(" + row + "," + col + ") " + cellState + (shipPart != null ? " [" + shipPart.getType().getDisplayName() + "]" : "");
    }
}
