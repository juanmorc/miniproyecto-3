package model;

import model.exceptions.CellAlreadyShotException;
import model.exceptions.InvalidShipPlacementException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game board in the Battle Naval Fury game.
 * 
 * <p>The board is a 10x10 grid that manages ship placement, shot tracking,
 * and game state. Each board maintains its own collection of ships and
 * tracks which cells have been targeted.</p>
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Managing the grid of cells</li>
 *   <li>Validating and placing ships</li>
 *   <li>Processing shots and determining results</li>
 *   <li>Tracking sunk ships</li>
 * </ul>
 * 
 * @author Battle Naval Fury Team
 * @version 1.0
 * @since 1.0
 * @see Cell
 * @see Ship
 * @see Player
 */
public class Board implements Serializable {
    
    /** Serial version UID for serialization compatibility */
    private static final long serialVersionUID = 1L;
    
    /** Standard board size (10x10 grid) */
    public final static int SIZE = 10;

    /** The main grid of cells representing the board */
    private Cell[][] grid;
    
    /** Grid tracking which cells have been shot at */
    private boolean[][] shotGrid;
    
    /** List of all ships placed on this board */
    private List<Ship> ships;
    
    /** Count of ships that have been completely sunk */
    private int sunkShipsCount;

    /**
     * Creates a new empty board with initialized grid.
     * 
     * <p>Initializes a 10x10 grid of cells and sets up tracking arrays
     * for shots and ships. All cells start empty and unshot.</p>
     */
    public Board() {
        this.grid = new Cell[SIZE][SIZE];
        this.shotGrid = new boolean[SIZE][SIZE];
        this.ships = new ArrayList<Ship>();
        initializeGrid();
        this.sunkShipsCount = 0;
    }

    /**
     * Initializes the grid with empty cells.
     * 
     * <p>Creates Cell objects for each position in the grid and
     * resets the shot tracking grid to false.</p>
     */
    public void initializeGrid() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = new Cell(i, j);
                shotGrid[i][j] = false;
            }
        }
    }

    /**
     * Gets the cell at the specified coordinates.
     * 
     * @param row the row coordinate (0-9)
     * @param col the column coordinate (0-9)
     * @return the Cell at the specified position, or null if coordinates are invalid
     */
    public Cell getCell(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            return grid[row][col];
        }
        return null;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    public int getSunkShipsCount() {
        return sunkShipsCount;
    }

    public boolean canPlaceShip(Ship ship, int startRow, int startCol, Orientation orientation) {
        int shipSize = ship.getSize();

        if (startCol < 0 || startCol >= SIZE || startRow < 0 || startRow >= SIZE) {
            return false;
        }

        if (orientation == Orientation.HORIZONTAL) {
            if (startCol + shipSize > SIZE) return false;
            for (int i = 0; i < shipSize; i++) {
                if (grid[startRow][startCol + i].getShipPart() != null) return false;
            }
        } else {
            if (startRow + shipSize > SIZE) return false;
            for (int i = 0; i < shipSize; i++) {
                if (grid[startRow + i][startCol].getShipPart() != null) return false;
            }
        }
        return true;
    }

    public void placeShip(Ship ship, int startRow, int startCol, Orientation orientation) throws InvalidShipPlacementException {
        if (!canPlaceShip(ship, startRow, startCol, orientation)) {
            throw new InvalidShipPlacementException("No se puede colocar el barco de tipo " + ship.getType().getDisplayName() +
                    " en (" + startRow + "," + startCol + ") con orientación " + orientation);
        }

        ship.setOrientation(orientation);

        for (int i = 0; i < ship.getSize(); i++) {
            Cell currentCell;
            if (orientation == Orientation.HORIZONTAL) {
                currentCell = grid[startRow][startCol + i];
            } else {
                currentCell = grid[startRow + i][startCol];
            }
            ship.addOccupiedCell(currentCell);
        }

        ships.add(ship);
    }

    public ShotResult receiveShot(int row, int col) throws CellAlreadyShotException {
        Cell targetCell = getCell(row, col);
        if (targetCell == null) {
            return ShotResult.INVALID_SHOT;
        }
        if (targetCell.isShot()) {
            throw new CellAlreadyShotException("Celda (" + row + "," + col + ") ya disparada.");
        }
        shotGrid[row][col] = true;

        Ship shipPart = targetCell.getShipPart();
        if (shipPart != null) {
            targetCell.setCellState(CellState.HIT_SHIP_PART);
            boolean justSunk = shipPart.registerHit();
            if (justSunk) {
                sunkShipsCount++;
                return ShotResult.SUNK;
            }
            return ShotResult.TOUCH;
        } else {
            targetCell.setCellState(CellState.WATER);
            return ShotResult.WATER;
        }
    }
    public boolean allShipsSunk() {
        if (ships.isEmpty() && sunkShipsCount == 0) return false;
        return sunkShipsCount == ships.size();
    }
    public void resetBoard() {
        initializeGrid();
        ships.clear();
        sunkShipsCount = 0;
    }
    public boolean hasShip(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return false;
        }
        return grid[row][col] != null && grid[row][col].getShipPart() != null;
    }
    public boolean wasShot(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return false;
        }
        return shotGrid[row][col];
    }
}